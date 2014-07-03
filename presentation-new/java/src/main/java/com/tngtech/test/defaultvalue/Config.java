// not shown
package com.tngtech.test.defaultvalue;

import com.tngtech.configbuilder.ConfigBuilder;
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

    //not shown
    //@DefaultValue("Wolverine")
    //shown
    private String characterName;

    //not shown
    //@DefaultValue("1")
    //shown
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