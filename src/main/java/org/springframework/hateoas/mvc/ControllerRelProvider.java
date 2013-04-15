/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas.mvc;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.RelProvider;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Gierke
 */
public class ControllerRelProvider implements RelProvider {

	private final Class<?> entityType;
	private final String collectionResourceRel;
	private final String singleResourceRel;

	public ControllerRelProvider(Class<?> controller) {

		ExposesResourceFor annotation = AnnotationUtils.findAnnotation(controller, ExposesResourceFor.class);
		Assert.notNull(annotation);

		this.entityType = annotation.value();
		this.singleResourceRel = StringUtils.uncapitalize(entityType.getSimpleName());
		this.collectionResourceRel = singleResourceRel + "List";
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForCollectionResource(java.lang.Class)
	 */
	@Override
	public String getRelForCollectionResource(Object resource) {
		return collectionResourceRel;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForSingleResource(java.lang.Class)
	 */
	@Override
	public String getRelForSingleResource(Object resource) {
		return singleResourceRel;
	}
}
