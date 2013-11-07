package com.tngtech.configbuilder.annotation.valueextractor;


import com.tngtech.configbuilder.configuration.BuilderConfiguration;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;

import java.lang.annotation.Annotation;

/**
 * Processes CommandLineValue annotations, implements IValueExtractorProcessor
 */
public class CommandLineValueProcessor implements IValueExtractorProcessor {

    public String getValue(Annotation annotation, ConfigBuilderFactory configBuilderFactory) {
        BuilderConfiguration builderConfiguration = configBuilderFactory.getInstance(BuilderConfiguration.class);
        
        if (((CommandLineValue) annotation).hasArg()) {
            return builderConfiguration.getCommandLine().getOptionValue(((CommandLineValue) annotation).shortOpt());
        } else {
            return String.valueOf(builderConfiguration.getCommandLine().hasOption(((CommandLineValue) annotation).shortOpt()) || builderConfiguration.getCommandLine().hasOption(((CommandLineValue) annotation).longOpt()));
        }
    }
}
