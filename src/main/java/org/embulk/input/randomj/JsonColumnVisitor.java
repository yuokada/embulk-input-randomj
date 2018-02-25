package org.embulk.input.randomj;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.text.CharacterPredicates;
import org.apache.commons.text.RandomStringGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JsonColumnVisitor
{
    private final Map<String, Object> map;
    private final Random rnd = new Random();
    private final RandomStringGenerator generator = new RandomStringGenerator.Builder()
            .withinRange('0', 'z')
            .filteredBy(CharacterPredicates.LETTERS, CharacterPredicates.DIGITS)
            .build();

    private static final String ITEMS = "items";

    public JsonColumnVisitor(Map<String, Object> gmap)
    {
        map = gmap;
    }

    public void booleanNode(JsonNode node)
    {
        String key = node.get("name").asText();
        if (Math.random() < 0.5) {
            map.put(key, true);
        }
        else {
            map.put(key, false);
        }
    }

    public void doubleNode(JsonNode node)
    {
        String key = node.get("name").asText();
        map.put(key, rnd.nextDouble() * 10000);
    }

    public void integerNode(JsonNode node)
    {
        String key = node.get("name").asText();
        map.put(key, rnd.nextInt(10000));
    }

    public void stringNode(JsonNode node)
    {
        String key = node.get("name").asText();
        map.put(key, generator.generate(8));
    }

    public void arrayNode(JsonNode node) // NOSONAR
    {
        String key = node.get("name").asText();
        String dataType = node.get(ITEMS).get("type").asText();
        int arraySize = node.get(ITEMS).get("size").asInt(1);
        SupportedJsonObject jtype = SupportedJsonObject.valueOf(dataType.toUpperCase());
        switch (jtype) {
            case BOOLEAN: { // NOSONAR
                List<Boolean> m = new ArrayList<>();
                for (int i = 0; i < arraySize; i++) {
                    if (Math.random() < 0.5) {
                        m.add(true);
                    }
                    else {
                        m.add(false);
                    }
                }
                map.put(key, m);
                break;
            }
            case INTEGER: { // NOSONAR
                ArrayList<Integer> m = new ArrayList<>();
                for (int i = 0; i < arraySize; i++) {
                    m.add(rnd.nextInt(100));
                }
                map.put(key, m);
                break;
            }
            case NUMBER: { // NOSONAR
                ArrayList<Number> m = new ArrayList<>();
                for (int i = 0; i < arraySize; i++) {
                    m.add(rnd.nextDouble() * 100);
                }
                map.put(key, m);
                break;
            }
            case STRING: { // NOSONAR
                int length = 8;
                ArrayList<String> m = new ArrayList<>();
                for (int i = 0; i < arraySize; i++) {
                    m.add(generator.generate(length));
                }
                map.put(key, m);
                break;
            }
            default:
                throw new UnsupportedOperationException("randomj input plugin does not support json-array-data type");
        }
    }

    public void objectNode(JsonNode node)
    {
        Map<String, Object> objectMap = new HashMap<>();

        for (JsonNode jsonNode : node.findValues(ITEMS).listIterator().next()) {
            String nestKey = jsonNode.get("name").asText();
            SupportedJsonObject jtype = SupportedJsonObject.valueOf(jsonNode.get("type").asText().toUpperCase());

            switch (jtype) {
                case BOOLEAN:
                    if (Math.random() < 0.5) {
                        objectMap.put(nestKey, true);
                    }
                    else {
                        objectMap.put(nestKey, false);
                    }
                    break;
                case NUMBER:
                    objectMap.put(nestKey, rnd.nextDouble() * 100);
                    break;
                case INTEGER:
                    objectMap.put(nestKey, rnd.nextInt(10000));
                    break;
                case STRING:
                    int length = 8;
                    objectMap.put(nestKey, generator.generate(length));
                    break;
                default:
                    throw new UnsupportedOperationException("randomj input plugin does not support json-data type");
            }
        }
        map.put(node.get("name").asText(), objectMap);
    }
}
