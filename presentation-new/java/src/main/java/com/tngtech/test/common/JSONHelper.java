package com.tngtech.test.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.nio.file.Path;

public class JSONHelper {
    @SuppressWarnings("deprecation")
    public static void printJSON(Object config) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));

        simpleModule.addSerializer(Path.class, new PathSerializer());
        objectMapper.registerModule(simpleModule);

        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(config));
    }
}