package org.springframework.hateoas.forms;

import org.springframework.hateoas.LinkBuilder;

/**
 * Extends {@link LinkBuilder} adding de possibility of building {@link Form} instances
 * 
 */
public interface FormBuilder extends LinkBuilder {

	/**
	 * Creates the {@link Form} using the given key
	 * @param key
	 * @return
	 */
	Form withKey(String key);

	/**
	 * Creates the {@link Form} using the default key {@link Template#DEFAULT_KEY}
	 * @return
	 */
	Form withDefaultKey();

}
