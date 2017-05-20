/*
 * Copyright 2014-2017 the original author or authors.
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

package org.springframework.hateoas.affordance.springmvc;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.hateoas.affordance.ParameterType;
import org.springframework.hateoas.affordance.SimpleSuggest;
import org.springframework.hateoas.affordance.Suggest;
import org.springframework.hateoas.affordance.SuggestType;
import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.hateoas.affordance.SuggestionsProvider;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.formaction.Options;
import org.springframework.hateoas.affordance.formaction.Select;
import org.springframework.hateoas.affordance.formaction.StringOptions;
import org.springframework.hateoas.affordance.formaction.Type;
import org.springframework.hateoas.affordance.support.DataTypeUtils;
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

/**
 * Describes a Spring MVC rest services method parameter value with recorded sample call value and input constraints.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public class SpringActionInputParameter implements ActionInputParameter {

	private static final String[] EMPTY = new String[0];
	private static final List<Suggest<?>> EMPTY_SUGGEST = Collections.emptyList();

	private final TypeDescriptor typeDescriptor;

	private RequestBody requestBody;
	private RequestParam requestParam;
	private PathVariable pathVariable;
	private RequestHeader requestHeader;

	private final MethodParameter methodParameter;
	private final Object value;

	private Boolean arrayOrCollection = null;

	private final Map<String, Object> inputConstraints = new HashMap<String, Object>();

	Suggest<?>[] possibleValues;

	private String[] excluded = EMPTY;
	private String[] readOnly = EMPTY;
	private String[] hidden = EMPTY;
	private String[] include = EMPTY;

	private boolean editable = true;

	private ParameterType type = ParameterType.UNKNOWN;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private PossibleValuesResolver<?> resolver = new FixedPossibleValuesResolver(EMPTY_SUGGEST, SuggestType.INTERNAL);

	private static final ConversionService DEFAULT_CONVERSION_SERVICE = new DefaultFormattingConversionService();

	private final ConversionService conversionService;

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
		this.conversionService = conversionService;

		Annotation[] annotations = methodParameter.getParameterAnnotations();
		Input inputAnnotation = null;
		Select select = null;

		for (Annotation annotation : annotations) {
			if (RequestBody.class.isInstance(annotation)) {
				this.requestBody = (RequestBody) annotation;
			} else if (RequestParam.class.isInstance(annotation)) {
				this.requestParam = (RequestParam) annotation;
			} else if (PathVariable.class.isInstance(annotation)) {
				this.pathVariable = (PathVariable) annotation;
			} else if (RequestHeader.class.isInstance(annotation)) {
				this.requestHeader = (RequestHeader) annotation;
			} else if (Input.class.isInstance(annotation)) {
				inputAnnotation = (Input) annotation;
			} else if (Select.class.isInstance(annotation)) {
				select = (Select) annotation;
			}
		}

		/**
		 * Check if annotations indicate that is required, for now only for request params & headers
		 */
		boolean requiredByAnnotations =
			(this.requestParam != null && this.requestParam.required())
			||
			(this.requestHeader != null && this.requestHeader.required());

		if (inputAnnotation != null) {
			putInputConstraint(ActionInputParameter.MIN, Integer.MIN_VALUE, inputAnnotation.min());
			putInputConstraint(ActionInputParameter.MAX, Integer.MAX_VALUE, inputAnnotation.max());
			putInputConstraint(ActionInputParameter.MIN_LENGTH, Integer.MIN_VALUE, inputAnnotation.minLength());
			putInputConstraint(ActionInputParameter.MAX_LENGTH, Integer.MAX_VALUE, inputAnnotation.maxLength());
			putInputConstraint(ActionInputParameter.STEP, 0, inputAnnotation.step());
			putInputConstraint(ActionInputParameter.PATTERN, "", inputAnnotation.pattern());
			setReadOnly(!inputAnnotation.editable());

			/**
			 * Check if annotations indicate that is required
			 */
			setRequired(inputAnnotation.required() || requiredByAnnotations);

			this.excluded = inputAnnotation.exclude();
			this.readOnly = inputAnnotation.readOnly();
			this.hidden = inputAnnotation.hidden();
			this.include = inputAnnotation.include();
			this.type = ParameterType.INPUT;
		} else {
			setReadOnly(select != null ? !select.editable() : !editable);
			putInputConstraint(ActionInputParameter.REQUIRED, "", requiredByAnnotations);
		}
		if (inputAnnotation == null || inputAnnotation.value() == Type.FROM_JAVA) {
			if (isArrayOrCollection() || isRequestBody()) {
				this.fieldType = null;
			} else if (DataTypeUtils.isNumber(getParameterType())) {
				this.fieldType = Type.NUMBER;
			} else {
				this.fieldType = Type.TEXT;
			}
		} else {
			this.fieldType = inputAnnotation.value();
		}
		createResolver(methodParameter, select);
		this.typeDescriptor = TypeDescriptor.nested(methodParameter, 0);
	}

	public SpringActionInputParameter(MethodParameter methodParameter, Object value, String name) {
		this(methodParameter, value, DEFAULT_CONVERSION_SERVICE, name);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createResolver(MethodParameter methodParameter, Select select) {

		Class<?> parameterType = methodParameter.getNestedParameterType();
		Class<?> nested;
		SuggestType type = SuggestType.INTERNAL;
		if (select != null && select.required()) {
			type = select.type();
			putInputConstraint(ActionInputParameter.REQUIRED, "", true);
		}

		if (select != null && (select.options() != StringOptions.class || !isEnumType(parameterType))) {
			this.resolver = new OptionsPossibleValuesResolver<Object>(select);
			this.type = ParameterType.SELECT;
		} else if (Enum[].class.isAssignableFrom(parameterType)) {
			this.resolver = new FixedPossibleValuesResolver(
					SimpleSuggest.wrap(parameterType.getComponentType().getEnumConstants()), type);
			this.type = ParameterType.SELECT;
		} else if (Enum.class.isAssignableFrom(parameterType)) {
			this.resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(parameterType.getEnumConstants()), type);
			this.type = ParameterType.SELECT;
		} else if (Collection.class.isAssignableFrom(parameterType)) {
			TypeDescriptor descriptor = TypeDescriptor.nested(methodParameter, 1);
			if (descriptor != null) {
				nested = descriptor.getType();
				if (Enum.class.isAssignableFrom(nested)) {
					this.resolver = new FixedPossibleValuesResolver(SimpleSuggest.wrap(nested.getEnumConstants()), type);
					this.type = ParameterType.SELECT;
				}
			}
		}

	}

	private boolean isEnumType(Class<?> parameterType) {

		return Enum[].class.isAssignableFrom(parameterType)
			|| Enum.class.isAssignableFrom(parameterType)
			|| Collection.class.isAssignableFrom(parameterType)
				&& Enum.class.isAssignableFrom(TypeDescriptor.nested(this.methodParameter, 1).getType());
	}

	private void putInputConstraint(String key, Object defaultValue, Object value) {

		if (!value.equals(defaultValue)) {
			this.inputConstraints.put(key, value);
		}
	}

	/**
	 * The value of the parameter at sample invocation time.
	 *
	 * @return value, may be null
	 */
	@Override
	public Object getValue() {
		return this.value;
	}

	/**
	 * The value of the parameter at sample invocation time, formatted according to conversion configuration.
	 *
	 * @return value, may be null
	 */
	@Override
	public String getValueFormatted() {

		if (this.value == null) {
			return null;
		} else {
			return (String) conversionService.convert(this.value, this.typeDescriptor, TypeDescriptor.valueOf(String.class));
		}
	}

	/**
	 * Gets HTML5 parameter type for input field according to {@link Type} annotation.
	 *
	 * @return the type
	 */
	@Override
	public Type getHtmlInputFieldType() {
		return this.fieldType;
	}

	@Override
	public void setHtmlInputFieldType(Type type) {
		this.fieldType = type;
	}

	@Override
	public boolean isRequestBody() {
		return this.requestBody != null;
	}

	@Override
	public boolean isRequestParam() {
		return this.requestParam != null;
	}

	@Override
	public boolean isPathVariable() {
		return this.pathVariable != null;
	}

	@Override
	public boolean isRequestHeader() {
		return this.requestHeader != null;
	}

	public boolean isInputParameter() {

		return this.type == ParameterType.INPUT
			&& this.requestBody == null
			&& this.pathVariable == null
			&& this.requestHeader == null
			&& this.requestParam == null;
	}

	@Override
	public String getRequestHeaderName() {
		return isRequestHeader() ? this.requestHeader.value() : null;
	}

	/**
	 * Has constraints defined via <code>@Input</code> annotation. Note that there might also be other kinds of
	 * constraints, e.g. <code>@Select</code> may define values for {@link #getPossibleValues}.
	 *
	 * @return true if parameter is constrained
	 */
	@Override
	public boolean hasInputConstraints() {
		return !this.inputConstraints.isEmpty();
	}

	@Override
	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		return this.methodParameter.getParameterAnnotation(annotation);
	}

	/**
	 * Determines if request body input parameter has a hidden input property.
	 *
	 * @param property name or property path
	 * @return true if hidden
	 */
	boolean isHidden(String property) {
		return arrayContains(this.hidden, property);
	}

	boolean isReadOnly(String property) {
		return (!this.editable || arrayContains(this.readOnly, property));
	}

	@Override
	public void setReadOnly(boolean readOnly) {

		this.editable = !readOnly;
		putInputConstraint(ActionInputParameter.EDITABLE, "", this.editable);
	}

	@Override
	public void setRequired(boolean required) {
		putInputConstraint(ActionInputParameter.REQUIRED, "", required);
	}

	boolean isIncluded(String property) {

		if (isExcluded(property)) {
			return false;
		}

		if (this.include == null || this.include.length == 0) {
			return true;
		}

		return containsPropertyIncludeValue(property);
	}

	/**
	 * Find out if property is included by searching through all annotations.
	 *
	 * @param property
	 * @return
	 */
	private boolean containsPropertyIncludeValue(String property) {
		return arrayContains(this.readOnly, property)
			|| arrayContains(this.hidden, property)
			|| arrayContains(this.include, property);
	}

	/**
	 * Determines if request body input parameter should be excluded, considering {@link Input#exclude}.
	 *
	 * @param property name or property path
	 * @return true if excluded, false if no include statement found or not excluded
	 */
	private boolean isExcluded(String property) {
		return this.excluded != null && arrayContains(this.excluded, property);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <T> List<Suggest<T>> getPossibleValues(ActionDescriptor actionDescriptor) {

		List<Object> from = new ArrayList<Object>();

		for (String paramName : resolver.getParams()) {

			ActionInputParameter parameterValue = actionDescriptor.getActionInputParameter(paramName);
			
			if (parameterValue != null) {
				from.add(parameterValue.getValue());
			}
		}

		return (List) resolver.getValues(from);

	}

	/* 
	 * (non-Javadoc)
	 * @see ActionInputParameter#getOptions()
	 */
	@Override
	public Suggestions getSuggestions() {

		org.springframework.hateoas.affordance.Select select = this.methodParameter
				.getParameterAnnotation(org.springframework.hateoas.affordance.Select.class);

		if (select == null) {
			return getDefaultSuggestions(getParameterType());
		}

		String[] strings = select.value();

		if (strings.length > 0) {
			return org.springframework.hateoas.affordance.Suggestions.values(strings);
		}

		Class<? extends SuggestionsProvider> options = select.provider();

		if (!options.equals(SuggestionsProvider.class)) {

			SuggestionsProvider instance = getBean(options);

			instance = instance == null ? BeanUtils.instantiateClass(options) : instance;

			return instance.getSuggestions();
		}

		return getDefaultSuggestions(getParameterType());
	}

	private static Suggestions getDefaultSuggestions(Class<?> type) {

		if (type.isEnum()) {
			return org.springframework.hateoas.affordance.Suggestions.values(type.getEnumConstants());
		}

		return org.springframework.hateoas.affordance.Suggestions.NONE;
	}

	@Override
	public <T> void setPossibleValues(List<Suggest<T>> possibleValues) {
		this.resolver = new FixedPossibleValuesResolver<T>(possibleValues, this.resolver.getType());
	}

	@Override
	public SuggestType getSuggestType() {
		return this.resolver.getType();
	}

	@Override
	public void setSuggestType(SuggestType type) {
		this.resolver.setType(type);
	}

	/**
	 * Determines if action input parameter is an array or collection.
	 *
	 * @return true if array or collection
	 */
	@Override
	public boolean isArrayOrCollection() {

		if (this.arrayOrCollection == null) {
			this.arrayOrCollection = DataTypeUtils.isArrayOrIterable(getParameterType());
		}
		return this.arrayOrCollection;
	}

	/**
	 * Is this action input parameter required, based on the presence of a default value, the parameter annotations and
	 * the kind of input parameter.
	 *
	 * @return true if required
	 */
	@Override
	public boolean isRequired() {

		if (isRequestBody()) {
			return this.requestBody.required();
		} else if (isRequestParam()) {
			return !(isDefined(this.requestParam.defaultValue()) || !this.requestParam.required());
		} else if (isRequestHeader()) {
			return !(isDefined(this.requestHeader.defaultValue()) || !this.requestHeader.required());
		} else {
			return true;
		}
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

		if (isRequestParam()) {
			return isDefined(this.requestParam.defaultValue()) ? this.requestParam.defaultValue() : null;
		} else if (isRequestHeader()) {
			return !(ValueConstants.DEFAULT_NONE.equals(this.requestHeader.defaultValue())) ? this.requestHeader.defaultValue() : null;
		} else {
			return null;
		}
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

		if (!isArrayOrCollection()) {
			throw new UnsupportedOperationException("parameter is not an array or collection");
		}

		if (getValue() == null) {
			return new Object[0];
		} else {
			if (getParameterType().isArray()) {
				return (Object[]) getValue();
			} else {
				return ((Collection<?>) getValue()).toArray();
			}
		}
	}

	/**
	 * Was a sample call value recorded for this parameter?
	 *
	 * @return if call value is present
	 */
	@Override
	public boolean hasValue() {
		return this.value != null;
	}

	/**
	 * Gets parameter name of this action input parameter.
	 *
	 * @return name
	 */
	@Override
	public String getParameterName() {

		String parameterName = this.methodParameter.getParameterName();

		if (parameterName == null) {
			this.methodParameter.initParameterNameDiscovery(new LocalVariableTableParameterNameDiscoverer());
			return this.methodParameter.getParameterName();
		} else {
			return parameterName;
		}
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
				+ (this.value != null ? this.value.toString() : "no value");
	}

	private static <T extends Options<V>, V> Options<V> getOptions(Class<? extends Options<V>> beanType) {

		Options<V> options = getBean(beanType);
		if (options == null) {
			try {
				options = BeanUtils.instantiateClass(beanType);
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

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public ParameterType getType() {
		return this.type;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	interface PossibleValuesResolver<T> {

		String[] getParams();

		List<Suggest<T>> getValues(List<?> value);

		SuggestType getType();

		void setType(SuggestType type);
	}

	class FixedPossibleValuesResolver<T> implements PossibleValuesResolver<T> {

		private final List<Suggest<T>> values;
		private SuggestType type;

		public FixedPossibleValuesResolver(List<Suggest<T>> values, SuggestType type) {

			this.values = values;
			this.type = type;
		}

		@Override
		public String[] getParams() {
			return EMPTY;
		}

		@Override
		public List<Suggest<T>> getValues(List<?> value) {
			return this.values;
		}

		@Override
		public SuggestType getType() {
			return this.type;
		}

		@Override
		public void setType(SuggestType type) {
			this.type = type;
		}

	}

	class OptionsPossibleValuesResolver<T> implements PossibleValuesResolver<T> {

		private final Options<T> options;
		private final Select select;

		private SuggestType type;

		@SuppressWarnings("unchecked")
		public OptionsPossibleValuesResolver(Select select) {

			this.select = select;
			this.type = select.type();
			this.options = getOptions((Class<Options<T>>) select.options());
		}

		@Override
		public String[] getParams() {
			return this.select.args();
		}

		@Override
		public List<Suggest<T>> getValues(List<?> args) {
			return this.options.get(select.value(), args.toArray());
		}

		@Override
		public SuggestType getType() {
			return this.type;
		}

		@Override
		public void setType(SuggestType type) {
			this.type = type;
		}
	}

}
