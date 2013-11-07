package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.util.ConfigBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultValueProcessorTest {

    private DefaultValueProcessor defaultValueProcessor;

    @Mock
    private ConfigBuilderFactory configBuilderFactory;
    @Mock
    private DefaultValue defaultValue;

    @Before
    public void setUp() throws Exception {
        defaultValueProcessor = new DefaultValueProcessor();
    }

    @Test
    public void testDefaultValueProcessor() {

        when(defaultValue.value()).thenReturn("value");
        assertEquals("value", defaultValueProcessor.getValue(defaultValue, configBuilderFactory).toString());
    }
}
