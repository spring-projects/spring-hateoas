/*
 * Copyright 2017-2024 the original author or authors.
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Extract information needed to assemble an {@link Affordance} from a Spring MVC web method.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class SpringAffordanceBuilder {

	public static final MappingDiscoverer DISCOVERER = CachingMappingDiscoverer
			.of(new PropertyResolvingMappingDiscoverer(new AnnotationMappingDiscoverer(RequestMapping.class)));

	private static final ConcurrentLruCache<AffordanceKey, Function<Affordances, List<Affordance>>> AFFORDANCES_CACHE = new ConcurrentLruCache<>(
			256, key -> SpringAffordanceBuilder.create(key.type, key.method));

	/**
	 * Returns all {@link Affordance}s for the given type's method and base URI.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @param href must not be {@literal null} or empty.
	 * @return list of affordances for the method
	 */
	public static List<Affordance> getAffordances(Class<?> type, Method method, String href) {

		String methodName = method.getName();
		Link affordanceLink = Link.of(href, LinkRelation.of(methodName));

		return AFFORDANCES_CACHE
				.get(new AffordanceKey(type, method))
				.apply(Affordances.of(affordanceLink));
	}

	/**
	 * Returns the mapping for the given type's method.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @return the URI mapping for the method
	 * @since 2.0
	 */
	public static UriMapping getUriMapping(Class<?> type, Method method) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		UriMapping mapping = DISCOVERER.getUriMapping(type, method);

		if (mapping == null) {
			throw new IllegalArgumentException("No mapping found for %s!".formatted(method.toString()));
		}

		return mapping;
	}

	private static Function<Affordances, List<Affordance>> create(Class<?> type, Method method) {

		String methodName = method.getName();
		ResolvableType outputType = ResolvableType.forMethodReturnType(method);
		Collection<HttpMethod> requestMethods = DISCOVERER.getRequestMethod(type, method);
		List<MediaType> inputMediaTypes = DISCOVERER.getConsumes(method);

		MethodParameters parameters = MethodParameters.of(method);

		ResolvableType inputType = parameters.getParametersWith(RequestBody.class).stream() //
				.findFirst() //
				.map(ResolvableType::forMethodParameter) //
				.orElse(ResolvableType.NONE);

		// Include both @RequestParam and @ModelAttribute parameters
		List<QueryParameter> queryMethodParameters = parameters.getParameters().stream()
				.filter(it -> shouldIncludeAsQueryParameter(it))
				.map(QueryParameter::of)
				.collect(Collectors.toList());

		return affordances -> requestMethods.stream() //
				.flatMap(it -> affordances.afford(it) //
						.withInput(inputType) //
						.withOutput(outputType) //
						.withParameters(queryMethodParameters) //
						.withName(methodName) //
						.withInputMediaTypes(inputMediaTypes) //
						.build() //
						.stream()) //
				.collect(Collectors.toList());
	}

	/**
	 * Determines if a method parameter should be included as a query parameter.
	 * Includes @RequestParam, @ModelAttribute (explicit and implicit), but excludes
	 * Map parameters and @RequestBody parameters.
	 */
	private static boolean shouldIncludeAsQueryParameter(org.springframework.core.MethodParameter parameter) {
		// Exclude Map parameters (existing logic)
		if (Map.class.isAssignableFrom(parameter.getParameterType())) {
			return false;
		}

		// Exclude @RequestBody parameters
		if (parameter.hasParameterAnnotation(RequestBody.class)) {
			return false;
		}

		// Include @RequestParam parameters
		if (parameter.hasParameterAnnotation(RequestParam.class)) {
			return true;
		}

		// Include @ModelAttribute parameters
		if (parameter.hasParameterAnnotation(ModelAttribute.class)) {
			return true;
		}

		// Include implicit @ModelAttribute (complex objects without other annotations)
		return isImplicitModelAttribute(parameter);
	}

	/**
	 * Checks if a parameter is an implicit @ModelAttribute according to Spring MVC rules.
	 */
	private static boolean isImplicitModelAttribute(org.springframework.core.MethodParameter parameter) {
		Class<?> parameterType = parameter.getParameterType();

		// Simple types are not implicit @ModelAttribute
		if (isSimpleValueType(parameterType)) {
			return false;
		}

		// Check if it's annotated with other Spring MVC annotations
		return !parameter.hasParameterAnnotation(RequestParam.class) &&
			   !parameter.hasParameterAnnotation(RequestBody.class) &&
			   !parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.PathVariable.class) &&
			   !parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestHeader.class) &&
			   !parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.CookieValue.class) &&
			   !parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestPart.class) &&
			   !parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.SessionAttribute.class) &&
			   !parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestAttribute.class);
	}

	/**
	 * Determines if a type is a simple value type that should not be treated as @ModelAttribute.
	 */
	private static boolean isSimpleValueType(Class<?> type) {
		return type.isPrimitive() ||
			   type == String.class ||
			   Number.class.isAssignableFrom(type) ||
			   type == Boolean.class ||
			   type.isEnum() ||
			   java.util.Date.class.isAssignableFrom(type) ||
			   java.time.temporal.Temporal.class.isAssignableFrom(type) ||
			   type == java.net.URI.class ||
			   type == java.net.URL.class ||
			   type == java.util.Locale.class ||
			   type == java.util.TimeZone.class ||
			   type == java.io.InputStream.class ||
			   type == java.io.Reader.class ||
			   type == org.springframework.web.multipart.MultipartFile.class;
	}

	private static final class AffordanceKey {

		private final Class<?> type;
		private final Method method;

		AffordanceKey(Class<?> type, Method method) {

			this.type = type;
			this.method = method;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(@Nullable Object o) {

			if (this == o) {
				return true;
			}

			if (!(o instanceof AffordanceKey)) {
				return false;
			}

			AffordanceKey that = (AffordanceKey) o;

			return Objects.equals(this.type, that.type) //
					&& Objects.equals(this.method, that.method);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(this.type, this.method);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "WebHandler.AffordanceKey(type=" + this.type + ", method=" + this.method + ")";
		}
	}
}
