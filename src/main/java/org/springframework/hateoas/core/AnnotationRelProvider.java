/*
 * Copyright 2013-2017 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.RelProvider;

/**
 * @author Oliver Gierke
 * @author Alexander Baetz
 */
@Order(100)
public class AnnotationRelProvider implements RelProvider {

	private final Map<Class<?>, Relation> annotationCache = new HashMap<>();

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
		return annotationCache.computeIfAbsent(type, key -> AnnotationUtils.getAnnotation(key, Relation.class));
	}
}
