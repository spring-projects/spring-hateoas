/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.action.Options;
import de.escalon.hypermedia.action.Select;
import de.escalon.hypermedia.action.Type;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.DataType;
import de.escalon.hypermedia.affordance.SimpleSuggest;
import de.escalon.hypermedia.affordance.Suggest;
import de.escalon.hypermedia.affordance.SuggestType;

/**
 * Describes a Spring MVC rest services method parameter value with recorded sample call value and input constraints.
 *
 * @author Dietrich Schulten
 */
public class SpringActionInputParameter implements ActionInputParameter {

	private static final String[] EMPTY = new String[0];
	private static final Suggest<?>[] EMPTY_SUGGEST = new Suggest<?>[0];
	private final TypeDescriptor typeDescriptor;
	private final RequestBody requestBody;
	private final RequestParam requestParam;
	private final PathVariable pathVariable;
	private final RequestHeader requestHeader;
	private final MethodParameter methodParameter;
	private final Object value;
	private Boolean arrayOrCollection = null;
	private final Map<String, Object> inputConstraints = new HashMap<String, Object>();
	Suggest[] possibleValues;
	String[] excluded = EMPTY;
	String[] readOnly = EMPTY;
	String[] hidden = EMPTY;
	String[] include = EMPTY;
	boolean editable = true;
	ParameterType type = ParameterType.UNKNOWN;
	PossibleValuesResolver resolver = new FixedPossibleValuesResolver(EMPTY_SUGGEST);

	private ConversionService conversionService = new DefaultFormattingConversionService();
	private Type fieldType;

	private final String name;

	/**
	 * Creates action input parameter.
	 *
	 * @param methodParameter to describe
	 * @param value used during sample invocation
	 * @param conversionService to apply to value
	 */
	public SpringActionInputParameter(MethodParameter methodParameter, Object value, ConversionService conversionService,
			String name) {
		this.methodParameter = methodParameter;
		this.value = value;
		this.name = name;
		requestBody = methodParameter.getParameterAnnotation(RequestBody.class);
		requestParam = methodParameter.getParameterAnnotation(RequestParam.class);
		pathVariable = methodParameter.getParameterAnnotation(PathVariable.class);
		requestHeader = methodParameter.getParameterAnnotation(RequestHeader.class);
		// always determine input constraints,
		// might be a nested property which is neither requestBody, requestParam nor pathVariable
		Input inputAnnotation = methodParameter.getParameterAnnotation(Input.class);
		if (inputAnnotation != null) {
			putInputConstraint(Input.MIN, Integer.MIN_VALUE, inputAnnotation.min());
			putInputConstraint(Input.MAX, Integer.MAX_VALUE, inputAnnotation.max());
			putInputConstraint(Input.MIN_LENGTH, Integer.MIN_VALUE, inputAnnotation.minLength());
			putInputConstraint(Input.MAX_LENGTH, Integer.MAX_VALUE, inputAnnotation.maxLength());
			putInputConstraint(Input.STEP, 0, inputAnnotation.step());
			putInputConstraint(Input.PATTERN, "", inputAnnotation.pattern());
			putInputConstraint(Input.EDITABLE, "", inputAnnotation.editable());
			/**
			 * I think this is not correct, or at least makes XmlHtmlWriter to write readonly="[java.lang.String"
			 */
			// putInputConstraint(Input.READONLY, "", inputAnnotation.readOnly());
			putInputConstraint(Input.REQUIRED, "", inputAnnotation.required());

			editable = inputAnnotation.editable();
			excluded = inputAnnotation.exclude();
			readOnly = inputAnnotation.readOnly();
			hidden = inputAnnotation.hidden();
			include = inputAnnotation.include();
			type = ParameterType.INPUT;
		}
		if (inputAnnotation == null || inputAnnotation.value() == Type.FROM_JAVA) {
			if (isArrayOrCollection() || isRequestBody()) {
				fieldType = null;
			} else if (DataType.isNumber(getParameterType())) {
				fieldType = Type.NUMBER;
			} else {
				fieldType = Type.TEXT;
			}
		} else {
			fieldType = inputAnnotation.value();
		}
		createResolver(methodParameter);
		this.conversionService = conversionService;
		typeDescriptor = TypeDescriptor.nested(methodParameter, 0);
	}

