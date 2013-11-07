package com.tngtech.configbuilder.util;

import com.google.common.collect.Lists;
import com.tngtech.configbuilder.annotation.configuration.CollectionType;
import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.annotation.valueextractor.*;
import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FieldValueExtractorTest {

    private static class TestConfig {

        @PropertyValue("testField")
        @CommandLineValue(shortOpt = "t", longOpt = "testField")
        private Collection<String> testField;

        @LoadingOrder({CommandLineValue.class, PropertyValue.class})
        @PropertyValue("testFieldWithLoadingOrder")
        @CommandLineValue(shortOpt = "t", longOpt = "testFieldWithLoadingOrder")
        private Collection<String> testFieldWithLoadingOrder;

        @CollectionType
        @DefaultValue("value1,value2")
        private Collection<String> collectionField;

        @DefaultValue("3")
        private int primitiveField;

        @DefaultValue("3")
        private boolean booleanField;
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ConfigBuilderFactory configBuilderFactory;
    @Mock
    private BuilderConfiguration builderConfiguration;
    @Mock
    private AnnotationHelper annotationHelper;
    @Mock
    private PropertyValueProcessor propertyValueProcessor;
    @Mock
    private CommandLineValueProcessor commandLineValueProcessor;
    @Mock
    private DefaultValueProcessor defaultValueProcessor;

    private FieldValueExtractor fieldValueExtractor;
    private Field field;
    Class<? extends Annotation>[] order = new Class[]{PropertyValue.class, CommandLineValue.class};


    @Before
    public void setUp() throws Exception {
        when(builderConfiguration.getAnnotationOrder()).thenReturn(order);

        when(configBuilderFactory.getInstance(AnnotationHelper.class)).thenReturn(annotationHelper);
        when(configBuilderFactory.getInstance(PropertyValueProcessor.class)).thenReturn(propertyValueProcessor);
        when(configBuilderFactory.getInstance(CommandLineValueProcessor.class)).thenReturn(commandLineValueProcessor);
        when(configBuilderFactory.getInstance(DefaultValueProcessor.class)).thenReturn(defaultValueProcessor);

        when(propertyValueProcessor.getValue(Matchers.any(PropertyValue.class), Matchers.any(ConfigBuilderFactory.class))).thenReturn("propertyValue");
        when(commandLineValueProcessor.getValue(Matchers.any(CommandLineValue.class), Matchers.any(ConfigBuilderFactory.class))).thenReturn("commandLineValue");   
        
        fieldValueExtractor = new FieldValueExtractor(configBuilderFactory);
    }

    @Test
    public void testExtractValue() throws Exception {
        field = TestConfig.class.getDeclaredField("testField");

        PropertyValue propertyValue = field.getAnnotation(PropertyValue.class);
        CommandLineValue commandLineValue = field.getAnnotation(CommandLineValue.class);

        List<Annotation> orderList = Lists.newArrayList(propertyValue, commandLineValue);
        when(annotationHelper.getAnnotationsInOrder(Matchers.any(Field.class), Matchers.any(Class[].class))).thenReturn(orderList);

        String result = (String) fieldValueExtractor.extractValue(field, builderConfiguration);
        verify(annotationHelper).getAnnotationsInOrder(field, order);
        assertEquals("propertyValue", result);
    }

    @Test
    public void testExtractValueWithLoadingOrder() throws Exception {
        field = TestConfig.class.getDeclaredField("testFieldWithLoadingOrder");

        PropertyValue propertyValue = field.getAnnotation(PropertyValue.class);
        CommandLineValue commandLineValue = field.getAnnotation(CommandLineValue.class);

        List<Annotation> orderList = Lists.newArrayList(commandLineValue, propertyValue);
        when(annotationHelper.getAnnotationsInOrder(Matchers.any(Field.class), Matchers.any(Class[].class))).thenReturn(orderList);

        order = new Class[]{CommandLineValue.class, PropertyValue.class};

        String result = (String) fieldValueExtractor.extractValue(field, builderConfiguration);
        verify(annotationHelper).getAnnotationsInOrder(field, order);
        assertEquals("commandLineValue", result);
    }

    @Test
    public void testExtractValueWithNullValue() throws Exception {
        field = TestConfig.class.getDeclaredField("testField");

        PropertyValue propertyValue = field.getAnnotation(PropertyValue.class);
        CommandLineValue commandLineValue = field.getAnnotation(CommandLineValue.class);
        List<Annotation> orderList = Lists.newArrayList(propertyValue, commandLineValue);
        when(annotationHelper.getAnnotationsInOrder(Matchers.any(Field.class), Matchers.any(Class[].class))).thenReturn(orderList);

        when(propertyValueProcessor.getValue(Matchers.any(PropertyValue.class), Matchers.any(ConfigBuilderFactory.class))).thenReturn(null);
        when(commandLineValueProcessor.getValue(Matchers.any(CommandLineValue.class), Matchers.any(ConfigBuilderFactory.class))).thenReturn(null);

        Object result = fieldValueExtractor.extractValue(field, builderConfiguration);
        assertEquals(null, result);
    }
}
