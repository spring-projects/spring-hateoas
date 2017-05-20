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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.hateoas.affordance.ActionInputParameterVisitor;
import org.springframework.hateoas.affordance.formaction.Action;
import org.springframework.hateoas.affordance.formaction.Cardinality;
import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.affordance.formaction.ResourceHandler;
import org.springframework.hateoas.affordance.formaction.Select;
import org.springframework.hateoas.affordance.support.DataTypeUtils;
import org.springframework.hateoas.affordance.support.PropertyUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Describes an HTTP method independently of a specific rest framework. Has knowledge about possible request data, i.e.
 * which types and values are suitable for an action. For example, an action descriptor can be used to create a form
 * with select options and typed input fields that calls a POST handler. It has {@link ActionInputParameter}s which
 * represent method handler arguments. Supported method handler arguments are:
 * <ul>
 * <li>path variables</li>
 * <li>request params (url query params)</li>
 * <li>request headers</li>
 * <li>request body</li>
 * </ul>
 *
 * @author Dietrich Schulten
 * @author Oliver Gierke
 */
public class SpringActionDescriptor implements ActionDescriptor {

	private @Getter HttpMethod httpMethod;
	private String actionName;
	private @Getter List<MediaType> consumes, produces;
	private Map<String, ActionInputParameter> requestParams = new LinkedHashMap<String, ActionInputParameter>();
	private Map<String, ActionInputParameter> pathVariables = new LinkedHashMap<String, ActionInputParameter>();
	private Map<String, ActionInputParameter> requestHeaders = new LinkedHashMap<String, ActionInputParameter>();
	private Map<String, ActionInputParameter> bodyInputParameters = new LinkedHashMap<String, ActionInputParameter>();

	private String semanticActionType;
	private ActionInputParameter requestBody;
	private Cardinality cardinality = Cardinality.SINGLE;

	/**
	 * Creates an {@link ActionDescriptor}.
	 *
	 * @param actionName name of the action, e.g. the method name of the handler method. Can be used by an action
	 *          representation, e.g. to identify the action using a form name.
	 * @param httpMethod used during submit
	 */
	public SpringActionDescriptor(String actionName, HttpMethod httpMethod) {
		this(actionName, httpMethod, Collections.<MediaType>emptyList(), Collections.<MediaType>emptyList());
	}

	public SpringActionDescriptor(String actionName, HttpMethod httpMethod, List<MediaType> consumes,
			List<MediaType> produces) {

		Assert.notNull(actionName, "actionName must not be null!");
		Assert.notNull(httpMethod, "httpMethod must not be null!");

		this.httpMethod = httpMethod;
		this.actionName = actionName;
		this.consumes = consumes;
		this.produces = produces;
	}

	public SpringActionDescriptor(Method method) {

		this.httpMethod = getHttpMethod(method);
		this.actionName = method.getName();
		this.consumes = getConsumes(method);
		this.produces = getProduces(method);
		this.cardinality = getCardinality(method, this.httpMethod, method.getReturnType());
	}

	/**
	 * The name of the action, for use as form name, usually the method name of the handler method.
	 *
	 * @return action name, never null
	 */
	@Override
	public String getActionName() {
		return this.actionName;
	}

	/**
	 * Gets the path variable names.
	 *
	 * @return names or empty collection, never null
	 */
	@Override
	public Collection<String> getPathVariableNames() {
		return this.pathVariables.keySet();
	}

	/**
	 * Gets the request header names.
	 *
	 * @return names or empty collection, never null
	 */
	@Override
	public Collection<String> getRequestHeaderNames() {
		return this.requestHeaders.keySet();
	}

	/**
	 * Gets the request parameter (query param) names.
	 *
	 * @return names or empty collection, never null
	 */
	@Override
	public Collection<String> getRequestParamNames() {
		return this.requestParams.keySet();
	}

