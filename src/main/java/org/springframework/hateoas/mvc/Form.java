package org.springframework.hateoas.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a method as a form producer method.
 * @author Dietrich Schulten
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Form {

	/** The form's name.  */
	String value();

}
