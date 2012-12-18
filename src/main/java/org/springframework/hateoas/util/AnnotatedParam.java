package org.springframework.hateoas.util;

import java.lang.annotation.Annotation;

public class AnnotatedParam<T extends Annotation> {
	public final T paramAnnotation;
	public final Class<?> paramType;

	public AnnotatedParam(T paramAnnotation, Class<?> paramType) {
		super();
		this.paramAnnotation = paramAnnotation;
		this.paramType = paramType;
	}

	@Override
	public String toString() {
		return "AnnotatedParam [paramAnnotation=" + paramAnnotation + ", paramType=" + paramType + "]";
	}

}