package org.springframework.hateoas.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.client.RestTemplate;

/**
 * Assembles hypermedia-based message converters and applies them to an existing {@link RestTemplate}.
 *
 * @author Greg Turnquist
 * @since 1.1
 */
public class HypermediaRestTemplateConfigurer {

	private final ObjectProvider<WebConverters> converters;

	HypermediaRestTemplateConfigurer(ObjectProvider<WebConverters> converters) {
		this.converters = converters;
	}

    /**
     * Insert hypermedia-aware message converters to the front of the stack.
     *
     * @param template
     * @return
     */
	public RestTemplate registerHypermediaTypes(RestTemplate template) {

		template.setMessageConverters(converters.getObject().and(template.getMessageConverters()));
		return template;
	}
}
