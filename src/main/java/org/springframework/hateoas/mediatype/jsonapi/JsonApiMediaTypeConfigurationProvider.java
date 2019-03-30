package org.springframework.hateoas.mediatype.jsonapi;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.config.MediaTypeConfigurationProvider;
import org.springframework.http.MediaType;

import java.util.Collection;

public class JsonApiMediaTypeConfigurationProvider implements MediaTypeConfigurationProvider {
    @Override
    public Class<? extends HypermediaMappingInformation> getConfiguration() {
        return JsonApiMediaTypeConfiguration.class;
    }

    @Override
    public boolean supportsAny(Collection<MediaType> mediaTypes) {
        return mediaTypes.contains(MediaTypes.JSON_API);
    }
}
