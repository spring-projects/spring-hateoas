/*
 * Copyright 2017-2020 the original author or authors.
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
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.AffordanceModelFactory;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Extract information needed to assemble an {@link Affordance} from a Spring MVC web method.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class SpringAffordanceBuilder {

	/**
	 * Use the attributes of the current method call along with a collection of {@link AffordanceModelFactory}'s to create
	 * a set of {@link Affordance}s.
	 *
	 * @param type must not be {@literal null}.
	 * @param method must not be {@literal null}.
	 * @param href must not be {@literal null}.
	 * @param discoverer must not be {@literal null}.
	 * @return
	 */
	public static List<Affordance> create(Class<?> type, Method method, String href, MappingDiscoverer discoverer) {

		String methodName = method.getName();
		Link affordanceLink = Link.of(href, LinkRelation.of(methodName));

		MethodParameters parameters = MethodParameters.of(method);

		ResolvableType inputType = parameters.getParametersWith(RequestBody.class).stream() //
				.findFirst() //
				.map(ResolvableType::forMethodParameter) //
				.orElse(ResolvableType.NONE);

		List<QueryParameter> queryMethodParameters = parameters.getParametersWith(RequestParam.class).stream() //
				.map(QueryParameter::of) //
				.collect(Collectors.toList());

		ResolvableType outputType = ResolvableType.forMethodReturnType(method);
		Affordances affordances = Affordances.of(affordanceLink);

		return discoverer.getRequestMethod(type, method).stream() //
				.flatMap(it -> affordances.afford(it) //
						.withInput(inputType) //
						.withOutput(outputType) //
						.withParameters(queryMethodParameters) //
						.withName(methodName) //
						.build() //
						.stream()) //
				.collect(Collectors.toList());
	}
}
