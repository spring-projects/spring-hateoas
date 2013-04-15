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
package org.springframework.hateoas.hal;

import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;

/**
 * @author Oliver Gierke
 */
public class AnnotationRelProvider implements RelProvider {

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForCollectionResource(java.lang.Object)
	 */
	@Override
	public String getRelForCollectionResource(Object resource) {
		// check for hateoas wrapper type
		if (Resource.class.isInstance(resource)) {
			resource = ((Resource) resource).getContent();
		}

		HalRelation annotation = resource.getClass().getAnnotation(HalRelation.class);
		if (annotation == null || HalRelation.NO_RELATION.equals(annotation.collectionRelation())) {
			return null;
		}
		return annotation.value();
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForSingleResource(java.lang.Object)
	 */
	@Override
	public String getRelForSingleResource(Object resource) {
		// check for hateoas wrapper type
		if (Resource.class.isInstance(resource)) {
			resource = ((Resource) resource).getContent();
		}

		HalRelation annotation = resource.getClass().getAnnotation(HalRelation.class);
		if (annotation == null || HalRelation.NO_RELATION.equals(annotation.value())) {
			return null;
		}
		return annotation.value();
	}
}
