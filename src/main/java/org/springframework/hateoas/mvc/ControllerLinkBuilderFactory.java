/*
 * Copyright 2012-2019 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.hateoas.WebHandler;

/**
 * Factory for {@link LinkBuilderSupport} instances based on the request mapping annotated on the given controller.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Dietrich Schulten
 * @author Kamill Sokol
 * @author Ross Turner
 * @author Oemer Yildiz
 * @author Kevin Conaway
 * @author Andrew Naydyonock
 * @author Greg Turnquist
 */
public class ControllerLinkBuilderFactory implements MethodLinkBuilderFactory<ControllerLinkBuilder> {

	private List<UriComponentsContributor> uriComponentsContributors = new ArrayList<>();

	/**
	 * Configures the {@link UriComponentsContributor} to be used when building {@link Link} instances from method
	 * invocations.
	 * 
	 * @see #linkTo(Object)
	 * @param uriComponentsContributors the uriComponentsContributors to set
	 */
	public void setUriComponentsContributors(List<? extends UriComponentsContributor> uriComponentsContributors) {
		this.uriComponentsContributors = Collections.unmodifiableList(uriComponentsContributors);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class)
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller) {
		return ControllerLinkBuilder.linkTo(controller);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller, Object... parameters) {
		return ControllerLinkBuilder.linkTo(controller, parameters);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.util.Map)
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {
		return ControllerLinkBuilder.linkTo(controller, parameters);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.Class, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public ControllerLinkBuilder linkTo(Class<?> controller, Method method, Object... parameters) {
		return ControllerLinkBuilder.linkTo(controller, method, parameters);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.Object)
	 */
	@Override
	public ControllerLinkBuilder linkTo(Object invocationValue) {

		return WebHandler.linkTo(invocationValue,
			mapping -> ControllerLinkBuilder.getBuilder().path(mapping),
			(builder, invocation) -> {
			
				MethodParameters parameters = new MethodParameters(invocation.getMethod());
				Iterator<Object> parameterValues = Arrays.asList(invocation.getArguments()).iterator();

				for (MethodParameter parameter : parameters.getParameters()) {
					Object parameterValue = parameterValues.next();
					
					for (UriComponentsContributor contributor : this.uriComponentsContributors) {
						
						if (contributor.supportsParameter(parameter)) {
							contributor.enhance(builder, parameter, parameterValue);
						}
					}
				}

				return builder;
			});
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public ControllerLinkBuilder linkTo(Method method, Object... parameters) {
		return ControllerLinkBuilder.linkTo(method, parameters);
	}
}
