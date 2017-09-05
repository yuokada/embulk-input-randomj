package org.embulk.input.randomj

import java.time.{LocalDateTime, ZoneId}
import java.util
import java.util.Random
import java.util.stream.IntStream

import org.apache.commons.text.{CharacterPredicates, RandomStringGenerator}
import org.embulk.config.{ConfigDiff, ConfigSource, TaskReport, TaskSource}
import org.embulk.spi.{Exec, InputPlugin, PageBuilder, PageOutput, Schema}
import org.embulk.spi.time.Timestamp

object RandomjInputPlugin {
}

class RandomjInputPlugin extends InputPlugin {
  final private val generator = new RandomStringGenerator.Builder().withinRange('0', 'z').filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS).build

  override def transaction(config: ConfigSource, control: InputPlugin.Control): ConfigDiff = {
    val task = config.loadConfig(classOf[PluginTask])
    val schema = task.getSchema.toSchema
    val taskCount = task.getThreads // number of run() method calls
    resume(task.dump, schema, taskCount, control)
  }

  override def resume(taskSource: TaskSource, schema: Schema, taskCount: Int, control: InputPlugin.Control): ConfigDiff = {
    control.run(taskSource, schema, taskCount)
    Exec.newConfigDiff
  }

  override def cleanup(taskSource: TaskSource, schema: Schema, taskCount: Int, successTaskReports: util.List[TaskReport]): Unit = {
  }

  override def run(taskSource: TaskSource, schema: Schema, taskIndex: Int, output: PageOutput): TaskReport = {
    val task = taskSource.loadTask(classOf[PluginTask])
    val rows = task.getRows.asInstanceOf[Integer]
    try {
      val pagebuilder = new PageBuilder(Exec.getBufferAllocator, schema, output)
      try {
        val rnd = new Random
        java.util.stream.IntStream.rangeClosed(taskIndex * rows + 1, taskIndex * rows + rows).boxed.forEach((rowNumber: Integer) => {
          def foo(rowNumber: Integer) = {
            var i = 0
            while ( {
              i < schema.size
            }) {
              val column = schema.getColumn(i)
              val `type` = column.getType
              `type`.getName match {
                case "long" =>
                  val pk = task.getPrimaryKey
                  if (column.getName == pk) pagebuilder.setLong(i, rowNumber)
                  else pagebuilder.setLong(i, rnd.nextInt(10000))
                  break //todo: break is not supported
                case "double" =>
                  pagebuilder.setDouble(i, rnd.nextDouble * 10000)
                  break //todo: break is not supported
                case "boolean" =>
                  if (Math.random < 0.5) pagebuilder.setBoolean(i, false)
                  else pagebuilder.setBoolean(i, true)
                  break //todo: break is not supported
                case "string" =>
                  pagebuilder.setString(i, generator.generate(32))
                  break //todo: break is not supported
                case "timestamp" =>
                  val zoneId = ZoneId.systemDefault
                  val randd = Math.random
                  val randomDate = LocalDateTime.now.plusDays((randd * 100).toLong).plusSeconds((randd * 1000000).toLong)
                  val timestamp = Timestamp.ofEpochSecond(randomDate.atZone(zoneId).toEpochSecond)
                  pagebuilder.setTimestamp(column, timestamp)
                  break //todo: break is not supported
                case _ =>
                  System.out.println("Unsupported type")
                  break //todo: break is not supported
              }
              {
                i += 1
                i - 1
              }
            }
            pagebuilder.addRecord()
          }

          foo(rowNumber)
        })
        pagebuilder.finish()
      } finally if (pagebuilder != null) pagebuilder.close()
    }
    val taskReport = Exec.newTaskReport
    taskReport.set("columns", schema.size)
    taskReport.set("rows", rows)
    taskReport
  }

  override def guess(config: ConfigSource): ConfigDiff = Exec.newConfigDiff
}