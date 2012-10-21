package org.springframework.hateoas.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;

import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.LinkTemplate;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.util.MethodAnnotationUtils.AnnotatedParam;

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
		M annotation = AnnotationUtils.findAnnotation(method, methodAnnotation);
		// String[] params = (String[]) AnnotationUtils.getValue(annotation, "params");
		// TODO use MethodAnnotationUtil to get PathParams
		// List<AnnotatedParam<P>> params = MethodAnnotationUtils.getParamsWithAnnotation(method, pathParamAnnotation);

		String[] mappings = annotation == null ? new String[0] : (String[]) AnnotationUtils.getValue(annotation);

		if (mappings.length > 1) {
			throw new IllegalStateException("Multiple mappings defined on method" + method.getName());
		}
		List<AnnotatedParam<P>> pathVariables = MethodAnnotationUtils.getParamsWithAnnotation(method,
				pathVariableAnnotation);
		List<AnnotatedParam<R>> requestParams = MethodAnnotationUtils.getParamsWithAnnotation(method,
				requestParamAnnotation);

		final LinkTemplate<P, R> linkTemplate;
		if (classLevelMapping.length() == 0 && mappings.length == 0) {
			throw new IllegalStateException("No class level or method level request mappings found for method "
					+ method.getName());
		} else {
			final String methodMapping;
			if (mappings.length == 1) {
				methodMapping = mappings[0];
			} else {
				methodMapping = "";
			}
			linkTemplate = new LinkTemplate<P, R>(classLevelMapping + methodMapping, pathVariables, requestParams);
		}
		return linkTemplate;
	}

	public static <T extends Annotation> String getClassLevelMapping(Class<? extends Object> controller,
			Class<T> classAnnotation) {
		T classLevelAnnotation = AnnotationUtils.findAnnotation(controller, classAnnotation);

		String classLevelMapping;
		Object classLevelAnnotationValue = AnnotationUtils.getValue(classLevelAnnotation);
		if (classLevelAnnotationValue instanceof String[]) {
			String[] mappings = classLevelAnnotation == null ? new String[0] : (String[]) classLevelAnnotationValue;

			if (mappings.length > 1) {
				throw new IllegalStateException("Multiple mappings defined on " + controller.getName());
			}
			classLevelMapping = mappings[0];
		} else {
			classLevelMapping = (String) classLevelAnnotationValue;
		}

		final String ret;
		if (classLevelMapping != null) {
			ret = classLevelMapping.startsWith("/") ? classLevelMapping : "/" + classLevelMapping;
		} else {
			ret = "";
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
