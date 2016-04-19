package org.springframework.hateoas.forms;

import java.net.URI;

import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.LinkBuilder;

/**
 * Adds possibility of building {@link Template} instances
 * 
 */
public interface TemplateBuilder {

	/**
	 * Adds the given object's {@link String} representation as sub-resource to the current URI. Will unwrap
	 * {@link Identifiable}s to their id value (see {@link Identifiable#getId()}).
	 * 
	 * @param object
	 * @return
	 */
	TemplateBuilder slash(Object object);

	/**
	 * Adds the given {@link Identifiable}'s id as sub-resource. Will simply return the {@link LinkBuilder} as is if the
	 * given entity is {@literal null}.
	 * 
	 * @param identifiable
	 * @return
	 */
	TemplateBuilder slash(Identifiable<?> identifiable);

	/**
	 * Creates a URI of the link built by the current builder instance.
	 * 
	 * @return
	 */
	URI toUri();

	/**
	 * Creates the {@link Template} using the given key
	 * @param key
	 * @return
	 */
	Template withKey(String key);

	/**
	 * Creates the {@link Template} using the default key {@link Template#DEFAULT_KEY}
	 * @return
	 */
	Template withDefaultKey();

}
