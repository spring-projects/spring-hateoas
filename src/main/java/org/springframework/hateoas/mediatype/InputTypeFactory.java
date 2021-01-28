/*
 * Copyright 2021 the original author or authors.
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

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

/**
 * SPI interface for components that can derive an input type from a {@link ResolvableType}. Primary usage to enable to
 * default the resolution to use HTML based {@link org.springframework.hateoas.mediatype.html.HtmlInputType} without
 * creating a package cycle. If you want to replace that implementation with a different one, register it in
 * {@code META-INF/spring.factories}.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 */
public interface InputTypeFactory {

	/**
	 * Derive an input type from the given {@link ResolvableType}.
	 *
	 * @param type type to resolve the input type for, will never be {@literal null}.
	 * @return the input type for the given type or {@literal null} if no type could be derived.
	 */
	@Nullable
	String getInputType(Class<?> type);
}
