package com.tngtech.test.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class ColorSerializer extends com.fasterxml.jackson.databind.JsonSerializer<Color> {
    @Override
    public void serialize(Color color, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        String colorText = color.toString();
        jsonGenerator.writeString(colorText);
    }
}