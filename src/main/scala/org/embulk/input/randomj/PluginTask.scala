package org.embulk.input.randomj

import org.embulk.config.{Config, ConfigDefault, Task}
import org.embulk.spi.SchemaConfig

trait PluginTask extends Task {
  @Config("rows") def getRows: Int

  // ref: https://github.com/embulk/embulk-input-jdbc/blob/master/embulk-input-mysql/src/main/java/org/embulk/input/MySQLInputPlugin.java#L33-L35
  @Config("threads")
  @ConfigDefault("1")
  def getThreads: Int

  @Config("primary_key")
  @ConfigDefault("")
  def getPrimaryKey: String

  @Config("schema")
  def getSchema: SchemaConfig
}
