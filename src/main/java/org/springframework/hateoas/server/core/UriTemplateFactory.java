/*
 * Copyright 2019 the original author or authors.
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

import java.util.Map;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.util.UriTemplate;

/**
 * Builds and caches {@link UriTemplate} instances.
 *
 * @author Michal Stochmialek
 * @author Oliver Drotbohm
 */
public class UriTemplateFactory {

	private static final Map<String, UriTemplate> CACHE = new ConcurrentReferenceHashMap<>();

	/**
	 * Returns the the {@link UriTemplate} for the given mapping.
	 *
	 * @param mapping must not be {@literal null} or empty.
	 * @return
	 */
	public static UriTemplate templateFor(@Nullable String mapping) {

		Assert.hasText(mapping, "Mapping must not be null or empty!");

		return CACHE.computeIfAbsent(mapping, UriTemplate::new);
	}
}
