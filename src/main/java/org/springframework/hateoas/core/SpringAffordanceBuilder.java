/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModelFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;

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
	 * @param invocation
	 * @param discoverer
	 * @param components
	 * @return
	 */
	public static List<Affordance> create(MethodInvocation invocation, MappingDiscoverer discoverer,
			UriComponents components) {

		List<Affordance> affordances = new ArrayList<>();

		for (HttpMethod requestMethod : discoverer.getRequestMethod(invocation.getTargetType(), invocation.getMethod())) {

			String methodName = invocation.getMethod().getName();

			String href = components.toUriString().equals("") ? "/" : components.toUriString();
			Link affordanceLink = new Link(href).withRel(LinkRelation.of(methodName));

			MethodParameters invocationMethodParameters = new MethodParameters(invocation.getMethod());

			ResolvableType inputType = invocationMethodParameters.getParametersWith(RequestBody.class).stream() //
					.findFirst() //
					.map(ResolvableType::forMethodParameter) //
					.orElse(ResolvableType.NONE);

			List<QueryParameter> queryMethodParameters = invocationMethodParameters.getParametersWith(RequestParam.class)
					.stream() //
					.map(methodParameter -> methodParameter.getParameterAnnotation(RequestParam.class)) //
					.map(requestParam -> new QueryParameter(requestParam.name(), requestParam.value(), requestParam.required())) //
					.collect(Collectors.toList());

			ResolvableType outputType = ResolvableType.forMethodReturnType(invocation.getMethod());

			affordances
					.add(new Affordance(methodName, affordanceLink, requestMethod, inputType, queryMethodParameters, outputType));

		}

		return affordances;
	}
}
