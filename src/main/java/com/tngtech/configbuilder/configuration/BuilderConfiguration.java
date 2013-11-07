package com.tngtech.configbuilder.configuration;

import com.tngtech.configbuilder.annotation.valueextractor.*;
import org.apache.commons.cli.CommandLine;

import java.lang.annotation.Annotation;
import java.util.Properties;

/**
 * Stores the configuration for the ConfigBuilder, i.e. the CommandLine, the Properties and the global annotation processing order.
 */
public class BuilderConfiguration {

    private Properties properties;
    private CommandLine commandLine;
    private Object importedConfiguration;
    private Class<? extends Annotation>[] annotationOrder = new Class[]{CommandLineValue.class, PropertyValue.class, EnvironmentVariableValue.class, SystemPropertyValue.class, ImportedValue.class, DefaultValue.class};

    public BuilderConfiguration() {
        properties = new Properties();
        commandLine = null;
        importedConfiguration = null;
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public Properties getProperties() {
        return properties;
    }

    public Object getImportedConfiguration() {
        return importedConfiguration;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setCommandLine(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public void setImportedConfiguration(Object importedConfiguration) {
        this.importedConfiguration = importedConfiguration;
    }

    public void setAnnotationOrder(Class<? extends Annotation>[] annotationOrder) {
        this.annotationOrder = annotationOrder;
    }

    public Class<? extends Annotation>[] getAnnotationOrder() {
        return annotationOrder;
    }
}
