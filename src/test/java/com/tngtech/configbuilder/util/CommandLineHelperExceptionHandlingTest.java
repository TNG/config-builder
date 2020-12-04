package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.ConfigBuilder;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValueDescriptor;
import com.tngtech.configbuilder.exception.ConfigBuilderException;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CommandLineHelperExceptionHandlingTest {

    private static class TestConfig {

        @CommandLineValue(shortOpt = "u", longOpt = "user", required = true)
        public String user;
    }

    private static class TestConfigWithInvalidCommandLineValueDescriptor {

        @CommandLineValue(shortOpt = "u", longOpt = "user")
        public String user;

        @CommandLineValueDescriptor
        private static void description() { }
    }

    private static class TestConfigWithMultipleCommandLineValueDescriptors {

        @CommandLineValue(shortOpt = "u", longOpt = "user")
        public String user;

        @CommandLineValueDescriptor
        private static String description1() {
            return "";
        }

        @CommandLineValueDescriptor
        private static String description2() {
            return "";
        }
    }

    @Test
    public void testUndefinedCommandLineOption() {
        String[]  args = new String[]{"nd", "notDefined"};
        assertThatThrownBy(() -> ConfigBuilder.on(TestConfig.class).withCommandLineArgs(args).build())
                .isInstanceOf(ConfigBuilderException.class)
                .hasMessageContaining("unable to parse command line arguments");
    }

    @Test
    public void testInvalidCommandLineValueDescriptor() {
        assertThatThrownBy(() -> ConfigBuilder.on(TestConfigWithInvalidCommandLineValueDescriptor.class).printCommandLineHelp())
                .isInstanceOf(ConfigBuilderException.class)
                .hasMessageContaining("invalid or multiple use of the @CommandLineValueDescriptor annotation");

        assertThatThrownBy(() -> ConfigBuilder.on(TestConfigWithInvalidCommandLineValueDescriptor.class).build())
                .isInstanceOf(ConfigBuilderException.class)
                .hasMessageContaining("invalid or multiple use of the @CommandLineValueDescriptor annotation");
    }

    @Test
    public void testMultipleCommandLineValueDescriptors() {
        assertThatThrownBy(() -> ConfigBuilder.on(TestConfigWithMultipleCommandLineValueDescriptors.class).printCommandLineHelp())
                .isInstanceOf(ConfigBuilderException.class)
                .hasMessageContaining("invalid or multiple use of the @CommandLineValueDescriptor annotation");

        assertThatThrownBy(() -> ConfigBuilder.on(TestConfigWithMultipleCommandLineValueDescriptors.class).build())
                .isInstanceOf(ConfigBuilderException.class)
                .hasMessageContaining("invalid or multiple use of the @CommandLineValueDescriptor annotation");
    }
}
