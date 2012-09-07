package org.springframework.hateoas;

import java.net.URI;

/**
 * Builder to ease building {@link Link} instances.
 * 
 * @author Ricardo Gladwell
 */
public interface LinkBuilder {

	/**
	 * Adds the given object's {@link String} representation as sub-resource to the current URI.
	 * 
	 * @param object
	 * @return
	 */
	LinkBuilder slash(Object object);

	/**
	 * Adds the given {@link AbstractEntity}'s id as sub-resource.
	 * 
	 * @param identifiable
	 * @return
	 */
	LinkBuilder slash(Identifiable<?> identifiable);

	/**
	 * Creates a URI of the link built by the current builder instance.
	 * 
	 * @return
	 */
	URI toUri();

	/**
	 * Creates the {@link Link} built by the current builder instance with the given rel.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	Link withRel(String rel);

	/**
	 * Creates the {@link Link} built by the current builder instance with the default self rel.
	 * 
	 * @see Link#REL_SELF
	 * @return
	 */
	Link withSelfRel();

}
