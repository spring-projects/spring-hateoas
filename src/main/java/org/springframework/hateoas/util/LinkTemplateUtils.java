package org.springframework.hateoas.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

public class LinkTemplateUtils {

	ParameterNameDiscoverer nameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	/**
	 * Creates a LinkTemplate
	 *
	 * @param classLevelMapping
	 * @param method
	 * @param methodAnnotation annotation on the method which contains the path mapping for the method, e.g.
	 *          RequestMapping or Path
	 * @param pathVariableAnnotation annotation on method params which are used as path parameters, e.g. PathParam or
	 *          PathVariable.
	 * @return
	 */
	public static <M extends Annotation, P extends Annotation, R extends Annotation> LinkTemplate<P, R> createLinkTemplate(
			String classLevelMapping, Method method, Class<M> methodAnnotation, Class<P> pathVariableAnnotation,
			Class<R> requestParamAnnotation) {
		M methodLevelAnnotation = AnnotationUtils.findAnnotation(method, methodAnnotation);

		String methodMapping = getMappingAnnotationValue(methodLevelAnnotation);
		List<AnnotatedParam<P>> pathVariables = MethodAnnotationUtils.getParamsWithAnnotation(method,
				pathVariableAnnotation);
		List<AnnotatedParam<R>> requestParams = MethodAnnotationUtils.getParamsWithAnnotation(method,
				requestParamAnnotation);

		final LinkTemplate<P, R> linkTemplate;
		if (classLevelMapping.length() == 0 && methodMapping.length() == 0) {
			throw new IllegalStateException("No class level or method level request mappings found for method "
					+ method.getName());
		}
		linkTemplate = new LinkTemplate<P, R>(classLevelMapping + methodMapping, pathVariables, requestParams);

		return linkTemplate;
	}

	public static <T extends Annotation> String getClassLevelMapping(Class<? extends Object> controller,
			Class<T> classAnnotation) {

		T classLevelAnnotation = AnnotationUtils.findAnnotation(controller, classAnnotation);
		String classLevelMapping = getMappingAnnotationValue(classLevelAnnotation);
		return classLevelMapping;
	}

	private static <T extends Annotation> String getMappingAnnotationValue(T mappingAnnotation) {
		String classLevelMapping;
		Object classLevelAnnotationValue = AnnotationUtils.getValue(mappingAnnotation);
		if (classLevelAnnotationValue != null) {
			if (classLevelAnnotationValue instanceof String[]) {
				String[] mappings = mappingAnnotation == null ? new String[0] : (String[]) classLevelAnnotationValue;

				if (mappings.length > 1) {
					throw new IllegalStateException("Multiple mapping values found, only one mapping is supported: "
							+ Arrays.toString(mappings));
				}
				classLevelMapping = mappings[0];
			} else {
				classLevelMapping = (String) classLevelAnnotationValue;
			}
		} else {
			classLevelMapping = "";
		}
		final String ret;
		if (classLevelMapping.length() > 0) {
			ret = classLevelMapping.startsWith("/") ? classLevelMapping : "/" + classLevelMapping;
		} else {
			ret = classLevelMapping;
		}
		return ret;
	}

	/**
	 * Allows to create a representation of a method on the given controller, for use with
	 * {@link ControllerLinkBuilder#linkToMethod(Object)}. Define the method representation by simply calling the desired
	 * method as shown below.
	 * <p>
	 * This example creates a representation of the method <code>PersonController.showAll()</code>:
	 *
	 * <pre>
	 * on(PersonController.class).showAll();
	 * </pre>
	 *
	 * @param controller
	 * @return
	 * @see #linkToMethod(Object)
	 */
	public static <T> T on(Class<T> controller) {

		Invocations invocations = new InvocationsImpl();

		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(controller);
		enhancer.setCallback(new RecordingMethodInterceptor(invocations));
		enhancer.setInterfaces(new Class<?>[] { Invocations.class });
		@SuppressWarnings("unchecked")
		T ret = (T) enhancer.create();
		return ret;
	}
}
