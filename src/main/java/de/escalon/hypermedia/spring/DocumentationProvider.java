package de.escalon.hypermedia.spring;

import de.escalon.hypermedia.affordance.ActionInputParameter;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Provides documentation urls for the given elements.
 *
 * Created by Dietrich on 04.04.2015.
 */
public interface DocumentationProvider {

    /**
     * Gets documentationUrl for given parameter.
     * @param actionInputParameter to document
     * @param content current value
     * @return url or null
     */
    String getDocumentationUrl(ActionInputParameter actionInputParameter, Object content);

    /**
     * Gets documentationUrl for given field.
     * @param field to document
     * @param content current value
     * @return url or null
     */
    String getDocumentationUrl(Field field, Object content);

    /**
     * Gets documentationUrl for given method.
     * @param method to document
     * @param content current value
     * @return url or null
     */
    String getDocumentationUrl(Method method, Object content);

    /**
     * Gets documentationUrl for given class.
     * @param clazz to document
     * @param content current value
     * @return url or null
     */
    String getDocumentationUrl(Class clazz, Object content);

    /**
     * Gets documentationUrl for given attribute name.
     * @param name to document
     * @param content current value
     * @return url or null
     */
    String getDocumentationUrl(String name, Object content);
}
