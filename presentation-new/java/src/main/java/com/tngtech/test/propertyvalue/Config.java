// not shown
package com.tngtech.test.propertyvalue;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.test.common.JSONHelper;

// shown
@PropertiesFiles("config")
public class Config {
    // not shown
    public static void main(String[] args) throws Exception {
        Config config = ConfigBuilder.on(Config.class).build();
        JSONHelper.printJSON(config);
    }

    // shown

    @DefaultValue("1")
    @CommandLineValue(shortOpt="d", longOpt="difficulty", hasArg=true)
    //not shown
    //@PropertyValue("difficulty")
    //shown
    private int difficulty;

    //not shown
    //@PropertyValue("player.color")
    //shown
    private int playerColor;
    // not shown
    public int getDifficulty() {
        return difficulty;
    }

    public int getPlayerColor() {
        return playerColor;
    }
    //shown
}