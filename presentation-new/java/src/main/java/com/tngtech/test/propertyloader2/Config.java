// not shown
package com.tngtech.test.propertyloader2;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.EnvironmentVariableValue;
import com.tngtech.test.common.JSONHelper;

import java.nio.file.Path;


//@PropertyLocations(directories = {"/home/user"}, fromClassLoader = true)
// shown
@PropertiesFiles({"config", "test-config"})
public class Config {
    // not shown
    public static void main(String[] args) throws Exception {
        Config config = new ConfigBuilder<>(Config.class).build();
        JSONHelper.printJSON(config);
    }

    // shown

    @DefaultValue("Player")
    @CommandLineValue(shortOpt="c", longOpt="character-name", hasArg=true)
    private String characterName;

    @EnvironmentVariableValue("PWD")
    private Path currentDirectory;

    // ...

}