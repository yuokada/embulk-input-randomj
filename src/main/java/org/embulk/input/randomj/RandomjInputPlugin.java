package org.embulk.input.randomj;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.Task;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.Column;
import org.embulk.spi.Exec;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfig;
import org.embulk.spi.time.Timestamp;
import org.embulk.spi.type.Type;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomjInputPlugin
        implements InputPlugin
{
    private final RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
            .build();

    public interface PluginTask
            extends Task
    {
        // configuration row (required integer)
        @Config("rows")
        public int getRows();

        // ref: https://github.com/embulk/embulk-input-jdbc/blob/master/embulk-input-mysql/src/main/java/org/embulk/input/MySQLInputPlugin.java#L33-L35
        @Config("threads")
        @ConfigDefault("1")
        public int getThreads();

        @Config("primary_key")
        @ConfigDefault("")
        String getPrimaryKey();

        @Config("schema")
        public SchemaConfig getSchema();
    }

    @Override
    public ConfigDiff transaction(ConfigSource config,
            InputPlugin.Control control)
    {
        PluginTask task = config.loadConfig(PluginTask.class);

        Schema schema = task.getSchema().toSchema();
        int taskCount = task.getThreads();  // number of run() method calls

        return resume(task.dump(), schema, taskCount, control);
    }

    @Override
    public ConfigDiff resume(TaskSource taskSource,
            Schema schema, int taskCount,
            InputPlugin.Control control)
    {
        control.run(taskSource, schema, taskCount);
        return Exec.newConfigDiff();
    }

    @Override
    public void cleanup(TaskSource taskSource,
            Schema schema, int taskCount,
            List<TaskReport> successTaskReports)
    {
    }

    @Override
    public TaskReport run(TaskSource taskSource,
            Schema schema, int taskIndex,
            PageOutput output)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        Integer rows = (Integer) task.getRows();
        try (PageBuilder pagebuilder =
                new PageBuilder(Exec.getBufferAllocator(), schema, output)) {

            Random rnd = new Random();
            List<Integer> rowNumbers = IntStream.rangeClosed(
                    taskIndex * rows + 1,
                    taskIndex * rows + rows
            ).boxed().collect(Collectors.toList());
            for (Integer rowNumber : rowNumbers) {
                for (int i = 0; i < schema.size(); i++) {
                    Column column = schema.getColumn(i);
                    Type type = column.getType();

                    switch (type.getName()) {
                        case "long":
                            final String pk = task.getPrimaryKey();
                            if (column.getName().equals(pk)) {
                                pagebuilder.setLong(i, rowNumber);
                            }
                            else {
                                pagebuilder.setLong(i, rnd.nextInt(10000));
                            }
                            break;
                        case "double":
                            pagebuilder.setDouble(i, rnd.nextDouble() * 10000);
                            break;
                        case "boolean":
                            if (Math.random() < 0.5) {
                                pagebuilder.setBoolean(i, false);
                            }
                            else {
                                pagebuilder.setBoolean(i, true);
                            }
                            break;
                        case "string":
                            pagebuilder.setString(i, generator.generate(32));
                            break;
                        case "timestamp":
                            final ZoneId zoneId = ZoneId.systemDefault();
                            final double randd = Math.random();
                            LocalDateTime randomDate = LocalDateTime.now()
                                    .plusDays((long) (randd * 100))
                                    .plusSeconds((long) (randd * 1000000));
                            Timestamp timestamp = Timestamp.ofEpochSecond(
                                    randomDate.atZone(zoneId).toEpochSecond()
                            );
                            pagebuilder.setTimestamp(column, timestamp);
                            break;
                        default:
                            System.out.println("Unsupported type");
                            break;
                    }
                }
                pagebuilder.addRecord();
            }
            pagebuilder.finish();
        }

        TaskReport taskReport = Exec.newTaskReport();
        taskReport.set("columns", schema.size());
        taskReport.set("rows", rows);
        return taskReport;
    }

    @Override
    public ConfigDiff guess(ConfigSource config)
    {
        return Exec.newConfigDiff();
    }
}
