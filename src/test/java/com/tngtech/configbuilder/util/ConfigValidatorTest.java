package com.tngtech.configbuilder.util;

import com.tngtech.configbuilder.annotation.validation.Validation;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.ValidatorException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConfigValidatorTest {

    private static class TestConfig {
        @Validation
        private void validate() {
            throw new RuntimeException();
        }
    }

    private ConfigValidator<TestConfig> configValidator;

    @Mock
    private ConfigBuilderFactory configBuilderFactory;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private TestConfig testConfig;
    @Mock
    private ConstraintViolation<TestConfig> constraintViolation1, constraintViolation2;
    @Mock
    private ErrorMessageSetup errorMessageSetup;
    @Mock
    private AnnotationHelper annotationHelper;

    @Before
    public void setUp() {
        when(configBuilderFactory.getInstance(AnnotationHelper.class)).thenReturn(annotationHelper);
        when(configBuilderFactory.getInstance(ErrorMessageSetup.class)).thenReturn(errorMessageSetup);
        when(configBuilderFactory.getInstance(ValidatorFactory.class)).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);

        configValidator = new ConfigValidator<>(configBuilderFactory);
    }

    @Test
    public void testValidateWithConstraintViolations() {
        when(validator.validate(testConfig)).thenReturn(newHashSet(constraintViolation1, constraintViolation2));
        when(errorMessageSetup.getErrorMessage(any(Class.class))).thenReturn("Validation found the following constraint violations:");

        assertThatThrownBy(() -> configValidator.validate(testConfig))
                .isInstanceOf(ValidatorException.class)
                .hasMessageContaining("Validation found the following constraint violations:");
    }

    @Test
    public void testCallValidationMethods() throws Exception {
        when(errorMessageSetup.getErrorMessage(any(Throwable.class))).thenReturn("InvocationTargetException");
        when(annotationHelper.getMethodsAnnotatedWith(TestConfig.class, Validation.class)).thenReturn(newHashSet(TestConfig.class.getDeclaredMethod("validate")));

        assertThatThrownBy(() -> configValidator.validate(new TestConfig()))
                .isInstanceOf(ValidatorException.class)
                .hasMessage("InvocationTargetException");
    }

    @Test
    public void testValidateWithoutConstraintViolations() {
        Set<ConstraintViolation<TestConfig>> constraintViolations = newHashSet();
        when(validator.validate(testConfig)).thenReturn(constraintViolations);

        configValidator.validate(testConfig);
    }
}
