package org.springframework.hateoas.mvc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FormAction {

	/**
	 * The name of the form which points to this method. Only required if there is more than one {@link FormAction} annotation
	 * in a controller class.
	 */

	String formName() default "";

}
