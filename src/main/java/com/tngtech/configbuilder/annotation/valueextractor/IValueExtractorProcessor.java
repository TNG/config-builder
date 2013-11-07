package com.tngtech.configbuilder.annotation.valueextractor;


import com.tngtech.configbuilder.util.ConfigBuilderFactory;

import java.lang.annotation.Annotation;

/**
 * This interface is implemented by annotation processors that get String values from annotations.
 */
public interface IValueExtractorProcessor {
    public Object getValue(Annotation annotation, ConfigBuilderFactory configBuilderFactory);
}