	/**
	 * Adds descriptor for request param.
	 *
	 * @param key name of request param
	 * @param actionInputParameter descriptor
	 */
	public void addRequestParam(String key, ActionInputParameter actionInputParameter) {
		this.requestParams.put(key, actionInputParameter);
	}

	/**
	 * Adds descriptor for path variable.
	 *
	 * @param key name of path variable
	 * @param actionInputParameter descriptorg+ann#2
	 */

	public void addPathVariable(String key, ActionInputParameter actionInputParameter) {
		this.pathVariables.put(key, actionInputParameter);
	}

	/**
	 * Adds descriptor for request header.
	 *
	 * @param key name of request header
	 * @param actionInputParameter descriptor
	 */
	public void addRequestHeader(String key, ActionInputParameter actionInputParameter) {
		this.requestHeaders.put(key, actionInputParameter);
	}

	/**
	 * Gets all action input parameter names.
	 *
	 * @return
	 */
	@Override
	public Collection<String> getActionInputParameterNames() {

		Collection<String> actionInputParameterNames = new ArrayList<String>();

		actionInputParameterNames.addAll(this.requestParams.keySet());
		actionInputParameterNames.addAll(this.pathVariables.keySet());
		actionInputParameterNames.addAll(this.bodyInputParameters.keySet());

		return actionInputParameterNames;
	}

	/**
	 * Get all of the {@link ActionInputParameter}s.
	 *
	 * @return
	 */
	@Override
	public Collection<ActionInputParameter> getActionInputParameters() {

		Collection<ActionInputParameter> actionInputParameters = new ArrayList<ActionInputParameter>();

		actionInputParameters.addAll(this.requestParams.values());
		actionInputParameters.addAll(this.pathVariables.values());
		actionInputParameters.addAll(this.bodyInputParameters.values());

		return actionInputParameters;
	}

	/**
	 * Gets input parameter info which is part of the URL mapping, be it request parameters, path variables or request
	 * body attributes.
	 *
	 * @param name to retrieve
	 * @return parameter descriptor or null
	 */
	@Override
	public ActionInputParameter getActionInputParameter(String name) {

		ActionInputParameter results = this.requestParams.get(name);

		if (results == null) {
			results = this.pathVariables.get(name);
		}
		if (results == null) {
			results = this.bodyInputParameters.get(name);
		}

		return results;
	}

	/**
	 * Recursively navigate to return a BeanWrapper for the nested property path.
	 *
	 * @param propertyPath property property path, which may be nested
	 * @return a BeanWrapper for the target bean
	 */
	PropertyDescriptor getPropertyDescriptorForPropertyPath(String propertyPath, Class<?> propertyType) {

		int pos = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(propertyPath);

		// Handle nested properties recursively.
		if (pos > -1) {
			String nestedProperty = propertyPath.substring(0, pos);
			String nestedPath = propertyPath.substring(pos + 1);
			PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(propertyType, nestedProperty);
			return getPropertyDescriptorForPropertyPath(nestedPath, propertyDescriptor.getPropertyType());
		} else {
			return BeanUtils.getPropertyDescriptor(propertyType, propertyPath);
		}
	}

	/**
	 * Gets request header info.
	 *
	 * @param name of the request header
	 * @return request header descriptor or null
	 */
	public ActionInputParameter getRequestHeader(String name) {
		return this.requestHeaders.get(name);
	}

	/**
	 * Gets request body info.
	 *
	 * @return request body descriptor or null
	 */
	@Override
	public ActionInputParameter getRequestBody() {
		return this.requestBody;
	}

	/**
	 * Determines if this descriptor has a request body.
	 *
	 * @return true if request body is present
	 */
	@Override
	public boolean hasRequestBody() {
		return this.requestBody != null;
	}

