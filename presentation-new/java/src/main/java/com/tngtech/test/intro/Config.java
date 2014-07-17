// not shown
package com.tngtech.test.intro;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.test.common.JSONHelper;

// shown
public class Config {

    public static void main(String[] args) throws Exception {
    //not shown
        //Config config = ConfigBuilder.on(Config.class)
        //                    .withCommandLineArgs(args)
        //                    .build();
        //JSONHelper.printJSON(config);
    //shown
    }

    private String characterName;

    public String getCharacterName() {
          return characterName;
    }
}