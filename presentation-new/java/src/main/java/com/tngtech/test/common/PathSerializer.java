package com.tngtech.test.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.nio.file.Path;

public class PathSerializer extends com.fasterxml.jackson.databind.JsonSerializer<java.nio.file.Path> {
    @Override
    public void serialize(Path path, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        String pathText = path.toString();
        jsonGenerator.writeString(pathText);
    }
}