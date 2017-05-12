/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas.affordance;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.affordance.formaction.Type;

/**
 * Interface to represent an input parameter to a resource handler method, independent of a particular ReST framework.
 *
 * @author Dietrich Schulten
 */
public interface ActionInputParameter {

	String MIN = "min";
	String MAX = "max";
	String STEP = "step";
	String MIN_LENGTH = "minLength";
	String MAX_LENGTH = "maxLength";
	String PATTERN = "pattern";
	String READONLY = "readonly";
	String EDITABLE = "editable";
	String REQUIRED = "required";

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
	 * Set the type of parameter when used in html-like contexts (e.g. Siren, Uber, XHtml)
	 *
	 * @return type
	 */
	void setHtmlInputFieldType(Type type);

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
	 * Gets possible values for this parameter.
	 *
	 * @param actionDescriptor in case that access to the other parameters is necessary to determine the possible values.
	 * @return possible values or empty array
	 */
	<T> List<Suggest<T>> getPossibleValues(ActionDescriptor actionDescriptor);

	Suggestions getSuggestions();

	/**
	 * Establish possible values for this parameter
	 * 
	 * @param possibleValues
	 */
	<T> void setPossibleValues(List<Suggest<T>> possibleValues);

	/**
	 * Retrieve the suggest type
	 * 
	 * @return
	 */
	SuggestType getSuggestType();

	/**
	 * Sets the suggest type
	 * 
	 * @param type
	 */
	void setSuggestType(SuggestType type);

	/**
	 * Parameter is an array or collection, think {?val*} in uri template.
	 *
	 * @return true for collection or array
	 */
	boolean isArrayOrCollection();

	/**
	 * Is this action input parameter required, bTPERased on the presence of a default value, the parameter annotations
	 * and the kind of input parameter.
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
	 * @return constraints where the key is one of {@link ActionInputParameter#MAX} etc. and the value is a string or
	 *         number, depending on the input constraint.
	 * @see ActionInputParameter#MAX
	 * @see ActionInputParameter#MIN
	 * @see ActionInputParameter#MAX_LENGTH
	 * @see ActionInputParameter#MIN_LENGTH
	 * @see ActionInputParameter#STEP
	 * @see ActionInputParameter#PATTERN
	 * @see ActionInputParameter#READONLY
	 */
	Map<String, Object> getInputConstraints();

	String getName();

	void setReadOnly(boolean readOnly);

	void setRequired(boolean required);

	ParameterType getType();

}
