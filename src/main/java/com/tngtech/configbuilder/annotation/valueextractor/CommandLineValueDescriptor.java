package com.tngtech.configbuilder.annotation.valueextractor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a method as description text supplier for command line options.
 * The annotated method must be static and accept a single String parameter, which is the longOpt name of a command line option.
 * There may be at most one such method per class.<br>
 * If a field is annotated with {@link com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue} but has no description,
 * then this method is called to generate the description text.<br>
 * <b>Usage:</b> <code>@CommandLineValueDescriptor</code>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandLineValueDescriptor {}
