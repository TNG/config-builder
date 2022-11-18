package com.tngtech.configbuilder.util;


import com.tngtech.configbuilder.annotation.validation.Validation;
import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.exception.ValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.lang.reflect.Method;
import java.util.Set;

public class ConfigValidator<T> {

    private final static Logger log = LoggerFactory.getLogger(ConfigValidator.class);

    private final ConfigBuilderFactory configBuilderFactory;
    private final ErrorMessageSetup errorMessageSetup;
    private final AnnotationHelper annotationHelper;

    public ConfigValidator(ConfigBuilderFactory miscFactory) {
        this.configBuilderFactory = miscFactory;
        this.annotationHelper = configBuilderFactory.getInstance(AnnotationHelper.class);
        this.errorMessageSetup = configBuilderFactory.getInstance(ErrorMessageSetup.class);
    }

    public void validate(T instanceOfConfigClass) {
        log.debug("validating instance of {}", instanceOfConfigClass.getClass());
        callValidationMethods(instanceOfConfigClass);
        callJSRValidation(instanceOfConfigClass);
    }

    private void callValidationMethods(T instanceOfConfigClass) {
        for (Method method : annotationHelper.getMethodsAnnotatedWith(instanceOfConfigClass.getClass(), Validation.class)) {
            try {
                method.setAccessible(true);
                method.invoke(instanceOfConfigClass);
            } catch (Exception e) {
                throw new ValidatorException(errorMessageSetup.getErrorMessage(e), e);
            }
        }
    }

    private void callJSRValidation(T instanceOfConfigClass) {
        ValidatorFactory factory = configBuilderFactory.getInstance(ValidatorFactory.class);
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(instanceOfConfigClass);
        if (!constraintViolations.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder(errorMessageSetup.getErrorMessage(ValidatorException.class) + "\n");
            for (ConstraintViolation<T> constraintViolation : constraintViolations) {
                stringBuilder.append(constraintViolation.getMessage());
            }
            throw new ValidatorException(stringBuilder.toString(), constraintViolations);
        }
    }
}
