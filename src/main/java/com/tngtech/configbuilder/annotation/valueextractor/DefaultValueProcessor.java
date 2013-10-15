package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.configuration.BuilderConfiguration;

import java.lang.annotation.Annotation;

public class DefaultValueProcessor implements IValueExtractorProcessor {

    public String getValue(Annotation annotation, BuilderConfiguration builderConfiguration) {
        return ((DefaultValue) annotation).value();
    }
}
