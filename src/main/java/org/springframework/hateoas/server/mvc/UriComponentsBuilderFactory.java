/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import java.net.URI;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Factory class for {@link UriComponentsBuilder} instances caching the lookups to avoid unnecessary subsequent lookups.
 *
 * @author Michal Stochmialek
 * @author Oliver Gierke
 */
class UriComponentsBuilderFactory {

	static final String REQUEST_ATTRIBUTES_MISSING = "Could not find current request via RequestContextHolder. Is this being called from a Spring MVC handler?";
	private static final String CACHE_KEY = UriComponentsBuilderFactory.class.getName() + "#BUILDER_CACHE";

	/**
	 * Returns a {@link UriComponentsBuilder} obtained from the current servlet mapping with scheme tweaked in case the
	 * request contains an {@code X-Forwarded-Ssl} header. If no {@link RequestContextHolder} exists (you're outside a
	 * Spring Web call), fall back to relative URIs.
	 *
	 * @return
	 */
	public static UriComponentsBuilder getBuilder() {

		if (RequestContextHolder.getRequestAttributes() == null) {
			return UriComponentsBuilder.fromPath("/");
		}

		URI baseUri = getCachedBaseUri();

		return baseUri != null //
				? UriComponentsBuilder.fromUri(baseUri) //
				: cacheBaseUri(ServletUriComponentsBuilder.fromCurrentServletMapping());
	}

	public static UriComponents getComponents() {
		return getBuilder().build();
	}

	private static RequestAttributes getRequestAttributes() {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

		if (requestAttributes == null) {
			throw new IllegalStateException("Could not look up RequestAttributes!");
		}

		Assert.state(requestAttributes != null, REQUEST_ATTRIBUTES_MISSING);
		Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);

		return requestAttributes;
	}

	private static UriComponentsBuilder cacheBaseUri(UriComponentsBuilder builder) {

		URI uri = builder.build().toUri();

		getRequestAttributes().setAttribute(CACHE_KEY, uri, RequestAttributes.SCOPE_REQUEST);

		return builder;
	}

	@Nullable
	private static URI getCachedBaseUri() {
		return (URI) getRequestAttributes().getAttribute(CACHE_KEY, RequestAttributes.SCOPE_REQUEST);
	}
}
