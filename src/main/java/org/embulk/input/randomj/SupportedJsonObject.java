package org.embulk.input.randomj;

public enum SupportedJsonObject
{
    OBJECT("object"),
    STRING("string"),
    BOOLEAN("boolean"),
    INTEGER("integer"),
    NUMBER("number"),
    ARRAY("array");

    private final String typeName;

    private SupportedJsonObject(final String typeName)
    {
        this.typeName = typeName;
    }
}
