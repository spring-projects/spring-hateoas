/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.hateoas;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;

/**
 * Collection of attributes needed to render any form of hypermedia.
 * 
 * @author Greg Turnquist
 */
@EqualsAndHashCode
@AllArgsConstructor
public class GenericAffordanceModel {

	/**
	 * Name for the REST action of this resource.
	 */
	private @Getter String name;

	/**
	 * {@link Link} for the URI of the resource.
	 */
	private @Getter Link link;

	/**
	 * Request method verb for this resource. For multiple methods, add multiple {@link Affordance}s.
	 */
	private @Getter HttpMethod httpMethod;

	/**
	 * Domain type used to create a new resource.
	 */
	private @Getter ResolvableType inputType;

	/**
	 * Collection of {@link QueryParameter}s to interrogate a resource.
	 */
	private @Getter List<QueryParameter> queryMethodParameters;
	
	/**
	 * Response body domain type.
	 */
	private @Getter ResolvableType outputType;
}
