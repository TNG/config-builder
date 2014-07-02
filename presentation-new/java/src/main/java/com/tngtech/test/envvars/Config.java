// not shown
package com.tngtech.test.envvars;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.valueextractor.EnvironmentVariableValue;
import com.tngtech.configbuilder.annotation.valueextractor.SystemPropertyValue;
import com.tngtech.test.common.JSONHelper;

import java.nio.file.Path;

// shown
public class Config {
    // not shown
    public static void main(String[] args) throws Exception {
        Config config = new ConfigBuilder<>(Config.class).build();
        JSONHelper.printJSON(config);
    }

    // shown
    // ...

    @SystemPropertyValue("user.language")
    private String language;

    @EnvironmentVariableValue("PWD")
    private Path currentDirectory;

    // ...
    // not shown
    public String getLanguage() { return language; }

    public Path getCurrentDirectory() { return currentDirectory; }
    //shown
}