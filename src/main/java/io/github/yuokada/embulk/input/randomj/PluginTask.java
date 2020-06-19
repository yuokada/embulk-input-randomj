package io.github.yuokada.embulk.input.randomj;

import org.embulk.config.Config;
import org.embulk.config.ConfigDefault;
import org.embulk.config.Task;
import org.embulk.spi.SchemaConfig;

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
