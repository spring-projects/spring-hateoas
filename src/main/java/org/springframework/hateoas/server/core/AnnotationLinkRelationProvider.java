/*
 * Copyright 2013-2019 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link LinkRelationProvider} that evaluates the {@link Relation} annotation on entity types.
 *
 * @author Oliver Gierke
 * @author Alexander Baetz
 * @author Greg Turnquist
 */
public class AnnotationLinkRelationProvider implements LinkRelationProvider, Ordered {

	private final Map<Class<?>, Relation> annotationCache = new HashMap<>();

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.LinkRelationProvider#getCollectionResourceRelFor(java.lang.Class)
	 */
	@Override
	public LinkRelation getCollectionResourceRelFor(Class<?> type) {

		Relation annotation = lookupAnnotation(type);

		if (annotation == null || Relation.NO_RELATION.equals(annotation.collectionRelation())) {
			throw new IllegalArgumentException(String.format("No collection relation found for type %s!", type.getName()));
		}

		return LinkRelation.of(annotation.collectionRelation());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.LinkRelationProvider#getItemResourceRelFor(java.lang.Class)
	 */
	@Override
	public LinkRelation getItemResourceRelFor(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");

		Relation annotation = lookupAnnotation(type);

		if (annotation == null || Relation.NO_RELATION.equals(annotation.value())) {
			throw new IllegalStateException(String.format("Type %s is not supported!", type.getName()));
		}

		return LinkRelation.of(annotation.value());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.core.Ordered#getOrder()
	 */
	@Override
	public int getOrder() {
		return 100;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(LookupContext context) {

		Relation relation = lookupAnnotation(context.getType());

		if (relation == null) {
			return false;
		}

		if (context.isItemRelationLookup()) {
			return !relation.value().equals(Relation.NO_RELATION);
		}

		if (context.isCollectionRelationLookup()) {
			return !relation.collectionRelation().equals(Relation.NO_RELATION);
		}

		return false;
	}

	@Nullable
	private Relation lookupAnnotation(Class<?> type) {
		return annotationCache.computeIfAbsent(type, key -> AnnotatedElementUtils.getMergedAnnotation(key, Relation.class));
	}
}
