package com.tngtech.configbuilder;

import com.tngtech.configbuilder.exception.ValidatorException;
import com.tngtech.configbuilder.testclasses.TestConfig;
import com.tngtech.configbuilder.testutil.SystemOutRule;
import org.junit.Rule;
import org.junit.Test;

import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ConfigBuilderIntegrationTest {

    @Rule
    public SystemOutRule systemOut = new SystemOutRule();

    private ConfigBuilder<TestConfig> configBuilder = ConfigBuilder.on(TestConfig.class);

    @Test
    public void testConfigBuilderWithParameters() {
        TestConfig expectedTestConfig = new TestConfig();
        expectedTestConfig.setSomeString("Hello, World!");
        expectedTestConfig.setSomeNumber(3);
        expectedTestConfig.setBoolean(true);
        expectedTestConfig.setStringCollection(newArrayList("first entry", "second entry"));
        expectedTestConfig.setIntegerList(newArrayList(1, 2, 3, 4, 5));
        expectedTestConfig.setPathCollection(newHashSet(Paths.get("/etc"), Paths.get("/usr")));
        expectedTestConfig.setHomeDir(Paths.get(System.getenv("HOME")));
        expectedTestConfig.setSystemProperty(System.getProperty("user.language"));

        String[] args = {"-u", "--collection", "first entry,second entry"};
        Object result = configBuilder.withCommandLineArgs(args).build();

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedTestConfig);
        assertThat(systemOut.getLog()).contains("config validated");
    }

    @Test
    public void testWithImportedConfig() {
        ArrayList<String> arrayList = newArrayList("collection", "two");
        String home = System.getenv("HOME");
        String userLanguage = System.getProperty("user.language");

        TestConfig importedTestConfig = new TestConfig();
        importedTestConfig.setSomeNumber(5);
        importedTestConfig.setStringCollection(newArrayList("/mnt", "/home"));

        TestConfig expectedTestConfig = new TestConfig();
        expectedTestConfig.setSomeNumber(5);
        List<Path> paths = Arrays.asList(Paths.get("/mnt"), Paths.get("/home"));
        expectedTestConfig.setPathCollection(newHashSet(paths));
        expectedTestConfig.setCopiedStringCollection(importedTestConfig.getStringCollection());
        expectedTestConfig.setSomeString("Hello, World!");
        expectedTestConfig.setBoolean(true);
        expectedTestConfig.setStringCollection(arrayList);
        expectedTestConfig.setIntegerList(newArrayList(1, 2, 3, 4, 5));

        expectedTestConfig.setHomeDir(Paths.get(home));
        expectedTestConfig.setSystemProperty(userLanguage);

        String[] args = {"-u", "--collection", "collection,two"};
        Object result = configBuilder.withCommandLineArgs(args).withImportedConfiguration(importedTestConfig).build();

        assertThat(result).usingRecursiveComparison().isEqualTo(expectedTestConfig);
        assertThat(systemOut.getLog()).contains("config validated");
    }

    static class ConfigWithNotNullValidation {
        @NotNull
        private String notNullString;
    }

    @Test
    public void testValidation(){
        ConfigBuilder<ConfigWithNotNullValidation> configBuilder = new ConfigBuilder<>(ConfigWithNotNullValidation.class);
        assertThatThrownBy(configBuilder::build)
                .isInstanceOf(ValidatorException.class)
                .hasMessageContaining("must not be null");
    }
}
