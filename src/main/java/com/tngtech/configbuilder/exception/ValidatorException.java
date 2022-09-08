package com.tngtech.configbuilder.exception;

import com.google.common.collect.Sets;

import jakarta.validation.ConstraintViolation;
import java.util.Set;

public class ValidatorException extends RuntimeException {

    Set<ConstraintViolation<?>> constraintViolations;

    public <T> ValidatorException(String message, Set<ConstraintViolation<T>> constraintViolations) {
        super(message);
        this.constraintViolations = Sets.newHashSet(constraintViolations);
    }

    public ValidatorException(String message, Throwable e) {
        super(message, e);
    }
}
