package org.springframework.hateoas.core;

import org.springframework.hateoas.RelProvider;

import com.fasterxml.jackson.annotation.JsonRootName;

public class JsonRootRelProvider implements RelProvider {

    private DefaultRelProvider defaultRelProvider = new DefaultRelProvider();

    @Override
    public String getItemResourceRelFor(Class<?> type) {
        JsonRootName rootName = type.getAnnotation(JsonRootName.class);
        return rootName != null ? rootName.value() : defaultRelProvider.getCollectionResourceRelFor(type);
    }

    @Override
    public String getCollectionResourceRelFor(Class<?> type) {
        JsonRootName rootName = type.getAnnotation(JsonRootName.class);
        return rootName != null ? rootName.value() : defaultRelProvider.getCollectionResourceRelFor(type);
    }

    @Override
    public boolean supports(Class<?> delimiter) {
        return defaultRelProvider.supports(delimiter);
    }
}
