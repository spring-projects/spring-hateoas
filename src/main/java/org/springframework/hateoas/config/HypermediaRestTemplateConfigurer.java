package org.springframework.hateoas.config;

import org.springframework.web.client.RestTemplate;

/**
 * Assembles hypermedia-based message converters and applies them to an existing {@link RestTemplate}.
 *
 * @author Greg Turnquist
 * @since 1.1
 */
public class HypermediaRestTemplateConfigurer {

	private final WebConverters converters;

    /**
     * Creates a new {@link HypermediaRestTemplateConfigurer} using the {@link WebConverters}.
     *
     * @param converters
     */
	HypermediaRestTemplateConfigurer(WebConverters converters) {
		this.converters = converters;
	}

	/**
	 * Insert hypermedia-aware message converters in front of any other existing message converters.
	 *
	 * @param template
	 * @return {@link RestTemplate} capable of speaking hypermedia.
	 */
	public RestTemplate registerHypermediaTypes(RestTemplate template) {

		template.setMessageConverters(converters.and(template.getMessageConverters()));
		return template;
	}
}
