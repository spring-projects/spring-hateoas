/*
 * Copyright 2013-2014 the original author or authors.
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
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.RelProvider;

/**
 * @author Oliver Gierke
 * @author Alexander Baetz
 */
@Order(100)
public class AnnotationRelProvider implements RelProvider, ApplicationContextAware {

	private ApplicationContext context;

	private final Map<Class<?>, Relation> annotationCache = new HashMap<Class<?>, Relation>();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForCollectionResource(java.lang.Class)
	 */
	@Override
	public String getCollectionResourceRelFor(Class<?> type) {

		Relation annotation = lookupAnnotation(type);

		if (annotation == null || Relation.NO_RELATION.equals(annotation.collectionRelation())) {
			return null;
		}

		return annotation.collectionRelation();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForSingleResource(java.lang.Object)
	 */
	@Override
	public String getItemResourceRelFor(Class<?> type) {

		Relation annotation = lookupAnnotation(type);

		if (annotation == null || Relation.NO_RELATION.equals(annotation.value())) {
			return null;
		}

		return annotation.value();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return lookupAnnotation(delimiter) != null;
	}

	private Relation lookupAnnotation(Class<?> type) {

		Relation relation = annotationCache.get(type);

		if (relation != null) {
			return relation;
		}

		Class controllerClass = getControllerTypeWithAnnotation(ExposesResourceFor.class, type);

		if (controllerClass != null) {

			relation = AnnotationUtils.getAnnotation(controllerClass, Relation.class);
		}

		if (relation == null) {

			relation = AnnotationUtils.getAnnotation(type, Relation.class);
		}

		if (relation != null) {
			annotationCache.put(type, relation);
		}

		return relation;
	}

	private Class<?> getControllerTypeWithAnnotation(Class<? extends Annotation> annotationType, Class annotationValue) {

		if (annotationValue == Object.class) {
			return null;
		}

		for (String beanName : context.getBeanDefinitionNames()) {

			Annotation annotation = context.findAnnotationOnBean(beanName, annotationType);
			if (annotation != null && AnnotationUtils.getValue(annotation) == annotationValue) {

				return context.getType(beanName);
			}
		}

		return getControllerTypeWithAnnotation(annotationType, annotationValue.getSuperclass());
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		this.context = applicationContext;
	}
}
