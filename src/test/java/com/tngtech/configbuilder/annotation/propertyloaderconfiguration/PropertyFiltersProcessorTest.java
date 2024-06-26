package com.tngtech.configbuilder.annotation.propertyloaderconfiguration;

import com.tngtech.propertyloader.PropertyLoader;
import com.tngtech.propertyloader.impl.DefaultPropertyFilterContainer;
import com.tngtech.propertyloader.impl.filters.DecryptingFilter;
import com.tngtech.propertyloader.impl.filters.EnvironmentResolvingFilter;
import com.tngtech.propertyloader.impl.filters.ThrowIfPropertyHasToBeDefined;
import com.tngtech.propertyloader.impl.filters.ValueModifyingFilter;
import com.tngtech.propertyloader.impl.filters.VariableResolvingFilter;
import com.tngtech.propertyloader.impl.filters.WarnOnSurroundingWhitespace;
import com.tngtech.propertyloader.impl.interfaces.PropertyLoaderFilter;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PropertyFiltersProcessorTest {

    private static class TestPropertyFilter extends ValueModifyingFilter {
        @Override
        protected String filterValue(String key, String value, Properties properties) {
            return null;
        }
    }

    @Mock
    private PropertyFilters propertyFilters;
    @Mock
    private DefaultPropertyFilterContainer filterContainer;
    @Mock
    private PropertyLoader propertyLoader;

    private final PropertyFiltersProcessor propertyFiltersProcessor = new PropertyFiltersProcessor();

    @BeforeEach
    public void setUp() {
        when(propertyLoader.getFilters()).thenReturn(filterContainer);
    }

    @Test
    public void testAnnotationWithNoValues() {
        @SuppressWarnings("unchecked") Class<? extends PropertyLoaderFilter>[] classes = new Class[0];
        when(propertyFilters.value()).thenReturn(classes);

        propertyFiltersProcessor.configurePropertyLoader(propertyFilters, propertyLoader);

        verify(filterContainer).clear();
    }

    @Test
    public void testAnnotationWithSomeValues() {
        @SuppressWarnings("unchecked") Class<? extends PropertyLoaderFilter>[] classes = new Class[]{
                VariableResolvingFilter.class,
                DecryptingFilter.class,
                EnvironmentResolvingFilter.class,
                WarnOnSurroundingWhitespace.class,
                ThrowIfPropertyHasToBeDefined.class
        };
        when(propertyFilters.value()).thenReturn(classes);

        propertyFiltersProcessor.configurePropertyLoader(propertyFilters, propertyLoader);

        InOrder order = inOrder(filterContainer);
        order.verify(filterContainer).clear();
        order.verify(filterContainer).withVariableResolvingFilter();
        order.verify(filterContainer).withDecryptingFilter();
        order.verify(filterContainer).withEnvironmentResolvingFilter();
        order.verify(filterContainer).withWarnOnSurroundingWhitespace();
        order.verify(filterContainer).withWarnIfPropertyHasToBeDefined();
        order.verifyNoMoreInteractions();
    }

    @Test
    public void testAnnotationWithUnknownValuesShouldThrowException() {
        @SuppressWarnings("unchecked") Class<? extends PropertyLoaderFilter>[] classes = new Class[]{TestPropertyFilter.class};
        when(propertyFilters.value()).thenReturn(classes);

        assertThatThrownBy(() -> propertyFiltersProcessor.configurePropertyLoader(propertyFilters, propertyLoader))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("unhandled filter class TestPropertyFilter");
    }
}
