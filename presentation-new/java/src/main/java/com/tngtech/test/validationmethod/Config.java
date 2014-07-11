// not shown
package com.tngtech.test.validationmethod;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.validation.Validation;
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

    @Size(min = 2, max = 14)
    @DefaultValue("Wolverine")
    @CommandLineValue(shortOpt="c", longOpt="character-name", hasArg=true)
    private String characterName;

    @NotNull
    @EnvironmentVariableValue("PWD")
    private Path currentDirectory;

    // not shown
    //@Validation
    //private void validate() {
    //       if("god".equalsIgnoreCase(characterName)) {
    //           throw new RuntimeException("You can not name yourself God!");
    //       }
    //}
    // shown
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