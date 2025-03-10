package io.github.yuokada.embulk.input.randomj.visitor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.github.yuokada.embulk.input.randomj.PluginTask;
import io.github.yuokada.embulk.input.randomj.SupportedJsonObject;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;
import org.embulk.spi.Column;
import org.embulk.spi.ColumnVisitor;
import org.embulk.spi.PageBuilder;
import org.embulk.util.json.JsonValueParser;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RandomjColumnVisitor
        implements ColumnVisitor
{
    private final PageBuilder pageBuilder;
    private final PluginTask task;
    private final int row;
    private final Random rnd;
    private final Map<Column, Map<String, Integer>> columnOptions;
    private final Map<Column, List<JsonNode>> schemaOptions;
    private final RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
            .build();
    private final ZoneId zoneId = ZoneId.systemDefault();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyyMMdd")
            .withResolverStyle(ResolverStyle.LENIENT);
    private static final long cacheSize = 64;
    private static final Cache<String, ZonedDateTime> zonedDateTimeCache = CacheBuilder
            .newBuilder()
            .maximumSize(cacheSize)
            .build();
    private static final Cache<String, Long> durationCache = CacheBuilder
            .newBuilder()
            .maximumSize(cacheSize)
            .build();

    private static final String NULL_RATE = "null_rate";

    public RandomjColumnVisitor(PageBuilder pageBuilder, PluginTask task, int row,
            Map<Column, Map<String, Integer>> columnOptions,
            Map<Column, List<JsonNode>> schemaOptions)
    {
        this.task = task;
        this.pageBuilder = pageBuilder;
        this.row = row;
        this.columnOptions = columnOptions;
        this.rnd = new Random();
        this.schemaOptions = schemaOptions;
    }

    @Override
    public void booleanColumn(Column column)
    {
        Integer nullRate = columnOptions.get(column).get(NULL_RATE);
        if (Math.random() < (double) nullRate / 10000) {
            pageBuilder.setNull(column);
        }
        else {
            if (Math.random() < 0.5) {
                pageBuilder.setBoolean(column, false);
            }
            else {
                pageBuilder.setBoolean(column, true);
            }
        }
    }

    @Override
    public void longColumn(Column column)
    {
        final String pk = task.getPrimaryKey();
        if (column.getName().equals(pk)) {
            Integer pki = columnOptions.get(column).get("pk_offset");
            pageBuilder.setLong(column, pki == null ? row : row + pki);
        }
        else {
            Integer nullRate = columnOptions.get(column).get(NULL_RATE);
            if (Math.random() < (double) nullRate / 10000) {
                pageBuilder.setNull(column);
            }
            else {
                Integer max = columnOptions.get(column).get("max_value");
                Integer min = columnOptions.get(column).get("min_value");
                if (max != null) {
                    if (min != null) {
                        int s = min + rnd.nextInt((max - min));
                        pageBuilder.setLong(column, s);
                    }
                    else {
                        pageBuilder.setLong(column, rnd.nextInt(max));
                    }
                }
                else {
                    pageBuilder.setLong(column, rnd.nextInt(10000));
                }
            }
        }
    }

    @Override
    public void doubleColumn(Column column)
    {
        Integer nullRate = columnOptions.get(column).get(NULL_RATE);
        if (Math.random() < (double) nullRate / 10000) {
            pageBuilder.setNull(column);
        }
        else {
            Integer max = columnOptions.get(column).get("max_value");
            Integer min = columnOptions.get(column).get("min_value");
            if (max != null) {
                if (min != null) {
                    double d = min + rnd.nextInt((max - min) - 1) + rnd.nextDouble();
                    pageBuilder.setDouble(column, d);
                }
                else {
                    double d = rnd.nextInt(max - 1) + rnd.nextDouble();
                    pageBuilder.setDouble(column, d);
                }
            }
            else {
                pageBuilder.setDouble(column, rnd.nextDouble() * 10000);
            }
        }
    }

    @Override
    public void stringColumn(Column column)
    {
        Integer nullRate = columnOptions.get(column).get(NULL_RATE);
        if (Math.random() < (double) nullRate / 10000) {
            pageBuilder.setNull(column);
        }
        else {
            final Integer length = columnOptions.get(column).getOrDefault("length", 0);
            if (length == 0) {
                pageBuilder.setString(column, generator.generate(32));
            }
            else {
                pageBuilder.setString(column, generator.generate(length));
            }
        }
    }

    @Override
    public void timestampColumn(Column column)
    {
        Integer nullRate = columnOptions.get(column).get(NULL_RATE);
        if (Math.random() < (double) nullRate / 10000) {
            pageBuilder.setNull(column);
        }
        else {
            ZonedDateTime start = getZonedDatetime(column, "start_date");
            ZonedDateTime end = getZonedDatetime(column, "end_date");
            long duration = getDuration(column, start, end);
            if (duration != 0) {
                int plus = rnd.nextInt((int) duration);
                Instant instant = Instant.ofEpochSecond(
                        start.plusSeconds(plus).toEpochSecond()
                );
                pageBuilder.setTimestamp(column, instant);
            }
            else {
                double randDouble = Math.random();
                LocalDateTime randomDate = LocalDateTime.now()
                        .plusDays((long) (randDouble * 100))
                        .plusSeconds((long) (randDouble * 1000000));
                Instant instant = Instant.ofEpochSecond(
                        randomDate.atZone(zoneId).toEpochSecond()
                );
                pageBuilder.setTimestamp(column, instant);
            }
        }
    }

    private ZonedDateTime getZonedDatetime(Column column, String dateString)
    {
        String cacheKey = String.format("%s::%s", column.getName(), dateString);
        ZonedDateTime start = zonedDateTimeCache.getIfPresent(cacheKey);
        if (start == null) {
            Integer startDate = columnOptions.get(column).getOrDefault(dateString, null);
            if (startDate == null) {
                start = LocalDate.now().atStartOfDay(zoneId);
            }
            else {
                start = LocalDate.parse(startDate.toString(), formatter)
                        .atStartOfDay(zoneId);
            }
            zonedDateTimeCache.put(cacheKey, start);
        }

        return start;
    }

    private long getDuration(Column column, ZonedDateTime start, ZonedDateTime end)
    {
        Long duration = durationCache.getIfPresent(column.getName());
        if (duration == null) {
            duration = Duration.between(start, end).getSeconds();
            durationCache.put(column.getName(), duration);
        }
        return duration;
    }

    @Override
    public void jsonColumn(Column column)
    {
        Map<String, Object> map = new HashMap<>();
        JsonColumnVisitor visitor = new JsonColumnVisitor(map);

        List<JsonNode> nodes = schemaOptions.get(column);
        for (JsonNode node : nodes) {
            visit(node, visitor);
        }

        try {
            pageBuilder.setJson(column, JsonValueParser.builder().build(mapper.writeValueAsString(map)).readJsonValue());
        }
        catch (JsonProcessingException e) {
            e.printStackTrace(); // NOSONAR
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void visit(JsonNode node, JsonColumnVisitor visitor)
    {
        SupportedJsonObject object = SupportedJsonObject
                .valueOf(node.get("type").asText().toUpperCase());
        if (object.equals(SupportedJsonObject.BOOLEAN)) {
            visitor.booleanNode(node);
        }
        else if (object.equals(SupportedJsonObject.NUMBER)) {
            visitor.doubleNode(node);
        }
        else if (object.equals(SupportedJsonObject.INTEGER)) {
            visitor.integerNode(node);
        }
        else if (object.equals(SupportedJsonObject.STRING)) {
            visitor.stringNode(node);
        }
        else if (object.equals(SupportedJsonObject.ARRAY)) {
            visitor.arrayNode(node);
        }
        else if (object.equals(SupportedJsonObject.OBJECT)) {
            visitor.objectNode(node);
        }
        else {
            throw new UnsupportedOperationException(
                    "randomj input plugin does not support json-data type");
        }
    }
}
