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
public class SystemPropertyProcessorTest {

    private SystemPropertyProcessor systemPropertyProcessor;

    @Mock
    private SystemPropertyValue systemPropertyValue;

    @Mock
    private ConfigBuilderFactory configBuilderFactory;

    @Before
    public void setUp() throws Exception {
        systemPropertyProcessor = new SystemPropertyProcessor();
        when(systemPropertyValue.value()).thenReturn("user.language");
    }

    @Test
    public void testGetValue() throws Exception {
        assertEquals(System.getProperty("user.language"), systemPropertyProcessor.getValue(systemPropertyValue, configBuilderFactory).toString());
    }
}
