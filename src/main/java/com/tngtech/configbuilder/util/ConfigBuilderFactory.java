package com.tngtech.configbuilder.util;

import com.google.common.collect.Maps;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFilesProcessor;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyExtensionProcessor;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocationsProcessor;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertySuffixProcessor;
import com.tngtech.configbuilder.annotation.typetransformer.*;
import com.tngtech.configbuilder.annotation.valueextractor.*;
import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.FactoryInstantiationException;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.util.Map;

public class ConfigBuilderFactory {

    private final Map<Class<?>, Object> singletonMap = Maps.newHashMap();

    public <T> void initialize() {

        //Order is IMPORTANT!

        //configuration
        singletonMap.put(ErrorMessageSetup.class, new ErrorMessageSetup());
        singletonMap.put(BuilderConfiguration.class, new BuilderConfiguration());

        //util
        singletonMap.put(AnnotationHelper.class, new AnnotationHelper());
        singletonMap.put(GenericsAndCastingHelper.class, new GenericsAndCastingHelper());
        singletonMap.put(FieldValueExtractor.class, new FieldValueExtractor(this));
        singletonMap.put(FieldValueTransformer.class, new FieldValueTransformer(this));
        singletonMap.put(PropertyLoaderConfigurator.class, new PropertyLoaderConfigurator(this));
        singletonMap.put(ConstructionHelper.class, new ConstructionHelper<T>(this));
        singletonMap.put(FieldSetter.class, new FieldSetter<T>(this));
        singletonMap.put(ConfigValidator.class, new ConfigValidator<T>(this));
        singletonMap.put(CommandLineHelper.class, new CommandLineHelper(this));

        //AnnotationProcessors
        singletonMap.put(SystemPropertyProcessor.class, new SystemPropertyProcessor());
        singletonMap.put(PropertyValueProcessor.class, new PropertyValueProcessor());
        singletonMap.put(EnvironmentVariableProcessor.class, new EnvironmentVariableProcessor());
        singletonMap.put(CommandLineValueProcessor.class, new CommandLineValueProcessor());
        singletonMap.put(PropertySuffixProcessor.class, new PropertySuffixProcessor());
        singletonMap.put(PropertyLocationsProcessor.class, new PropertyLocationsProcessor());
        singletonMap.put(PropertyExtensionProcessor.class, new PropertyExtensionProcessor());
        singletonMap.put(PropertiesFilesProcessor.class, new PropertiesFilesProcessor());
        singletonMap.put(DefaultValueProcessor.class, new DefaultValueProcessor());

        //TypeTransformers
        singletonMap.put(StringOrPrimitiveToPrimitiveTransformer.class, new StringOrPrimitiveToPrimitiveTransformer());
        singletonMap.put(CollectionToArrayListTransformer.class, new CollectionToArrayListTransformer());
        singletonMap.put(CollectionToHashSetTransformer.class, new CollectionToHashSetTransformer());
        singletonMap.put(CharacterSeparatedStringToStringListTransformer.class, new CharacterSeparatedStringToStringListTransformer());
        singletonMap.put(CharacterSeparatedStringToStringSetTransformer.class, new CharacterSeparatedStringToStringSetTransformer());
        singletonMap.put(StringCollectionToCommaSeparatedStringTransformer.class, new StringCollectionToCommaSeparatedStringTransformer());
        singletonMap.put(StringToPathTransformer.class, new StringToPathTransformer());

        //other
        singletonMap.put(ValidatorFactory.class, Validation.buildDefaultValidatorFactory());
    }

    public <K> K getInstance(Class<K> clazz) {
        if(singletonMap.get(clazz) != null) {
            return (K)singletonMap.get(clazz);
        }
        else {
            return createInstance(clazz);
        }
    }

    //TODO: exception message
    public <K> K createInstance(Class<K> clazz) {
        try {
            return clazz.newInstance();
        } catch (IllegalAccessException e) {
            ErrorMessageSetup errorMessageSetup = (ErrorMessageSetup)singletonMap.get(ErrorMessageSetup.class);
            throw new FactoryInstantiationException(errorMessageSetup.getErrorMessage(FactoryInstantiationException.class, clazz.toString()));
        } catch (InstantiationException e) {
            return createInstanceOfInnerClass(clazz);
        }
    }

    //TODO: exception message
    private <K> K createInstanceOfInnerClass(Class<K> clazz) {
        Class<?> superClass = clazz.getDeclaringClass();
        try {
            Object superInstance = superClass.newInstance();
            Constructor<K> constructor = clazz.getConstructor(superClass);
            return constructor.newInstance(superInstance);
        } catch (Exception e) {
            ErrorMessageSetup errorMessageSetup = (ErrorMessageSetup)singletonMap.get(ErrorMessageSetup.class);
            throw new FactoryInstantiationException(errorMessageSetup.getErrorMessage(FactoryInstantiationException.class, clazz.toString()));
        }
    }
}
