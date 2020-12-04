package com.tngtech.configbuilder;

import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.annotation.configuration.PropertyNamePrefix;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.ErrorMessageFile;
import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.util.*;
import com.tngtech.propertyloader.PropertyLoader;
import com.tngtech.propertyloader.impl.DefaultPropertyFilterContainer;
import com.tngtech.propertyloader.impl.DefaultPropertyLocationContainer;
import com.tngtech.propertyloader.impl.DefaultPropertySuffixContainer;
import com.tngtech.propertyloader.impl.interfaces.PropertyLoaderFilter;
import org.apache.commons.cli.HelpFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Builds a config object.
 * ConfigBuilder instantiates a class and sets fields of the instance by parsing annotations and
 * loading values from properties files or the command line. It validates the instance by parsing JSR303 constraint annotations.<p>
 *
 * Fields of the config class can have the following annotations:<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.DefaultValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.PropertyValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.SystemPropertyValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.EnvironmentVariableValue}<br>
 * {@link LoadingOrder}<br>
 *
 * Properties files are loaded with a PropertyLoader using its default config. In order to change settings for the PropertyLoader, the config class may be annotated with<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles}<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations}<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertySuffixes}<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyExtension}<br>
 *
 * To specify a global order for parsing {@link com.tngtech.configbuilder.annotation.valueextractor.ValueExtractorAnnotation} annotations, annotate the class with <br>
 * {@link LoadingOrder}<br>
 *
 * To specify your own error messages file (which is loaded by the PropertyLoader with the same settings as other the properties files), annotate the class with <br>
 * {@link ErrorMessageFile}<br>
 *
 * @param <T> The type of the config class which shall be instantiated.
 * @author Matthias Bollwein
 * @version 0.1-SNAPSHOT
 */
