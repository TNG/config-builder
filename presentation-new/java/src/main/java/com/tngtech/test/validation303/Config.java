// not shown
package com.tngtech.test.validation303;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.valueextractor.EnvironmentVariableValue;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.test.common.JSONHelper;

import java.nio.file.Path;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

// shown
public class Config
{
    // not shown
    public static void main(String[] args) throws Exception {
        Config config = new ConfigBuilder<>(Config.class).withCommandLineArgs(args).build();
        JSONHelper.printJSON(config);
    }
    // shown
    // ...

    // not shown
    // @Size(min = 2, max = 14)
    // shown
    @DefaultValue("Player")
    @CommandLineValue(shortOpt="c", longOpt="character-name", hasArg=true)
    private String characterName;

    // not shown
    // @NotNull
    // shown
    @EnvironmentVariableValue("PWD")
    private Path currentDirectory;

    // Setters and getters

    // not shown
    public String getCharacterName() {
        return characterName;
    }

    public Path getCurrentDirectory() {
        return currentDirectory;
    }
    // shown
}