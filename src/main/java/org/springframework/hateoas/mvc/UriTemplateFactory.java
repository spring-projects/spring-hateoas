package org.springframework.hateoas.mvc;

import org.springframework.web.util.UriTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds and caches UriTemplates.
 */
class UriTemplateFactory {
	private Map<String, UriTemplate> templateCache = new ConcurrentHashMap<String, UriTemplate>();
	UriTemplate templateFor(String mapping) {
		if (templateCache.containsKey(mapping)) {
			return templateCache.get(mapping);
		} else {
			UriTemplate template = new UriTemplate(mapping);
			templateCache.put(mapping, template);
			return template;
		}
	}
}
