// not shown
package com.tngtech.test.propertyloader1;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyExtension;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertySuffixes;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.EnvironmentVariableValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.test.cheattransformer.Cheat;
import com.tngtech.test.common.JSONHelper;

import java.nio.file.Path;
import java.util.Set;

//@PropertiesFiles({"config", "test-config"})
//@PropertySuffixes(extraSuffixes = {"tngtech","myname"}, hostNames = true)
//@PropertyExtension("fileextension")

// shown
public class Config {
    // not shown
    public static void main(String[] args) throws Exception {
        Config config = new ConfigBuilder<>(Config.class).build();
        JSONHelper.printJSON(config);
    }

    // shown

    @DefaultValue("Wolverine")
    @CommandLineValue(shortOpt="c", longOpt="character-name", hasArg=true)
    private String characterName;

    @EnvironmentVariableValue("PWD")
    private Path currentDirectory;

    // ...

}