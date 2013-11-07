package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.ImportedConfigurationException;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class ImportedValueProcessor implements IValueExtractorProcessor {

    @Override
    public Object getValue(Annotation annotation, ConfigBuilderFactory configBuilderFactory) {
        BuilderConfiguration builderConfiguration = configBuilderFactory.getInstance(BuilderConfiguration.class);
        Object importedConfiguration = builderConfiguration.getImportedConfiguration();
        
        String fieldName = ((ImportedValue) annotation).value();
        Object result;

        try {
            Field field = importedConfiguration.getClass().getDeclaredField(fieldName);
            result = field.get(importedConfiguration);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            ErrorMessageSetup errorMessageSetup = configBuilderFactory.getInstance(ErrorMessageSetup.class);
            throw new ImportedConfigurationException(errorMessageSetup.getErrorMessage(ImportedConfigurationException.class, fieldName));
        }

        return result;
    }
}