	private void createResolver(MethodParameter methodParameter) {
		Class<?> parameterType = methodParameter.getNestedParameterType();
		Class<?> nested;
		Select select = methodParameter.getParameterAnnotation(Select.class);
		SuggestType type = select != null ? select.type() : SuggestType.INTERNAL;
		if (Enum[].class.isAssignableFrom(parameterType)) {
			resolver = new FixedPossibleValuesResolver(
					SimpleSuggest.wrap(parameterType.getComponentType().getEnumConstants(), type));
		} else if (Enum.class.isAssignableFrom(parameterType)) {
			resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(parameterType.getEnumConstants(), type));
		} else if (Collection.class.isAssignableFrom(parameterType)
				&& Enum.class.isAssignableFrom(nested = TypeDescriptor.nested(methodParameter, 1).getType())) {
			resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(nested.getEnumConstants(), type));
		} else if (select != null) {
			resolver = new OptionsPossibleValuesResolver(select);
		}
	}

	public SpringActionInputParameter(MethodParameter methodParameter, Object value, String name) {
		this(methodParameter, value, new DefaultFormattingConversionService(), name);
	}

	/**
	 * Creates new ActionInputParameter with default formatting conversion service.
	 *
	 * @param methodParameter holding metadata about the parameter
	 * @param value during sample method invocation
	 */

	public SpringActionInputParameter(MethodParameter methodParameter, Object value) {
		this(methodParameter, value, null);
	}

	private void putInputConstraint(String key, Object defaultValue, Object value) {
		if (!value.equals(defaultValue)) {
			inputConstraints.put(key, value);
		}
	}

	/**
	 * The value of the parameter at sample invocation time.
	 *
	 * @return value, may be null
	 */
	@Override
	public Object getValue() {
		return value;
	}

	/**
	 * The value of the parameter at sample invocation time, formatted according to conversion configuration.
	 *
	 * @return value, may be null
	 */
	@Override
	public String getValueFormatted() {
		String ret;
		if (value == null) {
			ret = null;
		} else {
			ret = (String) conversionService.convert(value, typeDescriptor, TypeDescriptor.valueOf(String.class));
		}
		return ret;
	}

	/**
	 * Gets HTML5 parameter type for input field according to {@link Type} annotation.
	 *
	 * @return the type
	 */
	@Override
	public Type getHtmlInputFieldType() {
		return fieldType;
	}

	@Override
	public boolean isRequestBody() {
		return requestBody != null;
	}

	@Override
	public boolean isRequestParam() {
		return requestParam != null;
	}

	@Override
	public boolean isPathVariable() {
		return pathVariable != null;
	}

	@Override
	public boolean isRequestHeader() {
		return requestHeader != null;
	}

	public boolean isInputParameter() {
		return type == ParameterType.INPUT && requestBody == null && pathVariable == null && requestHeader == null
				&& requestParam == null;
	}

	@Override
	public String getRequestHeaderName() {
		return isRequestHeader() ? requestHeader.value() : null;
	}

	/**
	 * Has constraints defined via <code>@Input</code> annotation. Note that there might also be other kinds of
	 * constraints, e.g. <code>@Select</code> may define values for {@link #getPossibleValues}.
	 *
	 * @return true if parameter is constrained
	 */
	@Override
	public boolean hasInputConstraints() {
		return !inputConstraints.isEmpty();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		return methodParameter.getParameterAnnotation(annotation);
	}

	/**
	 * Determines if request body input parameter has a hidden input property.
	 *
	 * @param property name or property path
	 * @return true if hidden
	 */
	@Override
	public boolean isHidden(String property) {
		Input inputAnnotation = methodParameter.getParameterAnnotation(Input.class);
		return inputAnnotation != null && arrayContains(inputAnnotation.hidden(), property);
	}

	@Override
	public boolean isReadOnly(String property) {
		return (!editable || arrayContains(readOnly, property));
	}

	@Override
	public boolean isIncluded(String property) {
		return !hasExplicitOrImplicitPropertyIncludeValue() || containsPropertyIncludeValue(property);
	}

	/**
	 * Find out if property is included by searching through all annotations.
	 *
	 * @param property
	 * @return
	 */
	private boolean containsPropertyIncludeValue(String property) {
		return arrayContains(readOnly, property) || arrayContains(hidden, property) || arrayContains(include, property);
	}

	/**
	 * Has any explicit include value or might have implicit includes because there is a hidden or readOnly flag.
	 *
	 * @return true if explicitly or implicitly included.
	 */
	private boolean hasExplicitOrImplicitPropertyIncludeValue() {
		// TODO maybe not a useful optimization
		return readOnly.length > 0 || hidden.length > 0 || include.length > 0;
	}

	/**
	 * Determines if request body input parameter should be excluded, considering {@link Input#exclude}.
	 *
	 * @param property name or property path
	 * @return true if excluded, false if no include statement found or not excluded
	 */
	@Override
	public boolean isExcluded(String property) {
		return excluded != null && arrayContains(excluded, property);
	}

	private boolean arrayContains(String[] array, String toFind) {
		if (array == null || array.length == 0) {
			return false;
		}
		for (String item : array) {
			if (toFind.equals(item)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Suggest<T>[] getPossibleValues(ActionDescriptor actionDescriptor) {
		List<Object> from = new ArrayList<Object>();
		for (String paramName : resolver.getParams()) {
			ActionInputParameter parameterValue = actionDescriptor.getActionInputParameter(paramName);
			if (parameterValue != null) {
				from.add(parameterValue.getValue());
			}
		}

		Object[] args = from.toArray();
		return resolver.getValues(args);

	}

	@Override
	public <T> void setPossibleValues(Suggest<T>[] possibleValues) {
		resolver = new FixedPossibleValuesResolver(possibleValues);
	}

	/**
	 * Determines if action input parameter is an array or collection.
	 *
	 * @return true if array or collection
	 */
	@Override
	public boolean isArrayOrCollection() {
		if (arrayOrCollection == null) {
			arrayOrCollection = DataType.isArrayOrCollection(getParameterType());
		}
		return arrayOrCollection;
	}

	/**
	 * Is this action input parameter required, based on the presence of a default value, the parameter annotations and
	 * the kind of input parameter.
	 *
	 * @return true if required
	 */
	@Override
	public boolean isRequired() {
		boolean ret;
		if (isRequestBody()) {
			ret = requestBody.required();
		} else if (isRequestParam()) {
			ret = !(isDefined(requestParam.defaultValue()) || !requestParam.required());
		} else if (isRequestHeader()) {
			ret = !(isDefined(requestHeader.defaultValue()) || !requestHeader.required());
		} else {
			ret = true;
		}
		return ret;
	}

	private boolean isDefined(String defaultValue) {
		return !ValueConstants.DEFAULT_NONE.equals(defaultValue);
	}

	/**
	 * Determines default value of request param or request header, if available.
	 *
	 * @return value or null
	 */
	public String getDefaultValue() {
		String ret;
		if (isRequestParam()) {
			ret = isDefined(requestParam.defaultValue()) ? requestParam.defaultValue() : null;
		} else if (isRequestHeader()) {
			ret = !(ValueConstants.DEFAULT_NONE.equals(requestHeader.defaultValue())) ? requestHeader.defaultValue() : null;
		} else {
			ret = null;
		}
		return ret;
	}

	/**
	 * Allows convenient access to multiple call values in case that this input parameter is an array or collection. Make
	 * sure to check {@link #isArrayOrCollection()} before calling this method.
	 *
	 * @return call values or empty array
	 * @throws UnsupportedOperationException if this input parameter is not an array or collection
	 */
	@Override
	public Object[] getValues() {
		Object[] callValues;
		if (!isArrayOrCollection()) {
			throw new UnsupportedOperationException("parameter is not an array or collection");
		}
		Object callValue = getValue();
		if (callValue == null) {
			callValues = new Object[0];
		} else {
			Class<?> parameterType = getParameterType();
			if (parameterType.isArray()) {
				callValues = (Object[]) callValue;
			} else {
				callValues = ((Collection<?>) callValue).toArray();
			}
		}
		return callValues;
	}

	/**
	 * Was a sample call value recorded for this parameter?
	 *
	 * @return if call value is present
	 */
	@Override
	public boolean hasValue() {
		return value != null;
	}

	/**
	 * Gets parameter name of this action input parameter.
	 *
	 * @return name
	 */
	@Override
	public String getParameterName() {
		String ret;
		String parameterName = methodParameter.getParameterName();
		if (parameterName == null) {
			methodParameter.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());
			ret = methodParameter.getParameterName();
		} else {
			ret = parameterName;
		}
		return ret;
	}

	/**
	 * Class which declares the method to which this input parameter belongs.
	 *
	 * @return class
	 */
	public Class<?> getDeclaringClass() {
		return methodParameter.getDeclaringClass();
	}

	/**
	 * Type of parameter.
	 *
	 * @return type
	 */
	@Override
	public Class<?> getParameterType() {
		return methodParameter.getParameterType();
	}

	/**
	 * Generic type of parameter.
	 *
	 * @return generic type
	 */
	@Override
	public java.lang.reflect.Type getGenericParameterType() {
		return methodParameter.getGenericParameterType();
	}

	/**
	 * Gets the input constraints defined for this action input parameter.
	 *
	 * @return constraints
	 */
	@Override
	public Map<String, Object> getInputConstraints() {
		return inputConstraints;
	}

	@Override
	public String toString() {
		String kind;
		if (isRequestBody()) {
			kind = "RequestBody";
		} else if (isPathVariable()) {
			kind = "PathVariable";
		} else if (isRequestParam()) {
			kind = "RequestParam";
		} else if (isRequestHeader()) {
			kind = "RequestHeader";
		} else {
			kind = "nested bean property";
		}
		return kind + (getParameterName() != null ? " " + getParameterName() : "") + ": "
				+ (value != null ? value.toString() : "no value");
	}

	private static <T extends Options<?>> Options<?> getOptions(Class<? extends Options<?>> beanType) {
		Options<?> options = getBean(beanType);
		if (options == null) {
			try {
				options = beanType.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return options;
	}

	private static <T> T getBean(Class<T> beanType) {
		try {
			RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
			HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();

			WebApplicationContext context = WebApplicationContextUtils
					.getWebApplicationContext(servletRequest.getSession().getServletContext());
			Map<String, T> beans = context.getBeansOfType(beanType);
			if (!beans.isEmpty()) {
				return beans.values().iterator().next();
			}
		} catch (Exception e) {}
		return null;
	}

	public void setExcluded(String[] excluded) {
		this.excluded = excluded;
	}

	public String getName() {
		return name;
	}

	public enum ParameterType {
		INPUT, SELECT, UNKNOWN
	}

	interface PossibleValuesResolver {
		String[] getParams();

		Suggest[] getValues(Object[] value);
	}

	class FixedPossibleValuesResolver implements PossibleValuesResolver {

		private final Suggest<?>[] values;

		public FixedPossibleValuesResolver(Suggest<?>[] values) {
			this.values = values;
		}

		@Override
		public String[] getParams() {
			return EMPTY;
		}

		@Override
		public Suggest<?>[] getValues(Object[] value) {
			return values;
		}

	}

	class OptionsPossibleValuesResolver implements PossibleValuesResolver {
		private final Options<?> options;
		private final Select select;

		public OptionsPossibleValuesResolver(Select select) {
			this.select = select;
			options = getOptions(select.options());
		}

		@Override
		public String[] getParams() {
			return select.args();
		}

		@Override
		public Suggest[] getValues(Object[] args) {

			return options.get(select.type(), select.value(), args);
		}
	}

}
