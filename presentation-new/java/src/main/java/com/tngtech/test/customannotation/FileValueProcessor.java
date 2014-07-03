package com.tngtech.test.customannotation;

import com.tngtech.configbuilder.annotation.valueextractor.ValueExtractorProcessor;
import com.tngtech.configbuilder.util.ConfigBuilderFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileValueProcessor implements ValueExtractorProcessor {
    public String getValue(Annotation ann,
                           ConfigBuilderFactory configBuilderFactory) {
        try {
            FileContentValue fileContentValue = (FileContentValue) ann;
            byte[] content = Files.readAllBytes(
                    Paths.get(fileContentValue.value()));
            return new String(content, fileContentValue.encoding());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}