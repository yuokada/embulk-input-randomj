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
import org.embulk.spi.Exec;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfig;

import java.util.List;
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
        int getRows();

        // ref: https://github.com/embulk/embulk-input-jdbc/blob/master/embulk-input-mysql/src/main/java/org/embulk/input/MySQLInputPlugin.java#L33-L35
        @Config("threads")
        @ConfigDefault("1")
        Integer getThreads();

        @Config("primary_key")
        @ConfigDefault("")
        String getPrimaryKey();

        @Config("schema")
        SchemaConfig getSchema();
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
        Integer rows = task.getRows();
        try (PageBuilder pagebuilder =
                new PageBuilder(Exec.getBufferAllocator(), schema, output)) {
            IntStream.rangeClosed(
                    taskIndex * rows + 1,
                    taskIndex * rows + rows
            ).boxed().forEach(rowNumber -> {
                RandomColumnVisitor visitor = new RandomColumnVisitor(pagebuilder, task, rowNumber);
                schema.visitColumns(visitor);
                pagebuilder.addRecord();
            });
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
