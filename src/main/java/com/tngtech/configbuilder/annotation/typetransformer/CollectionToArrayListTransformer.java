package com.tngtech.configbuilder.annotation.typetransformer;


import com.google.common.collect.Lists;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;

public class CollectionToArrayListTransformer extends TypeTransformer<Collection, ArrayList> {

    @Override
    public ArrayList transform(Collection argument) {
        ArrayList result = Lists.newArrayList();
        for (Object value : argument) {
            result.add(fieldValueTransformer.performNecessaryTransformations(value, ((ParameterizedType) targetType).getActualTypeArguments()[0]));
        }
        return result;
    }
}
