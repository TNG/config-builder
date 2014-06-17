// not shown
package com.tngtech.test.cheattransformer;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import java.util.Set;

@PropertiesFiles("config")
// shown
public class Config {
    // not shown
    public static void main(String[] args) {
        Config config = new ConfigBuilder<>(Config.class).build();
        System.out.println(config.getCheats().toString());
    }

    // shown

    @PropertyValue("cheats.active")
    @TypeTransformers({StringToCheatTransformer.class})
    private Set<Cheat> cheats;

    // ...
    // not shown
    public Set<Cheat> getCheats() {
        return cheats;
    }
    // shown

    public class StringToCheatTransformer extends TypeTransformer<String, Cheat> {
        @Override
        public Cheat transform(String cheatText) {
            return Cheat.valueOf(cheatText);
        }
    }
}