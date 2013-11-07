package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;

import java.lang.annotation.Annotation;

/**
 * Processes PropertyValue annotations, implements IValueExtractorProcessor
 */
public class PropertyValueProcessor implements IValueExtractorProcessor {

    public String getValue(Annotation annotation, ConfigBuilderFactory configBuilderFactory) {
        BuilderConfiguration builderConfiguration = configBuilderFactory.getInstance(BuilderConfiguration.class);
        
        return builderConfiguration.getProperties().getProperty(((PropertyValue) annotation).value());
    }
}
