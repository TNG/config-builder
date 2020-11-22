package com.tngtech.configbuilder.annotation.typetransformer;

import com.tngtech.configbuilder.configuration.ErrorMessageSetup;
import com.tngtech.configbuilder.util.GenericsAndCastingHelper;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;
import com.tngtech.configbuilder.util.FieldValueTransformer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementations of this interface transform an object into a different type of object
 *
 * @param <SourceClass> the type of the parameter before the transformation
 * @param <TargetClass> return type
 */
public abstract class TypeTransformer<SourceClass, TargetClass> {

    protected Type targetType;
    protected FieldValueTransformer fieldValueTransformer;
    protected GenericsAndCastingHelper genericsAndCastingHelper;
    protected ErrorMessageSetup errorMessageSetup;
    protected Object[] additionalOptions;

    public abstract TargetClass transform(SourceClass argument);

    public void initialize(FieldValueTransformer fieldValueTransformer, ConfigBuilderFactory configBuilderFactory, Object... additionalOptions) {
        this.fieldValueTransformer = fieldValueTransformer;
        this.genericsAndCastingHelper = configBuilderFactory.getInstance(GenericsAndCastingHelper.class);
        this.errorMessageSetup = configBuilderFactory.getInstance(ErrorMessageSetup.class);
        this.additionalOptions = additionalOptions;
    }

    public boolean isMatching(Class<?> sourceClass, Class<?> targetClass) {
        return getTransformerSourceClass().isAssignableFrom(sourceClass) &&
                targetClass.isAssignableFrom(getTransformerTargetClass());
    }

    protected Class<?> getTransformerSourceClass() {
        Type[] genericTypes = determineTypeArguments();
        return genericsAndCastingHelper.castTypeToClass(genericTypes[0]);
    }

    protected Class<?> getTransformerTargetClass() {
        Type[] genericTypes = determineTypeArguments();
        return genericsAndCastingHelper.castTypeToClass(genericTypes[1]);
    }

    private Type[] determineTypeArguments() {
        Class<?> clazz = getClass();
        Type[] typeArguments = new Type[]{};

        while (!clazz.equals(TypeTransformer.class)) {
            TypeVariable[] typeParameters = clazz.getTypeParameters();

            Map<TypeVariable, Type> typeVariableMap = buildTypeNameMap(typeArguments, typeParameters);

            typeArguments = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();

            replaceKnownTypes(typeArguments, typeVariableMap);

            clazz = clazz.getSuperclass();
        }
        return typeArguments;
    }

    private Map<TypeVariable, Type> buildTypeNameMap(Type[] typeArguments, TypeVariable[] typeParameters) {
        Map<TypeVariable, Type> typeVariableMap = new HashMap<>();
        for (int i = 0; i < typeParameters.length; i++) {
            typeVariableMap.put(typeParameters[i], typeArguments[i]);
        }
        return typeVariableMap;
    }

    private void replaceKnownTypes(Type[] typeArguments, Map<TypeVariable, Type> typeVariableMap) {
        for (int i = 0; i < typeArguments.length; i++) {
            if (typeArguments[i] instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) typeArguments[i];
                if (typeVariableMap.containsKey(typeVariable)) {
                    typeArguments[i] = typeVariableMap.get(typeVariable);
                }
            }
        }
    }

    public void setTargetType(Type targetType) {
        this.targetType = targetType;
    }
}
