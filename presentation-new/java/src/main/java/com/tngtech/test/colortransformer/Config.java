// not shown
package com.tngtech.test.colortransformer;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import javax.swing.text.html.StyleSheet;
import java.awt.*;

@PropertiesFiles("config")
// shown
public class Config {
    // not shown
    public static void main(String[] args) {
        Config config = new ConfigBuilder<>(Config.class).build();
        System.out.println(config.getPlayerColor().toString());
    }
    
    // shown
    @PropertyValue("player.color")
    @TypeTransformers({StringToColorTransformer.class})
    private Color playerColor;

    // ...
    // not shown
    public Color getPlayerColor() {
        return playerColor;
    }
    // shown

    public static class StringToColorTransformer extends TypeTransformer<String, Color> {
        @Override
        public Color transform(String colorText) {
            StyleSheet styleSheet = new StyleSheet();
            return styleSheet.stringToColor(colorText);
        }
    }
}