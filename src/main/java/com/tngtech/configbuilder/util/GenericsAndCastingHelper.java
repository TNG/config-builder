package com.tngtech.configbuilder.util;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class GenericsAndCastingHelper {

    private final Map<Class<?>, Class<?>> primitiveToWrapperMapping = ImmutableMap.<Class<?>, Class<?>>builder()
            .put(boolean.class, Boolean.class)
            .put(byte.class, Byte.class)
            .put(short.class, Short.class)
            .put(char.class, Character.class)
            .put(int.class, Integer.class)
            .put(long.class, Long.class)
            .put(float.class, Float.class)
            .put(double.class, Double.class)
            .build();

    public Class<?> getWrapperClassIfPrimitive(Class clazz) {
        return primitiveToWrapperMapping.get(clazz) == null? clazz : primitiveToWrapperMapping.get(clazz);
    }

    public Class<?> castTypeToClass(Type type) {
        if(type.getClass().equals(Class.class)) {
            return (Class<?>) type;
        } else {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }
    }

    public boolean typesMatch(Object sourceValue, Type targetType) {
        if(sourceValue == null) {
            return true;
        }
        Class<?> sourceClass = getWrapperClassIfPrimitive(sourceValue.getClass());
        if(targetType.getClass().equals(Class.class)) {
            return getWrapperClassIfPrimitive((Class<?>) targetType).isAssignableFrom(sourceClass);
        }
        else if(Collection.class.isAssignableFrom((Class<?>)((ParameterizedType)targetType).getRawType())) {
            if(Collection.class.isAssignableFrom(sourceClass)) {
                Class<?> typeArgument = (Class<?>)((ParameterizedType) targetType).getActualTypeArguments()[0];
                for(Object item : (Collection)sourceValue) {
                    if(!typeArgument.isAssignableFrom(item.getClass())) {
                        return false;
                    }
                }
                return true;
            }
            else {
                return false;
            }
        }
        return (castTypeToClass(targetType)).isAssignableFrom(sourceClass);
    }

    public boolean isPrimitiveOrWrapper(Class targetClass) {
        return primitiveToWrapperMapping.containsKey(targetClass) || primitiveToWrapperMapping.containsValue(targetClass);
    }

    public <E extends Enum<E>> Optional<Class<E>> getEnumTypeOrParameterIfApplicable(Field field) {
        if (field.getType().isEnum()) {
            return Optional.of((Class<E>) field.getType());
        }

        if (!Collection.class.isAssignableFrom(field.getType())) {
            return Optional.empty();
        }

        Type collectionParameterType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        return collectionParameterType instanceof Class && ((Class<?>) collectionParameterType).isEnum()
               ? Optional.of((Class<E>) collectionParameterType)
               : Optional.empty();
    }
}
