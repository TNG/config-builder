package com.tngtech.configbuilder.annotation.typetransformer;

public class StringToEnumTypeTransformer<E extends Enum<E>> extends TypeTransformer<String, E> {

    private final Class<E> enumClass;

    public StringToEnumTypeTransformer(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public boolean isMatching(Class<?> sourceClass, Class<?> targetClass) {
        return sourceClass.equals(String.class) && targetClass.equals(enumClass);
    }

    @Override
    public E transform(final String value) {
        return E.valueOf(enumClass, value.trim().replace(' ', '_').toUpperCase());
    }
}
