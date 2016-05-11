/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.PartialUriTemplate;

/**
 * Factory for {@link AffordanceBuilder}s in a Spring MVC rest service. Normally one should use the static methods of
 * AffordanceBuilder to get an AffordanceBuilder. Created by dschulten on 03.10.2014.
 */
public class AffordanceBuilderFactory implements MethodLinkBuilderFactory<AffordanceBuilder> {

	private static final MappingDiscoverer MAPPING_DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

	@Override
	public AffordanceBuilder linkTo(Method method, Object... parameters) {
		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Method method, Object... parameters) {

		String pathMapping = MAPPING_DISCOVERER.getMapping(controller, method);

		final Set<String> params = getRequestParamNames(method, parameters);
		String query = join(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(
				AffordanceBuilder.getBuilder().build().toString() + mapping);

		Map<String, Object> values = new HashMap<String, Object>();

		Iterator<String> names = partialUriTemplate.getVariableNames().iterator();
		// there may be more or less mapping variables than arguments
		for (Object parameter : parameters) {
			if (!names.hasNext()) {
				break;
			}
			values.put(names.next(), parameter);
		}

		ActionDescriptor actionDescriptor = ActionDescriptorBuilder.createActionDescriptor(method, values, parameters);

		return new AffordanceBuilder(partialUriTemplate.expand(values), Collections.singletonList(actionDescriptor));
	}

	private String join(Set<String> params) {
		StringBuilder sb = new StringBuilder();
		for (String param : params) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(param);
		}
		return sb.toString();
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> target) {
		return linkTo(target, new Object[0]);
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Object... parameters) {
		Assert.notNull(controller);

		String mapping = MAPPING_DISCOVERER.getMapping(controller);

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(mapping == null ? "/" : mapping);

		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = partialUriTemplate.getVariableNames().iterator();
		// there may be more or less mapping variables than arguments
		for (Object parameter : parameters) {
			if (!names.hasNext()) {
				break;
			}
			values.put(names.next(), parameter);
		}
		return new AffordanceBuilder().slash(partialUriTemplate.expand(values));
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {
		String mapping = MAPPING_DISCOVERER.getMapping(controller);
		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(mapping == null ? "/" : mapping);
		return new AffordanceBuilder().slash(partialUriTemplate.expand(parameters));
	}

	@Override
	public AffordanceBuilder linkTo(Object invocationValue) {

		Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, invocationValue);
		DummyInvocationUtils.LastInvocationAware invocations = (DummyInvocationUtils.LastInvocationAware) invocationValue;

		DummyInvocationUtils.MethodInvocation invocation = invocations.getLastInvocation();
		Method invokedMethod = invocation.getMethod();

		String pathMapping = MAPPING_DISCOVERER.getMapping(invokedMethod);

		Set<String> params = getRequestParamNames(invokedMethod, invocation.getArguments());
		String query = join(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		PartialUriTemplate partialUriTemplate = new PartialUriTemplate(
				AffordanceBuilder.getBuilder().build().toString() + mapping);

		Iterator<Object> classMappingParameters = invocations.getObjectParameters();

		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = partialUriTemplate.getVariableNames().iterator();
		while (classMappingParameters.hasNext()) {
			values.put(names.next(), classMappingParameters.next());
		}

		for (Object argument : invocation.getArguments()) {
			if (names.hasNext()) {
				values.put(names.next(), argument);
			}
		}

		ActionDescriptor actionDescriptor = ActionDescriptorBuilder.createActionDescriptor(invocation.getMethod(), values,
				invocation.getArguments());

		return new AffordanceBuilder(partialUriTemplate.expand(values), Collections.singletonList(actionDescriptor));
	}

	private Set<String> getRequestParamNames(Method invokedMethod, Object[] arguments) {
		return ActionDescriptorBuilder.getRequestParams(invokedMethod, arguments).keySet();
	}
}
