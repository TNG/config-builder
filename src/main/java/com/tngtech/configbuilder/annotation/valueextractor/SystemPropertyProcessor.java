package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.util.ConfigBuilderFactory;

import java.lang.annotation.Annotation;

/**
 * Processes SystemPropertyValue annotations, implements IValueExtractorProcessor
 */
public class SystemPropertyProcessor implements IValueExtractorProcessor {
    public String getValue(Annotation annotation, ConfigBuilderFactory configBuilderFactory) {
        return System.getProperty(((SystemPropertyValue) annotation).value());
    }
}
