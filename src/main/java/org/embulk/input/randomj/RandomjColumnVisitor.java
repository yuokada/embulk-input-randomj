package org.embulk.input.randomj;

import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.embulk.input.randomj.RandomjInputPlugin.PluginTask;
import org.embulk.spi.Column;
import org.embulk.spi.ColumnVisitor;
import org.embulk.spi.PageBuilder;
import org.embulk.spi.time.Timestamp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Random;

public class RandomjColumnVisitor
        implements ColumnVisitor
{
    private final PageBuilder pageBuilder;
    private final PluginTask task;
    private final Integer row;
    private final Random rnd;
    private final RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
            .build();
    private final ZoneId zoneId = ZoneId.systemDefault();

    public RandomjColumnVisitor(PageBuilder pageBuilder, PluginTask task, Integer row)
    {
        this.task = task;
        this.pageBuilder = pageBuilder;
        this.row = row;
        this.rnd = new Random();
    }

    @Override
    public void booleanColumn(Column column)
    {
        if (Math.random() < 0.5) {
            pageBuilder.setBoolean(column, false);
        }
        else {
            pageBuilder.setBoolean(column, true);
        }
    }

    @Override
    public void longColumn(Column column)
    {
        final String pk = task.getPrimaryKey();
        if (column.getName().equals(pk)) {
            pageBuilder.setLong(column, row);
        }
        else {
            pageBuilder.setLong(column, rnd.nextInt(10000));
        }
    }

    @Override
    public void doubleColumn(Column column)
    {
        pageBuilder.setDouble(column, rnd.nextDouble() * 10000);
    }

    @Override
    public void stringColumn(Column column)
    {
        pageBuilder.setString(column, generator.generate(32));
    }

    @Override
    public void timestampColumn(Column column)
    {
        final double randd = Math.random();
        LocalDateTime randomDate = LocalDateTime.now()
                .plusDays((long) (randd * 100))
                .plusSeconds((long) (randd * 1000000));
        Timestamp timestamp = Timestamp.ofEpochSecond(
                randomDate.atZone(zoneId).toEpochSecond()
        );
        pageBuilder.setTimestamp(column, timestamp);
    }

    @Override
    public void jsonColumn(Column column)
    {
        throw new UnsupportedOperationException("orc output plugin does not support json type");
    }
}