	/**
	 * Allows to set request body descriptor.
	 *
	 * @param newRequestBody descriptor to set
	 */
	public void setRequestBody(ActionInputParameter newRequestBody) {

		this.requestBody = newRequestBody;

		if (newRequestBody != null) {

			List<ActionInputParameter> bodyInputParameters = new ArrayList<ActionInputParameter>();

			recurseBeanCreationParams(getRequestBody().getParameterType(), (SpringActionInputParameter) getRequestBody(),
					getRequestBody().getValue(), "", Collections.<String>emptySet(), new ActionInputParameterVisitor() {
						@Override
						public void visit(ActionInputParameter inputParameter) {}
					}, bodyInputParameters);

			for (ActionInputParameter actionInputParameter : bodyInputParameters) {
				this.bodyInputParameters.put(actionInputParameter.getName(), actionInputParameter);
			}
		}
	}

	/**
	 * Gets semantic type of action, e.g. a subtype of hydra:Operation or schema:Action. Use {@link Action} on a method
	 * handler to define the semantic type of an action.
	 *
	 * @return URL identifying the type
	 */
	@Override
	public String getSemanticActionType() {
		return this.semanticActionType;
	}

	/**
	 * Sets semantic type of action, e.g. a subtype of hydra:Operation or schema:Action.
	 *
	 * @param semanticActionType URL identifying the type
	 */
	public void setSemanticActionType(String semanticActionType) {
		this.semanticActionType = semanticActionType;
	}

	/**
	 * Determines action input parameters for required url variables.
	 *
	 * @return required url variables
	 */
	@Override
	public Map<String, ActionInputParameter> getRequiredParameters() {

		Map<String, ActionInputParameter> ret = new HashMap<String, ActionInputParameter>();

		for (Map.Entry<String, ActionInputParameter> entry : this.requestParams.entrySet()) {
			ActionInputParameter annotatedParameter = entry.getValue();
			if (annotatedParameter.isRequired()) {
				ret.put(entry.getKey(), annotatedParameter);
			}
		}

		for (Map.Entry<String, ActionInputParameter> entry : this.pathVariables.entrySet()) {
			ActionInputParameter annotatedParameter = entry.getValue();
			ret.put(entry.getKey(), annotatedParameter);
		}

		// requestBody not supported, would have to use exploded modifier
		return ret;
	}

	/**
	 * Allows to set the cardinality, i.e. specify if the action refers to a collection or a single resource. Default is
	 * {@link Cardinality#SINGLE}
	 *
	 * @param cardinality to set
	 */
	public void setCardinality(Cardinality cardinality) {
		this.cardinality = cardinality;
	}

	/**
	 * Allows to decide whether or not the action refers to a collection resource.
	 *
	 * @return cardinality
	 */
	@Override
	public Cardinality getCardinality() {
		return this.cardinality;
	}

	@Override
	public void accept(ActionInputParameterVisitor visitor) {

		if (hasRequestBody()) {
			for (ActionInputParameter inputParameter : this.bodyInputParameters.values()) {
				visitor.visit(inputParameter);
			}
		} else {
			Collection<String> paramNames = getRequestParamNames();
			for (String paramName : paramNames) {
				ActionInputParameter inputParameter = getActionInputParameter(paramName);
				visitor.visit(inputParameter);
			}
		}
	}

