package com.tngtech.configbuilder.annotation.valueextractor;

import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.ImportedConfigurationException;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImportedValueProcessorTest {
    
    private class ImportedTestConfig {
        int intField = 23;
        
        String stringField = "Foo";
    }
    
    @Mock
    ConfigBuilderFactory configBuilderFactory;
    @Mock
    BuilderConfiguration builderConfiguration;
    @Mock
    ErrorMessageSetup errorMessageSetup;
    @Mock
    ImportedValue importedValue;

    ImportedTestConfig importedTestConfig;
    
    private ImportedValueProcessor importedValueProcessor;
    
    @Before
    public void setUp() {
        importedValueProcessor = new ImportedValueProcessor();
        importedTestConfig = new ImportedTestConfig();
    }
    
    @Test
    public void testGetIntegerValue() {
        when(configBuilderFactory.getInstance(BuilderConfiguration.class)).thenReturn(builderConfiguration);
        when(builderConfiguration.getImportedConfiguration()).thenReturn(importedTestConfig);
        when(importedValue.value()).thenReturn("intField");
        
        int actualResult = (int) importedValueProcessor.getValue(importedValue, configBuilderFactory);
        assertThat(actualResult, equalTo(23));
    }
    
    @Test
    public void testGetStringValue() {
        when(configBuilderFactory.getInstance(BuilderConfiguration.class)).thenReturn(builderConfiguration);
        when(builderConfiguration.getImportedConfiguration()).thenReturn(importedTestConfig);
        when(importedValue.value()).thenReturn("stringField");

        String actualResult = (String) importedValueProcessor.getValue(importedValue, configBuilderFactory);
        assertThat(actualResult, equalTo("Foo"));
    }
    
    @Test(expected = ImportedConfigurationException.class)
    public void testExceptionIfFieldNotPresent() {
        when(configBuilderFactory.getInstance(BuilderConfiguration.class)).thenReturn(builderConfiguration);
        when(builderConfiguration.getImportedConfiguration()).thenReturn(importedTestConfig);
        when(importedValue.value()).thenReturn("notAField");
        when(configBuilderFactory.getInstance(ErrorMessageSetup.class)).thenReturn(errorMessageSetup);
        when(errorMessageSetup.getErrorMessage(ImportedConfigurationException.class)).thenReturn("Just a message");

        importedValueProcessor.getValue(importedValue, configBuilderFactory);
    }
    
}
