package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PropertyValueProcessorTest {

    private PropertyValueProcessor propertyValueProcessor;

    @Mock
    private BuilderConfiguration builderConfiguration;
    @Mock
    private ConfigBuilderFactory configBuilderFactory;
    @Mock
    private Properties properties;
    @Mock
    PropertyValue propertyValue;

    @Before
    public void setUp() throws Exception {
        propertyValueProcessor = new PropertyValueProcessor();
    }

    @Test
    public void testPropertyValueProcessor() {
        when(configBuilderFactory.getInstance(BuilderConfiguration.class)).thenReturn(builderConfiguration);
        when(builderConfiguration.getProperties()).thenReturn(properties);
        when(propertyValue.value()).thenReturn("test");
        when(properties.getProperty("test")).thenReturn("passed");
        assertEquals("passed", propertyValueProcessor.getValue(propertyValue, configBuilderFactory));
    }
}