	/**
	 * Renders input fields for bean properties of bean to add or update or patch.
	 *
	 * @param beanType to render
	 * @param annotatedParameter which requires the bean
	 * @param currentCallValue sample call value
	 * @param parentParamName
	 * @param knownFields
	 * @param methodHandler
	 * @param bodyInputParameters
	 */
	static void recurseBeanCreationParams(Class<?> beanType, SpringActionInputParameter annotatedParameter,
			Object currentCallValue, String parentParamName, Set<String> knownFields,
			ActionInputParameterVisitor methodHandler, List<ActionInputParameter> bodyInputParameters) {

		// TODO collection, map and object node creation are only describable by an annotation, not via type reflection
		if (ObjectNode.class.isAssignableFrom(beanType) || Map.class.isAssignableFrom(beanType)
				|| Collection.class.isAssignableFrom(beanType) || beanType.isArray()) {
			return; // use @Input(include) to list parameter names, at least? Or mix with hdiv's form builder?
		}
		try {
			// find default ctor
			Constructor<?> constructor = PropertyUtils.findConstructorByAnnotation(beanType, JsonCreator.class);

			// find ctor with JsonCreator ann
			if (constructor == null) {
				constructor = PropertyUtils.findDefaultConstructor(beanType);
			}

			int parameterCount = constructor == null ? 0 : constructor.getParameterTypes().length;
			
			Set<String> knownConstructorFields = new HashSet<String>();

			if (constructor != null && parameterCount > 0) {

				Class<?>[] parameters = constructor.getParameterTypes();
				int paramIndex = 0;
				for (Annotation[] annotationsOnParameter : constructor.getParameterAnnotations()) {
					for (Annotation annotation : annotationsOnParameter) {
						if (JsonProperty.class == annotation.annotationType()) {
							JsonProperty jsonProperty = (JsonProperty) annotation;

							// TODO use required attribute of JsonProperty for required fields ->
							String paramName = jsonProperty.value();
							Class<?> parameterType = parameters[paramIndex];
							Object propertyValue = PropertyUtils.getPropertyOrFieldValue(currentCallValue, paramName);
							MethodParameter methodParameter = new MethodParameter(constructor, paramIndex);

							String fieldName = invokeHandlerOrFollowRecurse(methodParameter, annotatedParameter, parentParamName,
									paramName, parameterType, propertyValue, knownConstructorFields, methodHandler, bodyInputParameters);

							if (fieldName != null) {
								knownConstructorFields.add(fieldName);
							}

							paramIndex++; // increase for each @JsonProperty
						}
					}
				}

				Assert.isTrue(parameters.length == paramIndex, "not all constructor arguments of @JsonCreator "
						+ constructor.getName() + " are annotated with @JsonProperty");
			}

			// TODO support Option provider by other method args?
			// add input field for every setter
			for (PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(beanType).getPropertyDescriptors()) {
				Method writeMethod = propertyDescriptor.getWriteMethod();
				String propertyName = propertyDescriptor.getName();

				if (writeMethod == null || knownFields.contains(parentParamName + propertyName)) {
					continue;
				}
				Class<?> propertyType = propertyDescriptor.getPropertyType();

				Object propertyValue = PropertyUtils.getPropertyOrFieldValue(currentCallValue, propertyName);
				MethodParameter methodParameter = new MethodParameter(propertyDescriptor.getWriteMethod(), 0);

				invokeHandlerOrFollowRecurse(methodParameter, annotatedParameter, parentParamName, propertyName, propertyType,
						propertyValue, knownConstructorFields, methodHandler, bodyInputParameters);

			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to write input fields for constructor", e);
		}
	}

	private static String invokeHandlerOrFollowRecurse(MethodParameter methodParameter,
			SpringActionInputParameter annotatedParameter, String parentParamName, String paramName, Class<?> parameterType,
			Object propertyValue, Set<String> knownFields, ActionInputParameterVisitor handler,
			List<ActionInputParameter> bodyInputParameters) {

		Annotation[] annotations = methodParameter.getParameterAnnotations();

		String paramPath = parentParamName + paramName;

		if (DataTypeUtils.isSingleValueType(parameterType) ||
			DataTypeUtils.isArrayOrIterable(parameterType) ||
			getParameterAnnotation(annotations, Select.class) != null) {

			/**
			 * TODO This is a temporal patch, to be reviewed...
			 */
			if (annotatedParameter == null) {

				ActionInputParameter inputParameter = new SpringActionInputParameter(methodParameter, propertyValue,
						parentParamName + paramName);
				bodyInputParameters.add(inputParameter);
				handler.visit(inputParameter);
				return inputParameter.getName();

			} else if (annotatedParameter.isIncluded(paramPath) && !knownFields.contains(parentParamName + paramName)) {

				DTOParam dtoAnnotation = getParameterAnnotation(annotations, DTOParam.class);

				StringBuilder sb = new StringBuilder(64);

				if (DataTypeUtils.isArrayOrIterable(parameterType) && dtoAnnotation != null) {
					Object wildCardValue = null;
					if (propertyValue != null) {
						// if the element is wildcard dto type element we need to get the first value
						if (parameterType.isArray()) {
							Object[] array = (Object[]) propertyValue;
							if (!dtoAnnotation.wildcard()) {
								for (int i = 0; i < array.length; i++) {
									if (array[i] != null) {
										sb.setLength(0);
										recurseBeanCreationParams(array[i].getClass(), annotatedParameter, array[i],
												sb.append(parentParamName).append(paramName).append('[').append(i).append("].").toString(),
												knownFields, handler, bodyInputParameters);
									}
								}
							} else if (array.length > 0) {
								wildCardValue = array[0];
							}
						} else {
							int i = 0;
							if (!dtoAnnotation.wildcard()) {
								for (Object value : (Collection<?>) propertyValue) {
									if (value != null) {
										sb.setLength(0);
										recurseBeanCreationParams(value.getClass(), annotatedParameter, value,
												sb.append(parentParamName).append(paramName).append('[').append(i++).append("].").toString(),
												knownFields, handler, bodyInputParameters);
									}
								}
							} else if (!((Collection<?>) propertyValue).isEmpty()) {
								wildCardValue = ((Collection<?>) propertyValue).iterator().next();
							}
						}
					}
					if (dtoAnnotation.wildcard()) {
						Class<?> willCardClass = null;
						if (wildCardValue != null) {
							willCardClass = wildCardValue.getClass();
						} else {
							Type type = methodParameter.getGenericParameterType();
							if (type != null && type instanceof ParameterizedType) {
								willCardClass = (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
							}
						}
						if (willCardClass != null) {
							recurseBeanCreationParams(willCardClass,
									annotatedParameter, wildCardValue, sb.append(parentParamName).append(paramName)
											.append(DTOParam.WILDCARD_LIST_MASK).append('.').toString(),
									knownFields, handler, bodyInputParameters);
						}
					}
					return parentParamName + paramName;
				} else {
					SpringActionInputParameter inputParameter = new SpringActionInputParameter(methodParameter, propertyValue,
							parentParamName + paramName);
					// TODO We need to find a better solution for this
					inputParameter.possibleValues = annotatedParameter.possibleValues;
					bodyInputParameters.add(inputParameter);
					handler.visit(inputParameter);
					if (annotatedParameter.isReadOnly(paramPath)) {
						inputParameter.setReadOnly(true);
					}
					if (annotatedParameter.isHidden(paramPath)) {
						inputParameter.setHtmlInputFieldType(org.springframework.hateoas.affordance.formaction.Type.HIDDEN);
					}
					return inputParameter.getName();
				}
			}

		} else {
			Object callValueBean;
			if (propertyValue instanceof Resource) {
				callValueBean = ((Resource<?>) propertyValue).getContent();
			} else {
				callValueBean = propertyValue;
			}
			recurseBeanCreationParams(parameterType, annotatedParameter, callValueBean, parentParamName + paramName + ".",
					knownFields, handler, bodyInputParameters);
		}

		return null;
	}

	/**
	 * Look up the "method" of a @RequestMapping.
	 *
	 * @param method
	 * @return
	 */
	private static HttpMethod getHttpMethod(Method method) {

		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		RequestMethod requestMethod;

		if (methodRequestMapping != null) {

			RequestMethod[] methods = methodRequestMapping.method();
			requestMethod = methods.length == 0 ? RequestMethod.GET : methods[0];

		} else {
			requestMethod = RequestMethod.GET; // default
		}

		return HttpMethod.valueOf(requestMethod.name());
	}

	/**
	 * Look up the "consumes" from a @RequestMapping.
	 * 
	 * @param method
	 * @return
	 */
	private static List<MediaType> getConsumes(Method method) {

		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);

		if (methodRequestMapping == null) {
			return Collections.emptyList();
		}

		// TODO: With Java 8, use a Stream operation

		List<MediaType> result = new ArrayList<MediaType>();

		for (String type : methodRequestMapping.consumes()) {
			result.add(MediaType.parseMediaType(type));
		}

		return result;
	}

	/**
	 * Look up the "produces" from a @RequestMapping.
	 *
	 * @param method
	 * @return
	 */
	private static List<MediaType> getProduces(Method method) {

		RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);

		if (methodRequestMapping == null) {
			return Collections.emptyList();
		}

		// TODO: With Java 8, use a Stream operation
		
		List<MediaType> result = new ArrayList<MediaType>();

		for (String type : methodRequestMapping.produces()) {
			result.add(MediaType.parseMediaType(type));
		}

		return result;
	}

