package org.embulk.input.randomj;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.embulk.config.ConfigDiff;
import org.embulk.config.ConfigSource;
import org.embulk.config.TaskReport;
import org.embulk.config.TaskSource;
import org.embulk.spi.Column;
import org.embulk.spi.ColumnConfig;
import org.embulk.spi.Exec;
import org.embulk.spi.InputPlugin;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.PageOutput;
import org.embulk.spi.Schema;
import org.embulk.spi.SchemaConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class RandomjInputPlugin
        implements InputPlugin
{
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
        // throw new UnsupportedOperationException("randomj input plugin does not support cleanup");
    }

    @Override
    public TaskReport run(TaskSource taskSource,
            Schema schema, int taskIndex,
            PageOutput output)
    {
        PluginTask task = taskSource.loadTask(PluginTask.class);
        Integer rows = task.getRows();
        final HashMap<Column, Map<String, Integer>> columnOptions = getColumnOptions(task);
        final HashMap<Column, List<JsonNode>> columnSchemas = getColumnSchemas(task);
        try (PageBuilder pagebuilder =
                new PageBuilder(Exec.getBufferAllocator(), schema, output)) {
            IntStream.rangeClosed(
                    taskIndex * rows + 1,
                    taskIndex * rows + rows
            ).boxed().forEach(rowNumber -> {
                RandomjColumnVisitor visitor = new RandomjColumnVisitor(pagebuilder, task, rowNumber, columnOptions, columnSchemas);
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

    HashMap<Column, Map<String, Integer>> getColumnOptions(PluginTask task)
    {
        SchemaConfig schemaConfig = task.getSchema();
        Schema schema = schemaConfig.toSchema();
        HashMap<Column, Map<String, Integer>> lengthMap = new HashMap<>();
        for (Column column : schema.getColumns()) {
            HashMap<String, Integer> miniMap = new HashMap<>();
            ColumnConfig c = schemaConfig.getColumn(column.getIndex());
            miniMap.put("length", c.getOption().get(Integer.class, "length", 0));
            miniMap.put("null_rate", c.getOption().get(Integer.class, "null_rate", 0));
            miniMap.put("max_value", c.getOption().get(Integer.class, "max_value", null));
            miniMap.put("min_value", c.getOption().get(Integer.class, "min_value", null));
            lengthMap.put(column, miniMap);
        }
        return lengthMap;
    }

    HashMap<Column, List<JsonNode>> getColumnSchemas(PluginTask task)
    {
        SchemaConfig schemaConfig = task.getSchema();
        Schema schema = schemaConfig.toSchema();
        HashMap<Column, List<JsonNode>> schemaMap = new HashMap<>();
        for (Column column : schema.getColumns()) {
            String schemaString = schemaConfig
                    .getColumn(column.getIndex())
                    .getOption().get(String.class, "schema", "");
            if (!schemaString.isEmpty()) {
                try {
                    List<JsonNode> jsonNodes = new ObjectMapper().readValue(schemaString, new TypeReference<List<JsonNode>>() {});
                    schemaMap.put(column, jsonNodes);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return schemaMap;
    }

    @Override
    public ConfigDiff guess(ConfigSource config)
    {
        return Exec.newConfigDiff();
    }
}
