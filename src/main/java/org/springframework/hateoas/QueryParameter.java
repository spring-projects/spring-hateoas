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
package org.springframework.hateoas;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Optional;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Representation of a web request's query parameter (https://example.com?name=foo) => {"name", "foo", true}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryParameter {

	private final String name;
	private final @Nullable @Wither String value;
	private final boolean required;

	/**
	 * Creates a new {@link QueryParameter} from the given {@link MethodParameter}.
	 *
	 * @param parameter must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static QueryParameter of(MethodParameter parameter) {

		MergedAnnotation<RequestParam> annotation = MergedAnnotations //
				.from(parameter.getParameter()) //
				.get(RequestParam.class);

		String name = annotation.isPresent() && annotation.hasNonDefaultValue("name") //
				? annotation.getString("name") //
				: parameter.getParameterName();

		if (name == null || !StringUtils.hasText(name)) {
			throw new IllegalStateException(String.format("Couldn't determine parameter name for %s!", parameter));
		}

		boolean required = annotation.isPresent() && annotation.hasNonDefaultValue("required") //
				? annotation.getBoolean("required") //
				: !Optional.class.equals(parameter.getParameterType()); //

		return required ? required(name) : optional(name);
	}

	/**
	 * Creates a new required {@link QueryParameter} with the given name;
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return
	 */
	public static QueryParameter required(String name) {

		Assert.hasText(name, "Name must not be null or empty!");

		return new QueryParameter(name, null, true);
	}

	/**
	 * Creates a new optional {@link QueryParameter} with the given name;
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return
	 */
	public static QueryParameter optional(String name) {
		return new QueryParameter(name, null, false);
	}
}
