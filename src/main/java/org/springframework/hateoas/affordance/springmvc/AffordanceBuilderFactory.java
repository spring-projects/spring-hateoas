/*
 * Copyright 2014-2017 the original author or authors.
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

package org.springframework.hateoas.affordance.springmvc;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.mvc.UriComponentsSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Factory for {@link AffordanceBuilder}s in a Spring MVC rest service. Normally one should use the static methods of
 * AffordanceBuilder to get an AffordanceBuilder.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public class AffordanceBuilderFactory implements MethodLinkBuilderFactory<AffordanceBuilder> {

	private static final MappingDiscoverer MAPPING_DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {

		String mapping = MAPPING_DISCOVERER.getMapping(controller);

		UriTemplate partialUriTemplate = new UriTemplate(mapping == null ? "/" : mapping);

		return new AffordanceBuilder().slash(partialUriTemplate.expand(parameters));
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Method method, Object... parameters) {

		String pathMapping = MAPPING_DISCOVERER.getMapping(controller, method);

		Set<String> params = ActionDescriptorBuilder.getRequestParamsAndDtoParams(method, parameters).keySet();

		String query = StringUtils.collectionToCommaDelimitedString(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		UriTemplate partialUriTemplate = new UriTemplate(UriComponentsSupport.getBuilder().build().toString() + mapping);

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

	@Override
	public AffordanceBuilder linkTo(Object invocationValue) {

		Assert.isInstanceOf(DummyInvocationUtils.LastInvocationAware.class, invocationValue);
		DummyInvocationUtils.LastInvocationAware invocations = (DummyInvocationUtils.LastInvocationAware) invocationValue;

		String pathMapping = MAPPING_DISCOVERER.getMapping(invocations.getLastInvocation().getMethod());

		Set<String> params = ActionDescriptorBuilder.getRequestParamsAndDtoParams(invocations.getLastInvocation().getMethod(), invocations.getLastInvocation().getArguments()).keySet();

		String query = StringUtils.collectionToCommaDelimitedString(params);
		String mapping = StringUtils.isEmpty(query) ? pathMapping : pathMapping + "{?" + query + "}";

		UriTemplate partialUriTemplate = new UriTemplate(UriComponentsSupport.getBuilder().build().toString() + mapping);

		Iterator<Object> classMappingParameters = invocations.getObjectParameters();

		Map<String, Object> values = new HashMap<String, Object>();
		Iterator<String> names = partialUriTemplate.getVariableNames().iterator();
		while (classMappingParameters.hasNext()) {
			values.put(names.next(), classMappingParameters.next());
		}

		for (Object argument : invocations.getLastInvocation().getArguments()) {
			if (names.hasNext()) {
				values.put(names.next(), argument);
			}
		}

		ActionDescriptor actionDescriptor = ActionDescriptorBuilder.createActionDescriptor(invocations.getLastInvocation().getMethod(), values,
			invocations.getLastInvocation().getArguments());

		return new AffordanceBuilder(partialUriTemplate.expand(values), Collections.singletonList(actionDescriptor));
	}

	@Override
	public AffordanceBuilder linkTo(Method method, Object... parameters) {
		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	@Override
	public AffordanceBuilder linkTo(Class<?> controller, Object... parameters) {
		
		Assert.notNull(controller, "controller must not be null!");

		String mapping = MAPPING_DISCOVERER.getMapping(controller);

		UriTemplate partialUriTemplate = new UriTemplate(mapping == null ? "/" : mapping);

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
	public AffordanceBuilder linkTo(Class<?> target) {
		return linkTo(target, new Object[0]);
	}
}
