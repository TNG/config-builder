package com.tngtech.configbuilder.annotation.typetransformer;

import com.tngtech.configbuilder.util.ConfigBuilderFactory;
import com.tngtech.configbuilder.util.FieldValueTransformer;
import com.tngtech.configbuilder.util.GenericsAndCastingHelper;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StringCollectionToCommaSeparatedStringTransformerTest {

    private final StringCollectionToCommaSeparatedStringTransformer transformer = new StringCollectionToCommaSeparatedStringTransformer();

    @Mock
    private FieldValueTransformer fieldValueTransformer;
    @Mock
    private ConfigBuilderFactory configBuilderFactory;

    @Test
    public void testTransformer() {
        Collection<String> collection = newArrayList("Rakim", "Lakim Shabazz", "2Pac");

        transformer.initialize(fieldValueTransformer, configBuilderFactory, ",");
        String actualResult = transformer.transform(collection);
        assertThat(actualResult).isEqualTo("Rakim,Lakim Shabazz,2Pac");

        transformer.initialize(fieldValueTransformer, configBuilderFactory, ";");
        actualResult = transformer.transform(collection);
        assertThat(actualResult).isEqualTo("Rakim;Lakim Shabazz;2Pac");
    }

    @Test
    public void testIsMatching() {
        initializeFactoryAndHelperMocks();
        transformer.initialize(fieldValueTransformer, configBuilderFactory, ",");
        assertThat(transformer.isMatching(Set.class, String.class)).isTrue();
        assertThat(transformer.isMatching(Collection.class, String.class)).isTrue();
        assertThat(transformer.isMatching(Object.class, String.class)).isFalse();
    }

    private void initializeFactoryAndHelperMocks(){
        when(configBuilderFactory.getInstance(GenericsAndCastingHelper.class)).thenReturn(new GenericsAndCastingHelper());
    }
}
