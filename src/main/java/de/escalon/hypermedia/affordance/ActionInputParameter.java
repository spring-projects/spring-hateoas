package de.escalon.hypermedia.affordance;

import java.lang.annotation.Annotation;
import java.util.Map;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.action.Type;

/**
 * Interface to represent an input parameter to a resource handler method, independent of a particular ReST framework.
 * Created by Dietrich on 05.04.2015.
 */
public interface ActionInputParameter {

	/**
	 * Raw field value, without conversion.
	 *
	 * @return value
	 */
	Object getValue();

	/**
	 * Formatted field value to be used as preset value (e.g. using ConversionService).
	 *
	 * @return formatted value
	 */
	String getValueFormatted();

	/**
	 * Type of parameter when used in html-like contexts (e.g. Siren, Uber, XHtml)
	 *
	 * @return type
	 */
	Type getHtmlInputFieldType();

	/**
	 * Parameter is a complex body.
	 *
	 * @return true if applicable
	 */
	boolean isRequestBody();

	/**
	 * Parameter is a request header.
	 *
	 * @return true if applicable
	 */
	boolean isRequestHeader();

	/**
	 * Parameter is a query parameter.
	 *
	 * @return true if applicable
	 */
	boolean isRequestParam();

	/**
	 * Parameter is a path variable.
	 *
	 * @return true if applicable
	 */
	boolean isPathVariable();

	/**
	 * Gets request header name.
	 *
	 * @return name
	 */
	String getRequestHeaderName();

	/**
	 * Parameter has input constraints (like range, step etc.)
	 *
	 * @return true for input constraints
	 * @see #getInputConstraints()
	 */
	boolean hasInputConstraints();

	/**
	 * If the action input parameter is annotation-based, provide access to annotation
	 *
	 * @param annotation to look for
	 * @param <T> type of annotation
	 * @return annotation or null
	 */
	<T extends Annotation> T getAnnotation(Class<T> annotation);

	/**
	 * Property is hidden, e.g. according to {@link Input#hidden()}
	 *
	 * @param property name or property path
	 * @return true if hidden
	 */
	boolean isHidden(String property);

	/**
	 * Determines if request body input parameter has a read-only input property.
	 *
	 * @param property name or property path
	 * @return true if read-only
	 */
	boolean isReadOnly(String property);

	/**
	 * Determines if request body input parameter should be included. E.g. considering all of {@link Input#include}, {@link
	 * Input#hidden} and {@link Input#readOnly}.
	 *
	 * @param property name or property path
	 * @return true if included
	 */
	boolean isIncluded(String property);

	/**
	 * Checks if property should be excluded according to {@link Input#exclude()}.
	 *
	 * @param property name or property path
	 * @return true if excluded
	 */
	boolean isExcluded(String property);

	/**
	 * Gets possible values for this parameter.
	 *
	 * @param actionDescriptor in case that access to the other parameters is necessary to determine the possible values.
	 * @return possible values or empty array
	 */
	<T>Suggest<T>[] getPossibleValues(ActionDescriptor actionDescriptor);

	/**
	 * Parameter is an array or collection, think {?val*} in uri template.
	 *
	 * @return true for collection or array
	 */
	boolean isArrayOrCollection();

	/**
	 * Is this action input parameter required, based on the presence of a default value,
	 * the parameter annotations and the kind of input parameter.
	 *
	 * @return true if required
	 */
	boolean isRequired();

	/**
	 * If parameter is an array or collection, the default values.
	 *
	 * @return values
	 * @see #isArrayOrCollection()
	 */
	Object[] getValues();

	/**
	 * Does the parameter have a value?
	 *
	 * @return true if a value is present
	 */
	boolean hasValue();

	/**
	 * Name of parameter.
	 *
	 * @return
	 */
	String getParameterName();

	/**
	 * Type of parameter.
	 *
	 * @return
	 */
	Class<?> getParameterType();

	/**
	 * Generic type of parameter.
	 *
	 * @return generic type
	 */
	java.lang.reflect.Type getGenericParameterType();

	/**
	 * Gets input constraints.
	 *
	 * @return constraints where the key is one of {@link Input#MAX} etc. and the value is a string or number,
	 * depending on the input constraint.
	 * @see Input#MAX
	 * @see Input#MIN
	 * @see Input#MAX_LENGTH
	 * @see Input#MIN_LENGTH
	 * @see Input#STEP
	 * @see Input#PATTERN
	 * @see Input#READONLY
	 */
	Map<String, Object> getInputConstraints();
}
