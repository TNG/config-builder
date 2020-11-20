package com.tngtech.configbuilder.util;

import com.google.common.collect.Lists;
import com.tngtech.configbuilder.annotation.configuration.Separator;
import com.tngtech.configbuilder.annotation.typetransformer.*;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.TypeTransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

//TODO: Content transformers (transform even if types already match, allow null as argument)
public class FieldValueTransformer {

    private final static Logger log = LoggerFactory.getLogger(FieldValueTransformer.class);

    private final ConfigBuilderFactory configBuilderFactory;
    private final ErrorMessageSetup errorMessageSetup;
    private final GenericsAndCastingHelper genericsAndCastingHelper;
    private Object[] additionalOptions;

    //Order is important: Prefer List over Set if both apply!
    private final ArrayList<Class<? extends TypeTransformer>> defaultTransformers = Lists.newArrayList(
            StringOrPrimitiveToPrimitiveTransformer.class,
            CharacterSeparatedStringToStringListTransformer.class,
            CharacterSeparatedStringToStringSetTransformer.class,
            CollectionToArrayListTransformer.class,
            CollectionToHashSetTransformer.class,
            StringCollectionToCommaSeparatedStringTransformer.class,
            StringToPathTransformer.class);

    private ArrayList<TypeTransformer> availableTransformers = Lists.newArrayList();

    public FieldValueTransformer(ConfigBuilderFactory configBuilderFactory) {
        this.configBuilderFactory = configBuilderFactory;
        this.errorMessageSetup = configBuilderFactory.getInstance(ErrorMessageSetup.class);
        this.genericsAndCastingHelper = configBuilderFactory.getInstance(GenericsAndCastingHelper.class);
    }

    public Object transformFieldValue(Field field, Object sourceValue) {
        initialize(field);
        sourceValue = performNecessaryTransformations(sourceValue, field.getGenericType());
        reset();
        return sourceValue;
    }

    private void initialize(Field field) {
        for(Class<? extends TypeTransformer> transformerClass : getAllTransformers(field)) {
            availableTransformers.add(configBuilderFactory.getInstance(transformerClass));
        }
        genericsAndCastingHelper.getEnumTypeOrParameterIfApplicable(field)
                .map(StringToEnumTypeTransformer::new)
                .ifPresent(availableTransformers::add);

        additionalOptions = field.isAnnotationPresent(Separator.class)? new Object[]{field.getAnnotation(Separator.class).value()} : new Object[]{","};
    }

    private ArrayList<Class<? extends TypeTransformer>> getAllTransformers(Field field) {
        ArrayList<Class<? extends TypeTransformer>> allTransformers = getUserSuggestedTransformers(field);
        allTransformers.addAll(defaultTransformers);
        return  allTransformers;
    }

    private ArrayList<Class<? extends TypeTransformer>> getUserSuggestedTransformers(Field field) {
        if(field.isAnnotationPresent(TypeTransformers.class)) {
            TypeTransformers annotation =  field.getAnnotation(TypeTransformers.class);
            return Lists.newArrayList(annotation.value());
        } else {
            return Lists.newArrayList();
        }
    }

    public Object performNecessaryTransformations(Object sourceValue, Type targetType) {
        if(genericsAndCastingHelper.typesMatch(sourceValue, targetType)) {
            return sourceValue;
        }
        Class<?> sourceClass = genericsAndCastingHelper.getWrapperClassIfPrimitive(sourceValue.getClass());
        Class<?> targetClass = genericsAndCastingHelper.castTypeToClass(targetType);

        log.debug("Searching for a transformer from {} to {}", sourceClass.getSimpleName(), targetClass.getSimpleName());

        TypeTransformer<Object, ?> transformer = findApplicableTransformer(sourceClass, targetType);
        sourceValue = transformer.transform(sourceValue);
        return performNecessaryTransformations(sourceValue, targetType);
    }

    private TypeTransformer findApplicableTransformer(Class<?> sourceClass, Type targetType) {
        Class<?> targetClass = genericsAndCastingHelper.getWrapperClassIfPrimitive(genericsAndCastingHelper.castTypeToClass(targetType));
        for(TypeTransformer<?,?> transformer: availableTransformers) {
            transformer.initialize(this, configBuilderFactory, additionalOptions);
            if(transformer.isMatching(sourceClass, targetClass)) {
                transformer.setTargetType(targetType);
                return transformer;
            }
        }
        throw new TypeTransformerException(errorMessageSetup.getErrorMessage(TypeTransformerException.class, sourceClass.toString(), targetType.toString()));
    }

    private void reset() {
        availableTransformers = Lists.newArrayList();
    }
}
