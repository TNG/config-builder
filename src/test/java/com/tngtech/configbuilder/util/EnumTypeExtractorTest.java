package com.tngtech.configbuilder.util;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumTypeExtractorTest {

    enum TestEnum1 {}

    enum TestEnum2 {}

    public static class TestClass {

        String simpleNonEnumField;
        Map<Integer, String> nonEnumParameterizedField;

        TestEnum1 simpleEnumField;
        Map<TestEnum1, TestEnum2> fieldWithEnumTypeParameters;
        Map<TestEnum1, List<TestEnum2>> fieldWithNestedEnumTypeParameters;
        Map<TestEnum1, Map<TestEnum1, TestEnum1>> fieldWithRepeatedEnumTypeParameter;
    }

    private final EnumTypeExtractor enumTypeExtractor = new EnumTypeExtractor();

    @Test
    public void testExtractionOnSimpleNonEnumType() throws Exception {
        Field simpleNonEnumField = TestClass.class.getDeclaredField("simpleNonEnumField");
        assertThat(enumTypeExtractor.getEnumTypesRelevantFor(simpleNonEnumField.getGenericType()))
                .isEmpty();
    }

    @Test
    public void testExtractionOnNonEnumParameterizedType() throws Exception {
        Field nonEnumParameterizedField = TestClass.class.getDeclaredField("nonEnumParameterizedField");
        assertThat(enumTypeExtractor.getEnumTypesRelevantFor(nonEnumParameterizedField.getGenericType()))
                .isEmpty();
    }

    @Test
    public void testExtractionOfSimpleEnumType() throws Exception {
        Field simpleEnumField = TestClass.class.getDeclaredField("simpleEnumField");
        assertThat(enumTypeExtractor.getEnumTypesRelevantFor(simpleEnumField.getGenericType()))
                .containsExactly(TestEnum1.class);
    }

    @Test
    public void testExtractionOfEnumTypeParameters() throws Exception {
        Field fieldWithEnumTypeParameters = TestClass.class.getDeclaredField("fieldWithEnumTypeParameters");
        assertThat(enumTypeExtractor.getEnumTypesRelevantFor(fieldWithEnumTypeParameters.getGenericType()))
                .containsExactlyInAnyOrder(TestEnum1.class, TestEnum2.class);
    }

    @Test
    public void testExtractionOfNestedEnumTypeParameters() throws Exception {
        Field fieldWithNestedEnumTypeParameters = TestClass.class.getDeclaredField("fieldWithNestedEnumTypeParameters");
        assertThat(enumTypeExtractor.getEnumTypesRelevantFor(fieldWithNestedEnumTypeParameters.getGenericType()))
                .containsExactlyInAnyOrder(TestEnum1.class, TestEnum2.class);
    }

    @Test
    public void testExtractedEnumTypesAreDeduplicated() throws Exception {
        Field fieldWithRepeatedEnumTypeParameter = TestClass.class.getDeclaredField("fieldWithRepeatedEnumTypeParameter");
        assertThat(enumTypeExtractor.getEnumTypesRelevantFor(fieldWithRepeatedEnumTypeParameter.getGenericType()))
                .containsExactly(TestEnum1.class);
    }
}
