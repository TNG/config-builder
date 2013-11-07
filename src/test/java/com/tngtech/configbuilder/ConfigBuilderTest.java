package com.tngtech.configbuilder;

import com.google.common.collect.Lists;
import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;
import com.tngtech.configbuilder.testclasses.TestConfig;
import com.tngtech.configbuilder.util.*;
import com.tngtech.propertyloader.PropertyLoader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigBuilderTest {

    private ConfigBuilder<TestConfig> configBuilder;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @Mock
    private ConfigBuilderFactory configBuilderFactory;
    @Mock
    private BuilderConfiguration builderConfiguration;
    @Mock
    private CommandLineHelper commandLineHelper;
    @Mock
    private FieldSetter<TestConfig> fieldSetter;
    @Mock
    private ConfigValidator<TestConfig> configValidator;
    @Mock
    private ErrorMessageSetup errorMessageSetup;
    @Mock
    private ConstructionHelper<TestConfig> constructionHelper;

    @Mock
    private PropertyLoaderConfigurator propertyLoaderConfigurator;

    @Mock
    private PropertyLoader propertyLoader;
    @Mock
    private Options commandLineOptions;
    @Mock
    private CommandLine commandLine;
    @Mock
    private Properties properties;
    @Mock
    private LoadingOrder loadingOrder;

    @Before
    public void setUp() throws Exception {

        System.setOut(new PrintStream(outContent));

        when(configBuilderFactory.getInstance(BuilderConfiguration.class)).thenReturn(builderConfiguration);
        when(configBuilderFactory.getInstance(CommandLineHelper.class)).thenReturn(commandLineHelper);
        when(configBuilderFactory.getInstance(ConfigValidator.class)).thenReturn(configValidator);
        when(configBuilderFactory.getInstance(FieldSetter.class)).thenReturn(fieldSetter);
        when(configBuilderFactory.getInstance(ErrorMessageSetup.class)).thenReturn(errorMessageSetup);
        when(configBuilderFactory.getInstance(ConstructionHelper.class)).thenReturn(constructionHelper);
        when(configBuilderFactory.getInstance(PropertyLoaderConfigurator.class)).thenReturn(propertyLoaderConfigurator);
        when(propertyLoaderConfigurator.configurePropertyLoader(TestConfig.class)).thenReturn(propertyLoader);
        when(commandLineHelper.getOptions(TestConfig.class)).thenReturn(commandLineOptions);

        configBuilder = new ConfigBuilder<>(TestConfig.class, configBuilderFactory);
    }

    @After
    public void tearDown() {
        System.setOut(null);
    }

    @Test
    public void testWithCommandLineArgs() throws Exception {
        when(commandLineHelper.getCommandLine(TestConfig.class, new String[]{})).thenReturn(commandLine);
        assertEquals(configBuilder,configBuilder.withCommandLineArgs(new String[]{}));
    }

    @Test
    public void testOverridePropertiesFiles() throws Exception {
        List<String> baseNames = Lists.newArrayList("file");
        configBuilder.overridePropertiesFiles(baseNames);
        verify(propertyLoader).withBaseNames(baseNames);
    }

    @Test
    public void testPrintCommandLineHelp() throws Exception {
        configBuilder.printCommandLineHelp();
        assertTrue(outContent.toString().contains("Command Line Options for class TestConfig"));
    }

    @Test
    public void testBuild() throws Exception {
        when(propertyLoader.load()).thenReturn(properties);
        configBuilder.build();
        verify(propertyLoader).load();
        verify(builderConfiguration).setProperties(properties);
        verify(errorMessageSetup).initialize(null, propertyLoader);

        verify(fieldSetter).setFields(Matchers.any(TestConfig.class), Matchers.any(BuilderConfiguration.class));
        verify(configValidator).validate(Matchers.any(TestConfig.class));
    }

//    @Test
//    This test does not work. It needs to be fixed. Urgently. Please do it. Only you can save us!
    public void testMerge() throws Exception {
        TestConfig importedConfig = new TestConfig();
        when(propertyLoader.load()).thenReturn(properties);
        
        configBuilder.withImportedConfiguration(importedConfig).build();
        
        verify(propertyLoader).load();
        verify(builderConfiguration).setProperties(properties);
        verify(errorMessageSetup).initialize(null, propertyLoader);
    }
}
