package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLoaderConfigurationAnnotation;
import com.tngtech.configbuilder.annotation.typetransformer.StringCollectionToCommaSeparatedStringTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;
import com.tngtech.configbuilder.annotation.valueextractor.ValueExtractorAnnotation;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AnnotationHelperTest {

    public class TestConfig {

        @PropertyValue("testField")
        @CommandLineValue(shortOpt = "t", longOpt = "testField")
        @TypeTransformers({StringCollectionToCommaSeparatedStringTransformer.class})
        private Collection<String> testField;
    }

    private final AnnotationHelper annotationHelper = new AnnotationHelper();
    private Field field;
    private final Class<? extends Annotation>[] annotationOrder = new Class[]{CommandLineValue.class, PropertyValue.class};

    @BeforeEach
    public void setUp() throws Exception {
        field = TestConfig.class.getDeclaredField("testField");
    }

    @Test
    public void testGetAnnotationsAnnotatedWith() {
        List<Annotation> result = annotationHelper.getAnnotationsAnnotatedWith(field.getDeclaredAnnotations(), ValueExtractorAnnotation.class);
        assertThat(result)
                .contains(field.getAnnotation(CommandLineValue.class), field.getAnnotation(PropertyValue.class))
                .doesNotContain(field.getAnnotation(TypeTransformers.class));
    }

    @Test
    public void testGetAnnotationsInOrder() {
        List<Annotation> result = annotationHelper.getAnnotationsInOrder(field, annotationOrder);
        assertThat(result).containsExactly(field.getAnnotation(CommandLineValue.class), field.getAnnotation(PropertyValue.class));
    }

    @Test
    public void testGetFieldsAnnotatedWith() {
        assertThat(annotationHelper.getFieldsAnnotatedWith(TestConfig.class, PropertyValue.class)).contains(field);
    }

    @Test
    public void testFieldHasAnnotation() {
        assertThat(annotationHelper.fieldHasAnnotationAnnotatedWith(field, ValueExtractorAnnotation.class)).isTrue();
        assertThat(annotationHelper.fieldHasAnnotationAnnotatedWith(field, PropertyLoaderConfigurationAnnotation.class)).isFalse();
    }
}
