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

package org.springframework.hateoas.affordance;

import org.springframework.util.Assert;

/**
 * Resource of a certain semantic type which may or may not be identifiable.
 *
 * @author Dietrich Schulten
 */
public class TypedResource {

	private String semanticType;
	private String identifyingUri;

	/**
	 * Creates a resource whose semantic type is known, but which cannot be identified as an individual.
	 *
	 * @param semanticType semantic type of the resource as string, either as Uri or Curie or as type name within the default vocabulary. Example: <code>Order</code> in a context where the default vocabulary is <code>http://schema.org/</code>
	 * @see <a href="http://www.w3.org/TR/curie/">Curie</a>
	 */
	public TypedResource(String semanticType) {

		Assert.notNull(semanticType, "semanticType must be given");
		this.semanticType = semanticType;
	}

	/**
	 * Creates identified resource of a semantic type.
	 *
	 * @param semanticType semantic type of the resource as string, either as Uri or Curie
	 * @param identifyingUri identifying an individual of the typed resource
	 * @see <a href="http://www.w3.org/TR/curie/">Curie</a>
	 */
	public TypedResource(String semanticType, String identifyingUri) {

		Assert.notNull(semanticType, "semanticType must be given");
		Assert.notNull(identifyingUri, "identifyingUri must be given");
		this.semanticType = semanticType;
		this.identifyingUri = identifyingUri;
	}

	public String getSemanticType() {
		return semanticType;
	}

	public String getIdentifyingUri() {
		return identifyingUri;
	}
}
