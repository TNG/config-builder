// not shown
package com.tngtech.test.commandlinevalue;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.test.common.JSONHelper;

// shown
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

    @DefaultValue("1")
    @CommandLineValue(shortOpt="d", longOpt="difficulty", hasArg=true)
    private int difficulty;

    // ...
    // not shown
    public String getCharacterName() {
        return characterName;
    }

    public int getDifficulty() {
        return difficulty;
    }
    //shown
}