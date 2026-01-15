/*
 * Copyright 2025-2026 the original author or authors.
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

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * An extension of {@link MappingDiscoverer} that allows access to the raw {@link String}-based mapping before turning
 * it into a {@link UriMapping}.
 *
 * @author Oliver Drotbohm
 * @since 3.0
 */
interface RawMappingDiscoverer extends MappingDiscoverer {

	/**
	 * Returns the raw URI mapping associated with the given method of the given class.
	 *
	 * @param type can be {@literal null}.
	 * @param method can be {@literal null}.
	 * @return can be {@literal null}.
	 */
	@Nullable
	String getMapping(@Nullable Class<?> type, @Nullable Method method);
}
