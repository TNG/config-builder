package com.tngtech.test.customannotation;

import com.tngtech.configbuilder.annotation.valueextractor.ValueExtractorAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ValueExtractorAnnotation(FileValueProcessor.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileContentValue {
    String value();
    String encoding() default "UTF-8";
}
