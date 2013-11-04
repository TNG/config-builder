package com.tngtech.configbuilder.annotation.valueextractor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ValueExtractorAnnotation(ImportedValueProcessor.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImportedValue {
    String value();
}
