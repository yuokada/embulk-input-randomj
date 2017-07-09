package org.embulk.input.randomj;

import org.embulk.EmbulkTestRuntime;
import org.embulk.config.ConfigSource;
import org.embulk.spi.Exec;
import org.embulk.spi.Schema;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static org.embulk.spi.type.Types.LONG;
import static org.hamcrest.CoreMatchers.is;

public class TestRandomjInputPlugin
{
    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    @Test
    public void checkDefaultValues()
    {
        // see: https://github.com/sonots/embulk-filter-column/blob/master/src/test/java/org/embulk/filter/column/TestColumnFilterPlugin.java
        Schema schema = Schema.builder().add("myid", LONG).build();

        ConfigSource config = Exec.newConfigSource()
                .set("rows", 1)
//                .set("primary_key", "myid")
                .set("schema", schema);

        RandomjInputPlugin.PluginTask task = config.loadConfig(RandomjInputPlugin.PluginTask.class);

        Assert.assertThat(task.getThreads(), is(1));
        Assert.assertThat(task.getPrimaryKey().isPresent(), is(false));
//        assertEquals(".parquet", task.getFileNameExtension());
//        assertEquals(".%03d", task.getSequenceFormat());
//        assertFalse(task.getOverwrite());
    }
}
