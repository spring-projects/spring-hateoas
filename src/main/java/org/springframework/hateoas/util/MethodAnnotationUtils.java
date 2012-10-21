package org.springframework.hateoas.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodAnnotationUtils {

	public static <T extends Annotation, R extends Annotation> List<AnnotatedParam<T>> getParamsWithAnnotation(
			Method method, Class<T> annotation) {

		Annotation[][] paramAnnotations = method.getParameterAnnotations();
		Class<?>[] methodParameterTypes = method.getParameterTypes();

		List<AnnotatedParam<T>> ret = new ArrayList<AnnotatedParam<T>>();

		for (int i = 0; i < paramAnnotations.length; i++) {
			Class<?> argType = methodParameterTypes[i];
			for (int j = 0; j < paramAnnotations[i].length; j++) {
				Annotation paramAnnotation = paramAnnotations[i][j];
				Class<? extends Annotation> annotationType = paramAnnotation.annotationType();
				if (annotation == annotationType) {
					@SuppressWarnings("unchecked")
					T foundParamAnnotation = (T) paramAnnotation;
					AnnotatedParam<T> annotatedParam = new AnnotatedParam<T>(foundParamAnnotation, argType);
					ret.add(annotatedParam);
				}
			}
		}
		return ret;
	}

}
