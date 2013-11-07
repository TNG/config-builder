package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.util.ConfigBuilderFactory;

import java.lang.annotation.Annotation;

/**
 * Processes EnvironmentVariableValue annotations, implements IValueExtractorProcessor
 */
public class EnvironmentVariableProcessor implements IValueExtractorProcessor {
    public String getValue(Annotation annotation, ConfigBuilderFactory configBuilderFactory) {
        return System.getenv(((EnvironmentVariableValue) annotation).value());
    }
}
