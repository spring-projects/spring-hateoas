/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.hateoas;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

/**
 * Collection of attributes needed to render any form of hypermedia.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@EqualsAndHashCode
@AllArgsConstructor
@Getter
public abstract class AffordanceModel {

	/**
	 * Name for the REST action of this resource.
	 */
	private String name;

	/**
	 * {@link Link} for the URI of the resource.
	 */
	private Link link;

	/**
	 * Request method verb for this resource. For multiple methods, add multiple {@link Affordance}s.
	 */
	private HttpMethod httpMethod;

	/**
	 * Domain type used to create a new resource.
	 */
	private ResolvableType inputType;

	/**
	 * Collection of {@link QueryParameter}s to interrogate a resource.
	 */
	private List<QueryParameter> queryMethodParameters;

	/**
	 * Response body domain type.
	 */
	private ResolvableType outputType;

	/**
	 * Expand the {@link Link} into an {@literal href} with no parameters.
	 *
	 * @return
	 */
	public String getURI() {
		return this.link.expand().getHref();
	}

	/**
	 * Returns whether the {@link Affordance} has the given {@link HttpMethod}.
	 *
	 * @param method must not be {@literal null}.
	 * @return
	 */
	public boolean hasHttpMethod(HttpMethod method) {

		Assert.notNull(method, "HttpMethod must not be null!");

		return this.httpMethod.equals(method);
	}

	/**
	 * Returns whether the {@link Affordance} points to the target of the given {@link Link}.
	 *
	 * @param link must not be {@literal null}.
	 * @return
	 */
	public boolean pointsToTargetOf(Link link) {

		Assert.notNull(link, "Link must not be null!");

		return getURI().equals(link.expand().getHref());
	}
}
