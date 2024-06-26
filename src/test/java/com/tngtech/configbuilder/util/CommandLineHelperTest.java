package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValueDescriptor;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommandLineHelperTest {

    private static class TestConfig {
        @CommandLineValue(shortOpt = "u", longOpt = "user", required = true, description = "some static description string")
        public String aString;
        @CommandLineValue(shortOpt = "v", longOpt = "vir")
        public String anotherString;

        @CommandLineValueDescriptor
        private static String description(String longOpt) {
            if ("vir".equals(longOpt)) {
                return "some dynamically generated description";
            }
            return "";
        }
    }

    private CommandLineHelper commandLineHelper;
    private final String[] args = null;

    @Mock
    private Options options;
    @Mock
    private DefaultParser parser;
    @Mock
    private CommandLine commandLine;
    @Mock
    private ConfigBuilderFactory configBuilderFactory;
    @Mock
    private ErrorMessageSetup errorMessageSetup;

    @BeforeEach
    public void setUp() throws Exception {
        when(configBuilderFactory.getInstance(AnnotationHelper.class)).thenReturn(new AnnotationHelper());
        when(configBuilderFactory.getInstance(ErrorMessageSetup.class)).thenReturn(errorMessageSetup);

        commandLineHelper = new CommandLineHelper(configBuilderFactory);
    }

    @Test
    public void testGetCommandLine() throws Exception {
        when(parser.parse(options, args)).thenReturn(commandLine);
        when(configBuilderFactory.createInstance(DefaultParser.class)).thenReturn(parser);
        when(configBuilderFactory.createInstance(Options.class)).thenReturn(options);
        ArgumentCaptor<Option> captor = ArgumentCaptor.forClass(Option.class);
        assertThat(commandLineHelper.getCommandLine(TestConfig.class, args)).isSameAs(commandLine);
        verify(options, times(2)).addOption(captor.capture());
        verify(parser).parse(options, args);

        List<Option> sortedOptions = new ArrayList<>(captor.getAllValues());
        sortedOptions.sort(comparing(Option::getLongOpt));

        assertThat(sortedOptions).hasSize(2);

        assertThat(sortedOptions.get(0).getLongOpt()).isEqualTo("user");
        assertThat(sortedOptions.get(0).getOpt()).isEqualTo("u");
        assertThat(sortedOptions.get(0).isRequired()).isTrue();

        assertThat(sortedOptions.get(1).getLongOpt()).isEqualTo("vir");
        assertThat(sortedOptions.get(1).getOpt()).isEqualTo("v");
        assertThat(sortedOptions.get(1).isRequired()).isFalse();
    }

    @Test
    public void testGetOptions() {
        Options options1 = new Options();
        when(configBuilderFactory.createInstance(Options.class)).thenReturn(options1);
        assertThat(commandLineHelper.getOptions(TestConfig.class)).isEqualTo(options1);
        assertThat(options1.getOption("user").getLongOpt()).isEqualTo("user");
        assertThat(options1.getOption("user").getDescription()).isEqualTo("some static description string");
        assertThat(options1.getOption("vir").getOpt()).isEqualTo("v");
        assertThat(options1.getOption("vir").getDescription()).isEqualTo("some dynamically generated description");
    }
}
