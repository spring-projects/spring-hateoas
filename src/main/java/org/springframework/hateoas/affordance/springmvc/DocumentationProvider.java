package org.springframework.hateoas.affordance.springmvc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.hateoas.affordance.ActionInputParameter;

/**
 * Provides documentation urls for the given elements.
 * 
 * @author Dietrich Schulten
 */
public interface DocumentationProvider {

	/**
	 * Gets documentationUrl for given parameter.
	 *
	 * @param actionInputParameter to document
	 * @param content current value
	 * @return url or null
	 */
	String getDocumentationUrl(ActionInputParameter actionInputParameter, Object content);

	/**
	 * Gets documentationUrl for given field.
	 *
	 * @param field to document
	 * @param content current value
	 * @return url or null
	 */
	String getDocumentationUrl(Field field, Object content);

	/**
	 * Gets documentationUrl for given method.
	 *
	 * @param method to document
	 * @param content current value
	 * @return url or null
	 */
	String getDocumentationUrl(Method method, Object content);

	/**
	 * Gets documentationUrl for given class.
	 *
	 * @param clazz to document
	 * @param content current value
	 * @return url or null
	 */
	String getDocumentationUrl(Class<?> clazz, Object content);

	/**
	 * Gets documentationUrl for given attribute name.
	 *
	 * @param name to document
	 * @param content current value
	 * @return url or null
	 */
	String getDocumentationUrl(String name, Object content);
}
