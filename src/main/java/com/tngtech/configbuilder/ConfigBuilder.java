package com.tngtech.configbuilder;

import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.ErrorMessageFile;
import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;
import com.tngtech.configbuilder.util.*;
import com.tngtech.propertyloader.PropertyLoader;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.util.List;

/**
 * Builds a config object.
 * ConfigBuilder instantiates a class and sets fields of the instance by parsing annotations and
 * loading values from properties files or the command line. It validates the instance by parsing JSR303 constraint annotations.<p>
 * <p/>
 * Fields of the config class can have the following annotations:<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.DefaultValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.PropertyValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.SystemPropertyValue}<br>
 * {@link com.tngtech.configbuilder.annotation.valueextractor.EnvironmentVariableValue}<br>
 * {@link LoadingOrder}<p>
 * <p/>
 * Properties files are loaded with a PropertyLoader using its default config. In order to change settings for the PropertyLoader, the config class may be annotated with<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles}<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations}<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertySuffixes}<br>
 * {@link com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyExtension}<p>
 * <p/>
 * To specify a global order for parsing {@link com.tngtech.configbuilder.annotation.valueextractor.ValueExtractorAnnotation} annotations, annotate the class with <br>
 * {@link LoadingOrder}<p>
 * <p/>
 * To specify your own error messages file (which is loaded by the PropertyLoader with the same settings as other the properties files), annotate the class with <br>
 * {@link ErrorMessageFile}<p>
 *
 * @param <T> The type of the config class which shall be instantiated.
 * @author Matthias Bollwein
 * @version 0.1-SNAPSHOT
 */
public class ConfigBuilder<T> {

    private final BuilderConfiguration builderConfiguration;
    private final CommandLineHelper commandLineHelper;
    private final FieldSetter<T> fieldSetter;
    private final ConfigValidator<T> configValidator;
    private final ErrorMessageSetup errorMessageSetup;
    private final ConstructionHelper<T> constructionHelper;

    private Class<T> configClass;
    private Options commandLineOptions;
    private PropertyLoader propertyLoader;
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

        propertyLoader = configBuilderFactory.getInstance(PropertyLoaderConfigurator.class).configurePropertyLoader(configClass);
        commandLineOptions = commandLineHelper.getOptions(configClass);
    }

    /**
     * @param configClass The config class of which an instance shall be built.
     */
    public ConfigBuilder(Class<T> configClass) {
        this(configClass,new ConfigBuilderFactory());
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
     * @param importedConfiguration
     * @return
     */
    public ConfigBuilder<T> withImportedConfiguration(Object importedConfiguration) {
        builderConfiguration.setImportedConfiguration(importedConfiguration);
        return this;
    }

    /**
     * Configures the Config Builder to load given properties files instead of those specified in the config class.
     *
     * @param baseNames
     * @return
     */
    public ConfigBuilder<T> overridePropertiesFiles(List<String> baseNames) {
        propertyLoader.withBaseNames(baseNames);
        return this;
    }

    /**
     * Prints a help message for all command line options that are configured in the config class.
     */
    public void printCommandLineHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setSyntaxPrefix("Command Line Options for class " + configClass.getSimpleName() + ":");
        formatter.printHelp(" ", commandLineOptions);
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
        builderConfiguration.setProperties(propertyLoader.load());
        builderConfiguration.setCommandLine(commandLineHelper.getCommandLine(configClass, commandLineArgs));
    }

    private void initializeErrorMessageSetup(PropertyLoader propertyLoader) {
        String errorMessageFile = configClass.isAnnotationPresent(ErrorMessageFile.class) ? configClass.getAnnotation(ErrorMessageFile.class).value() : null;
        errorMessageSetup.initialize(errorMessageFile, propertyLoader);
    }
}