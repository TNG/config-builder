package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.annotation.valueextractor.IValueExtractorProcessor;
import com.tngtech.configbuilder.annotation.valueextractor.ValueExtractorAnnotation;
import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class FieldValueExtractor {

    private final static Logger log = Logger.getLogger(FieldValueExtractor.class);

    private final AnnotationHelper annotationHelper;
    private final ConfigBuilderFactory configBuilderFactory;

    public FieldValueExtractor(ConfigBuilderFactory configBuilderFactory) {
        this.configBuilderFactory = configBuilderFactory;
        this.annotationHelper = configBuilderFactory.getInstance(AnnotationHelper.class);
    }


    public Object extractValue(Field field, BuilderConfiguration builderConfiguration) {
        Object value = null;
        Class<? extends Annotation>[] annotationOrderOfField = field.isAnnotationPresent(LoadingOrder.class) ? field.getAnnotation(LoadingOrder.class).value() : builderConfiguration.getAnnotationOrder();
        Class<? extends IValueExtractorProcessor> processor;
       
        for (Annotation annotation : annotationHelper.getAnnotationsInOrder(field, annotationOrderOfField)) {
            log.debug(String.format("trying to find a value for field %s with %s annotation", field.getName(), annotation.annotationType()));
            processor = annotation.annotationType().getAnnotation(ValueExtractorAnnotation.class).value();
            value = configBuilderFactory.getInstance(processor).getValue(annotation, configBuilderFactory);
            if (value != null) {
                log.debug(String.format("found value \"%s\" for field %s from %s annotation", value, field.getName(), annotation.annotationType()));
                break;
            }
        }
        return value;
    }
}