/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;

/**
 * @author Oliver Gierke
 */
public class AnnotationAttribute {

	private final Class<? extends Annotation> annotationType;
	private final String attributeName;

	public AnnotationAttribute(Class<? extends Annotation> annotationType) {
		this(annotationType, null);
	}

	/**
	 * @param annotationType
	 * @param attributeName
	 */
	public AnnotationAttribute(Class<? extends Annotation> annotationType, String attributeName) {

		Assert.notNull(annotationType);

		this.annotationType = annotationType;
		this.attributeName = attributeName;
	}

	/**
	 * @return the annotationType
	 */
	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	public String getValueFrom(AnnotatedElement annotatedElement) {

		Annotation annotation = annotatedElement.getAnnotation(annotationType);
		return (String) (attributeName == null ? AnnotationUtils.getValue(annotation) : AnnotationUtils.getValue(
				annotation, attributeName));
	}

	public String getValueFrom(Annotation annotation) {

		return (String) (attributeName == null ? AnnotationUtils.getValue(annotation) : AnnotationUtils.getValue(
				annotation, attributeName));
	}
}
