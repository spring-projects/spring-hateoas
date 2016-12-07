package org.springframework.hateoas.config;

import org.springframework.http.MediaType;

import java.util.List;

/**
 * Configures HATEOAS environment.
 *
 * @author Alexander Morozov
 * @see DefaultHateoasConfigurer
 */
public interface HateoasConfigurer {

    /**
     * @return list of supported {@link MediaType}
     */
    public List<MediaType> getSupportedMediaTypes();

}
