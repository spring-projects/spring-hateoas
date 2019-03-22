/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.server.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Simply helper to reference a dedicated attribute of an {@link Annotation}.
 * 
 * @author Oliver Gierke
 */
public class AnnotationAttribute {

	private final Class<? extends Annotation> annotationType;
	private final String attributeName;

	/**
	 * Creates a new {@link AnnotationAttribute} to the {@code value} attribute of the given {@link Annotation} type.
	 * 
	 * @param annotationType must not be {@literal null}.
	 */
	public AnnotationAttribute(Class<? extends Annotation> annotationType) {
		this(annotationType, null);
	}

	/**
	 * Creates a new {@link AnnotationAttribute} for the given {@link Annotation} type and annotation attribute name.
	 * 
	 * @param annotationType must not be {@literal null}.
	 * @param attributeName can be {@literal null}, defaults to {@code value}.
	 */
	public AnnotationAttribute(Class<? extends Annotation> annotationType, @Nullable String attributeName) {

		Assert.notNull(annotationType, "AnnotationType must not be null!");

		this.annotationType = annotationType;
		this.attributeName = attributeName;
	}

	/**
	 * Returns the annotation type.
	 * 
	 * @return the annotationType
	 */
	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	/**
	 * Reads the {@link Annotation} attribute's value from the given {@link MethodParameter}.
	 * 
	 * @param parameter must not be {@literal null}.
	 * @return
	 */
	@Nullable
	public String getValueFrom(MethodParameter parameter) {

		Assert.notNull(parameter, "MethodParameter must not be null!");
		Annotation annotation = parameter.getParameterAnnotation(annotationType);
		return annotation == null ? null : getValueFrom(annotation);
	}

	/**
	 * Reads the {@link Annotation} attribute's value from the given {@link AnnotatedElement}.
	 * 
	 * @param annotatedElement must not be {@literal null}.
	 * @return
	 */
	@Nullable
	public String getValueFrom(AnnotatedElement annotatedElement) {

		Assert.notNull(annotatedElement, "Annotated element must not be null!");
		Annotation annotation = annotatedElement.getAnnotation(annotationType);
		return annotation == null ? null : getValueFrom(annotation);
	}

	/**
	 * Returns the {@link Annotation} attribute's value from the given {@link Annotation}.
	 * 
	 * @param annotation must not be {@literal null}.
	 * @return
	 */
	@Nullable
	public String getValueFrom(Annotation annotation) {

		Assert.notNull(annotation, "Annotation must not be null!");
		return (String) (attributeName == null ? AnnotationUtils.getValue(annotation) : AnnotationUtils.getValue(
				annotation, attributeName));
	}
}
