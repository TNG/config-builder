// not shown
package com.tngtech.test.customannotation;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.configuration.LoadingOrder;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.test.common.JSONHelper;

@LoadingOrder({FileContentValue.class, CommandLineValue.class, PropertyValue.class, DefaultValue.class})
// shown
public class Config
{
    // not shown
    public static void main(String[] args) throws Exception {
        Config config = new ConfigBuilder<>(Config.class).build();
        JSONHelper.printJSON(config);
    }
    // shown
    // ...

    @FileContentValue(value="/tmp/help.txt", encoding="ISO-8859-1")
    private String helptext;

    // Setters and getters
    // not shown
    public String getHelptext() {
        return helptext;
    }
    // shown
}