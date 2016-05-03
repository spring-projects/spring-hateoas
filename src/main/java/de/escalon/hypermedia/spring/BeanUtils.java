package de.escalon.hypermedia.spring;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Resource;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.escalon.hypermedia.PropertyUtils;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import de.escalon.hypermedia.affordance.DataType;

public class BeanUtils {
	/**
	 * Renders input fields for bean properties of bean to add or update or patch.
	 *
	 * @param sirenFields to add to
	 * @param beanType to render
	 * @param annotatedParameters which describes the method
	 * @param annotatedParameter which requires the bean
	 * @param currentCallValue sample call value
	 */
	public static void recurseBeanCreationParams(Class<?> beanType, ActionDescriptor annotatedParameters,
			ActionInputParameter annotatedParameter, Object currentCallValue, String parentParamName, Set<String> knownFields,
			MethodParameterHandler methodHandler) {

		// TODO collection, map and object node creation are only describable by an annotation, not via type reflection
		if (ObjectNode.class.isAssignableFrom(beanType) || Map.class.isAssignableFrom(beanType)
				|| Collection.class.isAssignableFrom(beanType) || beanType.isArray()) {
			return; // use @Input(include) to list parameter names, at least? Or mix with hdiv's form builder?
		}
		try {
			Constructor<?>[] constructors = beanType.getConstructors();
			// find default ctor
			Constructor<?> constructor = PropertyUtils.findDefaultCtor(constructors);
			// find ctor with JsonCreator ann
			if (constructor == null) {
				constructor = PropertyUtils.findJsonCreator(constructors, JsonCreator.class);
			}
			Assert.notNull(constructor, "no default constructor or JsonCreator found for type " + beanType.getName());
			int parameterCount = constructor.getParameterTypes().length;

			Set<String> knownConstructorFields = new HashSet<String>();
			if (parameterCount > 0) {
				Annotation[][] annotationsOnParameters = constructor.getParameterAnnotations();

				Class<?>[] parameters = constructor.getParameterTypes();
				int paramIndex = 0;
				for (Annotation[] annotationsOnParameter : annotationsOnParameters) {
					for (Annotation annotation : annotationsOnParameter) {
						if (JsonProperty.class == annotation.annotationType()) {
							JsonProperty jsonProperty = (JsonProperty) annotation;

							// TODO use required attribute of JsonProperty for required fields
							String paramName = jsonProperty.value();
							Class<?> parameterType = parameters[paramIndex];
							Object propertyValue = PropertyUtils.getPropertyOrFieldValue(currentCallValue, paramName);
							MethodParameter methodParameter = new MethodParameter(constructor, paramIndex);

							String fieldName = methodHandler.onMethodParameter(methodParameter, annotatedParameter,
									annotatedParameters, parentParamName, paramName, parameterType, propertyValue);
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
			final BeanInfo beanInfo = getBeanInfo(beanType);
			final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

			// add input field for every setter
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				final Method writeMethod = propertyDescriptor.getWriteMethod();
				String propertyName = propertyDescriptor.getName();

				if (writeMethod == null || knownFields.contains(parentParamName + propertyName)) {
					continue;
				}
				final Class<?> propertyType = propertyDescriptor.getPropertyType();

				Object propertyValue = PropertyUtils.getPropertyOrFieldValue(currentCallValue, propertyName);
				MethodParameter methodParameter = new MethodParameter(propertyDescriptor.getWriteMethod(), 0);

				invokeHandlerOrFollorRecurse(methodParameter, annotatedParameter, annotatedParameters, parentParamName,
						propertyName, propertyType, propertyValue, knownConstructorFields, methodHandler);

			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to write input fields for constructor", e);
		}
	}

	private static String invokeHandlerOrFollorRecurse(MethodParameter methodParameter,
			ActionInputParameter annotatedParameter, ActionDescriptor annotatedParameters, String parentParamName,
			String paramName, Class<?> parameterType, Object propertyValue, Set<String> fields,
			MethodParameterHandler handler) {
		if (DataType.isSingleValueType(parameterType) || DataType.isArrayOrCollection(parameterType)) {

			if (annotatedParameter.isIncluded(paramName) && !fields.contains(parentParamName + paramName)) {

				return handler.onMethodParameter(methodParameter, annotatedParameter, annotatedParameters, parentParamName,
						paramName, parameterType, propertyValue);
			}

		} else {
			Object callValueBean;
			if (propertyValue instanceof Resource) {
				callValueBean = ((Resource<?>) propertyValue).getContent();
			} else {
				callValueBean = propertyValue;
			}
			recurseBeanCreationParams(parameterType, annotatedParameters, annotatedParameter, callValueBean, paramName + ".",
					fields, handler);
		}

		return null;
	}

	private static BeanInfo getBeanInfo(Class<?> beanType) {
		try {
			return Introspector.getBeanInfo(beanType);
		} catch (IntrospectionException e) {
			throw new RuntimeException(e);
		}
	}

	public static interface MethodParameterHandler {
		String onMethodParameter(MethodParameter methodParameter, ActionInputParameter annotatedParameter,
				ActionDescriptor annotatedParameters, String parentParamName, String paramName, Class<?> parameterType,
				Object propertyValue);
	}
}
