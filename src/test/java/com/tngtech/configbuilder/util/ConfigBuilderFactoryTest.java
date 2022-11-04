package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.FactoryInstantiationException;
import com.tngtech.propertyloader.PropertyLoader;
import org.apache.commons.cli.DefaultParser;
import org.junit.Before;
import org.junit.Test;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigBuilderFactoryTest {

    public class InnerClass {
        public class InnerInnerClass{}
    }
    public static class NestedClass {}
    public static class ClassWithoutDefaultConstructor {
        private ClassWithoutDefaultConstructor(){}
    }

    private ConfigBuilderFactory configBuilderFactory = new ConfigBuilderFactory();

    @Before
    public void initializeConfigBuilderFactory() {
        configBuilderFactory.initialize();
    }

    @Test
    public void testGetInstance() {
        assertThat(configBuilderFactory.getInstance(ErrorMessageSetup.class)).isInstanceOf(ErrorMessageSetup.class);
        assertThat(configBuilderFactory.getInstance(ValidatorFactory.class)).isInstanceOf(Validation.buildDefaultValidatorFactory().getClass());

        //test instantiation of arbitrary class
        assertThat(configBuilderFactory.getInstance(DefaultParser.class)).isInstanceOf(DefaultParser.class);

        //test instantiation of nested and inner classes
        assertThat(configBuilderFactory.getInstance(InnerClass.class)).isInstanceOf(InnerClass.class);
        assertThat(configBuilderFactory.getInstance(NestedClass.class)).isInstanceOf(NestedClass.class);
    }

    @Test
    public void testGetInstanceThrowsExceptionForInnerClass() {
        configBuilderFactory.getInstance(ErrorMessageSetup.class).initialize("errors", new PropertyLoader());
        assertThatThrownBy(() -> configBuilderFactory.getInstance(InnerClass.InnerInnerClass.class))
                .isInstanceOf(FactoryInstantiationException.class);
    }

    @Test
    public void testGetInstanceThrowsExceptionForClassWithoutDefaultConstructor() {
        configBuilderFactory.getInstance(ErrorMessageSetup.class).initialize("errors", new PropertyLoader());
        assertThatThrownBy(() -> configBuilderFactory.getInstance(ClassWithoutDefaultConstructor.class))
                .isInstanceOf(FactoryInstantiationException.class);
    }

    @Test
    public void testCreateInstance() {
        assertThat(configBuilderFactory.createInstance(DefaultParser.class)).isInstanceOf(DefaultParser.class);
    }

    @Test
    public void testCreateInstanceThrowsException() {
        assertThatThrownBy(() -> configBuilderFactory.createInstance(List.class))
                .isInstanceOf(RuntimeException.class);
    }
}