public class ConfigBuilder<T> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CommandLineHelper.class);
    public static final Object AT_CONTEXT_CLASS_PATH = new Object();
    
    private final BuilderConfiguration builderConfiguration;
    private final CommandLineHelper commandLineHelper;
    private final FieldSetter<T> fieldSetter;
    private final ConfigValidator<T> configValidator;
    private final ErrorMessageSetup errorMessageSetup;
    private final ConstructionHelper<T> constructionHelper;

    private Class<T> configClass;
    private PropertyLoader propertyLoader;
    private Properties additionalProperties;
    private String[] commandLineArgs = {};
    
    protected ConfigBuilder(Class<T> configClass, ConfigBuilderFactory configBuilderFactory) {
        configBuilderFactory.<T>initialize();
        this.configClass = configClass;
        this.builderConfiguration = configBuilderFactory.getInstance(BuilderConfiguration.class);
        this.commandLineHelper = configBuilderFactory.getInstance(CommandLineHelper.class);
        this.configValidator = configBuilderFactory.getInstance(ConfigValidator.class);
        this.fieldSetter = configBuilderFactory.getInstance(FieldSetter.class);
        this.errorMessageSetup = configBuilderFactory.getInstance(ErrorMessageSetup.class);
        this.constructionHelper = configBuilderFactory.getInstance(ConstructionHelper.class);
        this.additionalProperties = configBuilderFactory.createInstance(Properties.class);

        this.propertyLoader = configBuilderFactory.getInstance(PropertyLoaderConfigurator.class).configurePropertyLoader(configClass);
    }

    /**
     * @param configClass The config class of which an instance shall be built.
     */
    public ConfigBuilder(Class<T> configClass) {
        this(configClass, new ConfigBuilderFactory());
    }

    /**
     * Sets the command line arguments that the ConfigBuilder uses in order to parse fields annotated with <code>@CommandLineValue</code>.
     * Command line arguments must match the options specified in the <code>@CommandLineValue</code> annotations.
     *
     * @param args the command line arguments
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withCommandLineArgs(String[] args) {
        this.commandLineArgs = args;
        return this;
    }

    /**
     * Imports the values from the given object according to the field names in the annotations
     * @param importedConfiguration configuration object to be imported
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withImportedConfiguration(Object importedConfiguration) {
        builderConfiguration.setImportedConfiguration(importedConfiguration);
        return this;
    }

    /**
     * Configures the Config Builder to load given property files instead of those specified in the config class.
     *
     * @param baseNames base names of the property files to be loaded
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> overridePropertiesFiles(List<String> baseNames) {
        propertyLoader.withBaseNames(baseNames);
        return this;
    }

    /**
     * Provide additional properties which will overwrite the properties retrieved by the property loader
     * 
     * @param properties to be added to the properties already present (starting from the result of the property loader)
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> addProperties(Properties properties) {
        additionalProperties.putAll(properties);
        return this;
    }

    /**
     * set the extension to search for property files
     * @param propertyExtension property file name extension to use
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withPropertyExtension(String propertyExtension) {
        propertyLoader.withExtension(propertyExtension);
        return this;
    }

    /**
     * set property suffix to b
     * @param propertySuffix property file name suffix
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withPropertySuffix(String propertySuffix) {
        return withPropertySuffixes(propertySuffix);
    }
    
    /**
     * replace list of possible property suffixes by given elements 
     * @param suffixArray one or more property file name suffix
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withPropertySuffixes(String ... suffixArray) {
        final DefaultPropertySuffixContainer suffixes = propertyLoader.getSuffixes();
        suffixes.clear();
        suffixes.addSuffixList(Arrays.asList(suffixArray));
        return this;
    }

    /**
     * add more property file suffixes to the list of possible property suffixes
     * @param suffixArray one or more property file name suffix
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> addPropertySuffixes(String... suffixArray) {
        propertyLoader.getSuffixes().addSuffixList(Arrays.asList(suffixArray));
        return this;
    }

    /**
     * set file name of property file to read
     * @param fileName property file name
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withPropertiesFile(String fileName) {
        return withPropertiesFiles(fileName);
    }

    /**
     * set file names of property files to read
     * @param fileNames one or more property file names
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withPropertiesFiles(String ... fileNames) {
        propertyLoader.withBaseNames(Arrays.asList(fileNames));
        return this;
    }

    /**
     * set property locations
     * @param propertyLocations lists of property locations which can be 
     *                          Strings: use as Directory
     *                          Classes: use as Class Resource Location
     * @return the instance of ConfigBuilder
     */
    public ConfigBuilder<T> withPropertyLocations(Object ... propertyLocations) {
        final DefaultPropertyLocationContainer locations = propertyLoader.getLocations();
        locations.clear();
        for (Object propertyLocation : propertyLocations) {
            if (propertyLocation instanceof String) {
                locations.atDirectory((String)propertyLocation);
            } else if (propertyLocation instanceof Class) {
                locations.atRelativeToClass((Class)propertyLocation);
            } else if (propertyLocation == AT_CONTEXT_CLASS_PATH) {
                locations.atContextClassPath();
            } else {
                LOGGER.warn("unhandled property location '{}'", propertyLocation);
            }
        }
        return this;
    }

    /**
     * set property filters in use
     * @param propertyFilters property filters which should be applied after loading properties
     * @return the instance of ConfigBuilder
     */
    @SafeVarargs
    public final ConfigBuilder<T> withPropertyFilters(Class<? extends PropertyLoaderFilter>... propertyFilters) {
        final DefaultPropertyFilterContainer filterContainer = propertyLoader.getFilters();
        final List<PropertyLoaderFilter> filters = filterContainer.getFilters();
        filters.clear();

        for (Class<? extends PropertyLoaderFilter> propertyFilter : propertyFilters) {
            try {
                filters.add(propertyFilter.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                LOGGER.error("could not create filter '{}'", propertyFilter.getSimpleName(), e);
            }
        }
        return this;
    }
    
    /**
     * Prints a help message for all command line options that are configured in the config class.
     */
    public void printCommandLineHelp() {
        initializeErrorMessageSetup(propertyLoader);
        HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Command Line Options for class " + configClass.getSimpleName() + ":");
        formatter.printHelp(" ", commandLineHelper.getOptions(configClass));
    }

    /**
     * Gets an instance of the config, sets the fields and validates them.
     * The method sets up the configuration by loading properties and parsing command line arguments,
     * then tries to find a constructor of the config class that matches the arguments passed to it,
     * instantiates the config class, sets its fields and validates the instance.
     *
     * @param objects a vararg of Objects passed to a corresponding constructor of the config class.
     * @return An instance of the config class.
     */
    public T build(Object... objects) {
        initializeErrorMessageSetup(propertyLoader);
        setupBuilderConfiguration(propertyLoader);
        T instanceOfConfigClass = constructionHelper.getInstance(configClass, objects);
        fieldSetter.setFields(instanceOfConfigClass, builderConfiguration);
        configValidator.validate(instanceOfConfigClass);
        return instanceOfConfigClass;
    }

    private void setupBuilderConfiguration(PropertyLoader propertyLoader) {
        if (configClass.isAnnotationPresent(LoadingOrder.class)) {
            builderConfiguration.setAnnotationOrder(configClass.getAnnotation(LoadingOrder.class).value());
        }

        if (configClass.isAnnotationPresent(PropertyNamePrefix.class)) {
            builderConfiguration.setPropertyNamePrefixes(configClass.getAnnotation(PropertyNamePrefix.class).value());
        }
        
        final Properties properties = propertyLoader.load();
        properties.putAll(additionalProperties);
        builderConfiguration.setProperties(properties);
        
        builderConfiguration.setCommandLine(commandLineHelper.getCommandLine(configClass, commandLineArgs));
    }

    private void initializeErrorMessageSetup(PropertyLoader propertyLoader) {
        String errorMessageFile = configClass.isAnnotationPresent(ErrorMessageFile.class) ? configClass.getAnnotation(ErrorMessageFile.class).value() : null;
        errorMessageSetup.initialize(errorMessageFile, propertyLoader);
    }

    /**
     * Gets an instance of the ConfigBuilder for a given config class
     *
     * @param clazz config class for which the config builder is instantiated.
     * @param <T> generic type of the config class
     * @return ConfigBuilder instance for config class
     */
    public static <T> ConfigBuilder<T> on(Class<T> clazz) {
        return new ConfigBuilder<>(clazz);
    }
}