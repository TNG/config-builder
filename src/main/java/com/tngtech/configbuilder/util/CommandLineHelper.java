package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValueDescriptor;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.ConfigBuilderException;
import com.tngtech.configbuilder.exception.InvalidDescriptionMethodException;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import static com.google.common.collect.Iterables.getOnlyElement;

public class CommandLineHelper {

    private final static Logger log = LoggerFactory.getLogger(CommandLineHelper.class);

    private final ConfigBuilderFactory configBuilderFactory;
    private final AnnotationHelper annotationHelper;
    private final ErrorMessageSetup errorMessageSetup;

    public CommandLineHelper(ConfigBuilderFactory configBuilderFactory) {
        this.configBuilderFactory = configBuilderFactory;
        this.annotationHelper = configBuilderFactory.getInstance(AnnotationHelper.class);
        this.errorMessageSetup = configBuilderFactory.getInstance(ErrorMessageSetup.class);
    }

    public CommandLine getCommandLine(Class configClass, String[] args) {
        log.info("getting command line options from fields and parsing command line arguments");
        Options options = getOptions(configClass);
        return parseCommandLine(args, options);
    }

    public Options getOptions(Class configClass) {
        Options options = configBuilderFactory.createInstance(Options.class);
        for (Field field : annotationHelper.getFieldsAnnotatedWith(configClass, CommandLineValue.class)) {
            if (!field.isSynthetic()) {
                options.addOption(getOption(field, configClass));
            }
        }
        return options;
    }

    private Option getOption(Field field, Class configClass) {
        CommandLineValue commandLineValue = field.getAnnotation(CommandLineValue.class);
        log.debug("adding command line option {} for field {}", commandLineValue.shortOpt(), field.getName());
        return Option.builder(commandLineValue.shortOpt())
                .longOpt(commandLineValue.longOpt())
                .hasArg()
                .required(commandLineValue.required())
                .desc(extractDescriptionString(commandLineValue, configClass))
                .hasArg(commandLineValue.hasArg())
                .build();
    }

    private String extractDescriptionString(CommandLineValue commandLineValue, Class configClass) {
        if (!commandLineValue.description().isEmpty()) {
            return commandLineValue.description();
        }

        Set<Method> descriptorMethods = annotationHelper.getMethodsAnnotatedWith(configClass, CommandLineValueDescriptor.class);
        if (descriptorMethods.isEmpty()) {
            return "";
        }

        try {
            Method descriptorMethod = getOnlyElement(descriptorMethods);
            descriptorMethod.setAccessible(true);
            return descriptorMethod.invoke(null, commandLineValue.longOpt()).toString();
        } catch (Exception e) {
            throw new ConfigBuilderException(errorMessageSetup.getErrorMessage(InvalidDescriptionMethodException.class), e);
        }
    }

    private CommandLine parseCommandLine(String[] args, Options options) {
        CommandLine commandLine;
        try {
            commandLine = configBuilderFactory.createInstance(DefaultParser.class).parse(options, args);
        } catch (ParseException e) {
            throw new ConfigBuilderException(errorMessageSetup.getErrorMessage(e.getClass().getSuperclass()), e);
        }
        return commandLine;
    }
}