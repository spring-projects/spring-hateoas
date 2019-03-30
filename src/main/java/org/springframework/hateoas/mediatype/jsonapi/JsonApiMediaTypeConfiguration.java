package org.springframework.hateoas.mediatype.jsonapi;

import com.fasterxml.jackson.databind.Module;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.MediaType;

import java.util.List;

public class JsonApiMediaTypeConfiguration implements HypermediaMappingInformation {
    @Override
    public List<MediaType> getMediaTypes() {
        return HypermediaType.JSON_API.getMediaTypes();
    }

    @Override
    public Module getJacksonModule() {
        return new Jackson2JsonApiModule();
    }
}
