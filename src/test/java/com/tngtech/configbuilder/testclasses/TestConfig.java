package com.tngtech.configbuilder.testclasses;

import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyExtension;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertySuffixes;
import com.tngtech.configbuilder.annotation.typetransformer.*;
import com.tngtech.configbuilder.annotation.validation.Validation;
import com.tngtech.configbuilder.annotation.valueextractor.*;
import com.tngtech.propertyloader.PropertyLoader;

import java.util.Collection;

@PropertyExtension("testproperties")
@PropertySuffixes(extraSuffixes = {"test"})
@PropertyLocations(resourcesForClasses = {PropertyLoader.class})
@PropertiesFiles("demoapp-configuration")
public class TestConfig {

    public TestConfig() {

    }

    @DefaultValue("3")
//    @TypeTransformers({StringToIntegerTransformer.class})
    private int someNumber;

    @PropertyValue("a")
    private String helloWorld;

    @CommandLineValue(shortOpt = "u", longOpt = "user")
//    @TypeTransformers({StringToBooleanTransformer.class})
    private boolean aBoolean;

    @LoadingOrder(value = {CommandLineValue.class})
    @CommandLineValue(shortOpt = "c", longOpt = "collection", hasArg = true, description = "command line option description")
    @TypeTransformers({CommaSeparatedStringToStringCollectionTransformer.class})
    private Collection<String> stringCollection;

    @EnvironmentVariableValue("PATH")
    private String environmentVariable;

    @SystemPropertyValue("user.language")
    private String systemProperty;

    public void setSomeNumber(Integer someNumber) {
        this.someNumber = someNumber;
    }

    public void setHelloWorld(String helloWorld) {
        this.helloWorld = helloWorld;
    }

    public void setBoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
    }

    public void setStringCollection(Collection<String> stringCollection) {
        this.stringCollection = stringCollection;
    }

    public void setEnvironmentVariable(String environmentVariable) {
        this.environmentVariable = environmentVariable;
    }

    public void setSystemProperty(String systemProperty) {
        this.systemProperty = systemProperty;
    }

    @Validation
    private void validate() {

    }
}
