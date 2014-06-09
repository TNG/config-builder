// not shown
package com.tngtech.test.example1;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;

// shown
public class Config
{
  public static void main(String[] args) {
    Config config = new ConfigBuilder<>(Config.class).build();
    System.out.println(config.getCharacterName());
  }

  @DefaultValue("Wolverine")
  private String characterName;

  public String getCharacterName() {
    return characterName;
  }
}