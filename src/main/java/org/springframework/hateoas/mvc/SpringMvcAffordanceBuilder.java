/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mvc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.core.AffordanceModelFactory;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponents;

/**
 * Extract information needed to assemble an {@link Affordance} from a Spring MVC web method.
 * 
 * @author Greg Turnquist
 */
class SpringMvcAffordanceBuilder {

	/**
	 * Use the attributes of the current method call along with a collection of {@link AffordanceModelFactory}'s to create
	 * a set of {@link Affordance}s.
	 * 
	 * @param invocation
	 * @param discoverer
	 * @param components
	 * @return
	 */
	public static Collection<Affordance> create(MethodInvocation invocation, MappingDiscoverer discoverer,
			UriComponents components) {

		List<Affordance> affordances = new ArrayList<>();

		for (HttpMethod requestMethod : discoverer.getRequestMethod(invocation.getTargetType(), invocation.getMethod())) {

			String methodName = invocation.getMethod().getName();

			Link affordanceLink = new Link(components.toUriString()).withRel(methodName);

			MethodParameters invocationMethodParameters = new MethodParameters(invocation.getMethod());
			
			ResolvableType inputType = invocationMethodParameters.getParametersWith(RequestBody.class).stream()
				.findFirst()
				.map(ResolvableType::forMethodParameter)
				.orElse(ResolvableType.NONE);

			List<QueryParameter> queryMethodParameters = invocationMethodParameters.getParametersWith(RequestParam.class).stream()
				.map(methodParameter -> methodParameter.getParameterAnnotation(RequestParam.class))
				.map(requestParam -> new QueryParameter(requestParam.name(), requestParam.value(), requestParam.required()))
				.collect(Collectors.toList());

			ResolvableType outputType = ResolvableType.forMethodReturnType(invocation.getMethod());

			affordances.add(new Affordance(methodName, affordanceLink, requestMethod, inputType, queryMethodParameters, outputType));

		}

		return affordances;
	}
}
