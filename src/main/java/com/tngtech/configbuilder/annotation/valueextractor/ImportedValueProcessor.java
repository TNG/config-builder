package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.configuration.BuilderConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ImportedValueProcessor implements IValueExtractorProcessor {

    @Override
    public Object getValue(Annotation annotation, BuilderConfiguration argument, Field field) {
        field.get(argument.getImportedConfiguration());
        
        return argument.getImportedConfiguration();
    }
}
