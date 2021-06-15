/*
 * Copyright 2017-2021 the original author or authors.
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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.ConcurrentLruCache;
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

	@SuppressWarnings("deprecation") //
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
	 * @return
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
	 * @return
	 */
	@Nullable
	public static String getMapping(Class<?> type, Method method) {
		return DISCOVERER.getMapping(type, method);
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

		List<QueryParameter> queryMethodParameters = parameters.getParametersWith(RequestParam.class).stream() //
				.map(QueryParameter::of) //
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
