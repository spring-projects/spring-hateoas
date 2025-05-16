/*
 * Copyright 2013-2024 the original author or authors.
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

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.server.MethodLinkBuilderFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * SPI callback to enhance a {@link UriComponentsBuilder} when referring to a method through a dummy method invocation.
 * Will usually be implemented in implementations of {@link HandlerMethodArgumentResolver} as they represent exactly the
 * same functionality inverted.
 *
 * @see MethodLinkBuilderFactory#linkTo(Object)
 * @author Oliver Gierke
 * @author Réda Housni Alaoui
 */
public interface UriComponentsContributor {

	/**
	 * Returns whether the {@link UriComponentsBuilder} supports the given {@link MethodParameter}.
	 *
	 * @param parameter will never be {@literal null}.
	 * @return
	 */
	boolean supportsParameter(MethodParameter parameter);

	/**
	 * Enhance the given {@link UriComponentsBuilder} with the given value.
	 *
	 * @param builder will never be {@literal null}.
	 * @param parameter can be {@literal null}.
	 * @param value can be {@literal null}.
	 */
	void enhance(UriComponentsBuilder builder, @Nullable MethodParameter parameter, @Nullable Object value);

	/**
	 * Enhance the given {@link TemplateVariables}
	 *
	 * @param templateVariables will never be {@literal null}.
	 * @param uriComponents will never be {@literal null}.
	 * @param parameter will never be {@literal null}.
	 */
	default TemplateVariables enhance(TemplateVariables templateVariables, UriComponents uriComponents, MethodParameter parameter){
		return templateVariables;
	}
}