	/**
	 * Look at the REST method and decide if this is a {@literal COLLECTION} or a {@literal SINGLE} item.
	 * 
	 * @param invokedMethod
	 * @param httpMethod
	 * @param genericReturnType
	 * @return
	 */
	private Cardinality getCardinality(Method invokedMethod, HttpMethod httpMethod, Type genericReturnType) {

		ResourceHandler resourceAnn = AnnotationUtils.findAnnotation(invokedMethod, ResourceHandler.class);

		if (resourceAnn != null) {
			return resourceAnn.value();
		} else {
			if (HttpMethod.POST == httpMethod || containsCollection(genericReturnType)) {
				return Cardinality.COLLECTION;
			} else {
				return Cardinality.SINGLE;
			}
		}
	}

	/**
	 * Look at the return type of a method, and glean if it's a container or not.
	 *
	 * @param genericReturnType
	 * @return
	 */
	private boolean containsCollection(Type genericReturnType) {
		
		if (genericReturnType instanceof ParameterizedType) {

			ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
			Type rawType = parameterizedType.getRawType();

			Assert.state(rawType instanceof Class<?>, "raw type is not a Class: " + rawType.toString());

			Class<?> cls = (Class<?>) rawType;

			if (HttpEntity.class.isAssignableFrom(cls)) {
				Type[] typeArguments = parameterizedType.getActualTypeArguments();
				return containsCollection(typeArguments[0]);
			} else if (Iterable.class.isAssignableFrom(cls)) {
				return true;
			} else {
				return false;
			}

		} else if (genericReturnType instanceof GenericArrayType) {
			return true;
		} else if (genericReturnType instanceof WildcardType) {

			WildcardType wildcardType = (WildcardType) genericReturnType;
			return containsCollection(getBound(wildcardType.getLowerBounds()))
				||
				containsCollection(getBound(wildcardType.getUpperBounds()));

		} else if (genericReturnType instanceof TypeVariable) {
			return false;
		} else if (genericReturnType instanceof Class) {
			return Iterable.class.isAssignableFrom((Class<?>) genericReturnType);
		} else {
			return false;
		}
	}

	/**
	 * Fetch the first type in a generic wildcard boundary
	 * 
	 * @param bounds
	 * @return
	 */
	private Type getBound(Type[] bounds) {

		if (bounds != null && bounds.length > 0) {
			return bounds[0];
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return "SpringActionDescriptor [httpMethod=" + httpMethod + ", actionName=" + actionName + "]";
	}

	@SuppressWarnings("unchecked")
	private static <T extends Annotation> T getParameterAnnotation(Annotation[] anns, Class<T> annotationType) {

		for (Annotation ann : anns) {
			if (annotationType.isInstance(ann)) {
				return (T) ann;
			}
		}
		return null;
	}
}
