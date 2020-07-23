/*
 * Copyright 2018-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import org.springframework.hateoas.AbstractCollectionModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import com.fasterxml.jackson.databind.JavaType;

/**
 * Jackson utility methods.
 */
public final class JacksonHelper {

	/**
	 * Navigate a chain of parametric types (e.g. Resources&lt;Resource&lt;String&gt;&gt;) until you find the innermost type (String).
	 *
	 * @param contentType
	 * @return
	 */
	public static JavaType findRootType(JavaType contentType) {

		if (contentType.hasGenericTypes()) {
			return findRootType(contentType.containedType(0));
		} else {
			return contentType;
		}
	}

	/**
	 * Is this a {@literal Resources<Resource<?>>}?
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isResourcesOfResource(JavaType type) {

		return
			AbstractCollectionModel.class.isAssignableFrom(type.getRawClass())
			&&
			EntityModel.class.isAssignableFrom(type.containedType(0).getRawClass());
	}
}
