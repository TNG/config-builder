// not shown
package com.tngtech.test.intro;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.test.common.JSONHelper;

// shown
public class Config {

    public static void main(String[] args) throws Exception {
        Config config = new ConfigBuilder<>(Config.class).build();
        JSONHelper.printJSON(config);
    }

    private String characterName;

    public String getCharacterName() {
          return characterName;
      }
}