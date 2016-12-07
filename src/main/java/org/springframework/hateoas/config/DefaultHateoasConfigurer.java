package org.springframework.hateoas.config;

import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

/**
 * Default implementation of {@link HateoasConfigurer}.
 *
 * @author Alexander Morozov
 */
public class DefaultHateoasConfigurer implements HateoasConfigurer {

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Arrays.asList(MediaTypes.HAL_JSON);
    }

}
