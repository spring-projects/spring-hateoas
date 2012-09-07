package org.springframework.hateoas;

import org.springframework.hateoas.mvc.ControllerLinkBuilder;

/**
 * Factory for {@link LinkBuilder} instances.
 * 
 * @author Ricardo Gladwell
 */
public interface LinkBuilderFactory {

	/**
	 * Creates a new {@link LinkBuilder} with a base of the mapping annotated to the given target clas (controller,
	 * service, etc.).
	 * 
	 * @param target
	 * @return
	 */
	LinkBuilder linkTo(Class<?> target);

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated  to the given target class
	 * (controller, service, etc.). The additional parameters are used to fill up potentially available path variables
	 * in the class scope request mapping.
	 * 
	 * @param target
	 * @param parameters
	 * @return
	 */
	LinkBuilder linkTo(Class<?> controller, Object... parameters);

}
