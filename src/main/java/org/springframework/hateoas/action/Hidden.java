package org.springframework.hateoas.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to mark a method parameter as Hidden, e.g. when used as a POST parameter for a form which is not supposed to
 * be changed by the client.
 * 
 * @author Dietrich Schulten
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Hidden {

}
