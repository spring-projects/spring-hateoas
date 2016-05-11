package de.escalon.hypermedia.spring.halforms;

import java.util.List;

/**
 * Interface to mark classes that contains a list of {@link Template}
 */
interface TemplatesSupport {
	List<Template> getTemplates();
}
