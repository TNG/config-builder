package com.tngtech.configbuilder.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class EnumTypeExtractor {

    Stream<Class<? extends Enum<?>>> getEnumTypesRelevantFor(Type type) {
        if (type instanceof Class && ((Class<?>) type).isEnum()) {
            return Stream.of((Class<? extends Enum<?>>) type);
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            return stream(parameterizedType.getActualTypeArguments())
                    .flatMap(this::getEnumTypesRelevantFor)
                    .distinct();
        }
        return Stream.empty();
    }
}
