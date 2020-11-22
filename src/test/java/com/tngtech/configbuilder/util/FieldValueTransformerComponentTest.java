package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.annotation.typetransformer.*;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.PrimitiveParsingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.tngtech.configbuilder.util.FieldValueTransformerComponentTest.TestEnum.BAR;
import static com.tngtech.configbuilder.util.FieldValueTransformerComponentTest.TestEnum.FOO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FieldValueTransformerComponentTest {

    public static class TestTransformer extends TypeTransformer<String, Integer> {
        @Override
        public Integer transform(String argument) {
            return 1472;
        }
    }

    enum TestEnum {
        FOO, BAR
    }

    private static class TestConfigClass {

        @TypeTransformers({CharacterSeparatedStringToStringListTransformer.class})
        private Collection<String> stringCollectionField;
        private int intField;
        private double doubleField;
        private Boolean boolField;
        @TypeTransformers({TestTransformer.class})
        private int otherIntField;
        private Collection<Path> pathCollectionField;
        private Collection<Integer> integerCollectionField;
        private Collection<Object> objectCollectionField;
        private TestEnum enumField;
        private List<TestEnum> enumListField;
        private Set<TestEnum> enumSetField;
    }

    @Mock
    private ConfigBuilderFactory configBuilderFactory;
    @Mock
    private ErrorMessageSetup errorMessageSetup;

    private FieldValueTransformer fieldValueTransformer;

    private Field stringCollectionField;
    private Field intField;
    private Field doubleField;
    private Field boolField;
    private Field otherIntField;
    private Field pathCollectionField;
    private Field integerCollectionField;
    private Field objectCollectionField;
    private Field enumField;
    private Field enumListField;
    private Field enumSetField;

    @Before
    public void setUp() throws Exception {
        when(configBuilderFactory.getInstance(ErrorMessageSetup.class)).thenReturn(errorMessageSetup);
        when(configBuilderFactory.getInstance(GenericsAndCastingHelper.class)).thenReturn(new GenericsAndCastingHelper());
        when(configBuilderFactory.getInstance(EnumTypeExtractor.class)).thenReturn(new EnumTypeExtractor());

        when(configBuilderFactory.getInstance(CharacterSeparatedStringToStringListTransformer.class)).thenReturn(new CharacterSeparatedStringToStringListTransformer());
        when(configBuilderFactory.getInstance(CharacterSeparatedStringToStringSetTransformer.class)).thenReturn(new CharacterSeparatedStringToStringSetTransformer());
        when(configBuilderFactory.getInstance(StringCollectionToCommaSeparatedStringTransformer.class)).thenReturn(new StringCollectionToCommaSeparatedStringTransformer());
        when(configBuilderFactory.getInstance(StringToPathTransformer.class)).thenReturn(new StringToPathTransformer());
        when(configBuilderFactory.getInstance(CollectionToArrayListTransformer.class)).thenReturn(new CollectionToArrayListTransformer());
        when(configBuilderFactory.getInstance(CollectionToHashSetTransformer.class)).thenReturn(new CollectionToHashSetTransformer());
        when(configBuilderFactory.getInstance(StringOrPrimitiveToPrimitiveTransformer.class)).thenReturn(new StringOrPrimitiveToPrimitiveTransformer());
        when(configBuilderFactory.getInstance(TestTransformer.class)).thenReturn(new TestTransformer());

        this.fieldValueTransformer = new FieldValueTransformer(configBuilderFactory);

        stringCollectionField = TestConfigClass.class.getDeclaredField("stringCollectionField");
        intField = TestConfigClass.class.getDeclaredField("intField");
        boolField = TestConfigClass.class.getDeclaredField("boolField");
        otherIntField = TestConfigClass.class.getDeclaredField("otherIntField");
        pathCollectionField = TestConfigClass.class.getDeclaredField("pathCollectionField");
        integerCollectionField = TestConfigClass.class.getDeclaredField("integerCollectionField");
        doubleField = TestConfigClass.class.getDeclaredField("doubleField");
        objectCollectionField = TestConfigClass.class.getDeclaredField("objectCollectionField");
        enumField = TestConfigClass.class.getDeclaredField("enumField");
        enumListField = TestConfigClass.class.getDeclaredField("enumListField");
        enumSetField = TestConfigClass.class.getDeclaredField("enumSetField");
    }

    @Test
    public void testTransformingStringToStringCollection() {
        Object actualResult = fieldValueTransformer.transformFieldValue(stringCollectionField, "Alpha,Beta,Gamma");
        assertThat(actualResult).isEqualTo(newArrayList("Alpha", "Beta", "Gamma"));
    }

    @Test
    public void testTransformingStringToInt() {
        Object actualResult = fieldValueTransformer.transformFieldValue(intField, "17");
        assertThat(actualResult).isEqualTo(17);
    }

    @Test
    public void testTransformingIntegerToDouble() {
        Object actualResult = fieldValueTransformer.transformFieldValue(doubleField, 17);
        assertThat(actualResult).isEqualTo(17.0);
    }

    @Test(expected = PrimitiveParsingException.class)
    public void testExceptionIfValueCannotBeParsedToBoolean() {
        fieldValueTransformer.transformFieldValue(boolField, 38.7);
    }

    @Test
    public void testWhenNoTransformerNecessary() {
        Object actualResult = fieldValueTransformer.transformFieldValue(intField, 197);
        assertThat(actualResult).isEqualTo(197);
    }

    @Test
    public void testThatTransformersInAnnotationArePrioritized() {
        Object actualResult = fieldValueTransformer.transformFieldValue(otherIntField, "2");
        assertThat(actualResult).isEqualTo(1472);
    }

    @Test
    public void testTransformingStringToPathCollection() {
        Object actualResult = fieldValueTransformer.transformFieldValue(pathCollectionField, "/etc,/usr");
        assertThat(actualResult).isEqualTo(newArrayList(Paths.get("/etc"), Paths.get("/usr")));
    }

    @Test
    public void testTransformingStringToIntegerCollection() {
        Object actualResult = fieldValueTransformer.transformFieldValue(integerCollectionField, "3,4");
        assertThat(actualResult).isEqualTo(newArrayList(3, 4));
    }

    @Test
    public void testTransformingStringToObjectCollection() {
        Object actualResult = fieldValueTransformer.transformFieldValue(objectCollectionField, "someString,anotherString");
        assertThat(actualResult).isEqualTo(newArrayList("someString", "anotherString"));
    }

    @Test
    public void testTransformingStringToEnum() {
        Object actualResult = fieldValueTransformer.transformFieldValue(enumField, "FOO");
        assertThat(actualResult).isEqualTo(FOO);
    }

    @Test
    public void testTransformingStringToEnumList() {
        Object actualResult = fieldValueTransformer.transformFieldValue(enumListField, "FOO, BAR, FOO");
        assertThat(actualResult).isEqualTo(newArrayList(FOO, BAR, FOO));
    }

    @Test
    public void testTransformingStringToEnumSet() {
        Object actualResult = fieldValueTransformer.transformFieldValue(enumSetField, "BAR, FOO");
        assertThat(actualResult).isEqualTo(EnumSet.of(BAR, FOO));
    }

    @Test
    public void testThatValueTransformerIgnoresNull() {
        Object actualResult = fieldValueTransformer.transformFieldValue(pathCollectionField, null);
        assertThat(actualResult).isNull();
    }
}
