/*
 * Copyright 2012-14 the original author or authors.
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
package org.springframework.hateoas.jaxrs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.MethodLinkBuilderFactory;
import org.springframework.hateoas.core.AnnotatedParametersParameterAccessor;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.mvc.UriComponentsContributor;

/**
 * Factory for {@link LinkBuilder} instances based on the path mapping annotated on the given JAX-RS service.
 *
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 */
public class JaxRsLinkBuilderFactory implements MethodLinkBuilderFactory<JaxRsLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(Path.class);
	private static final AnnotatedParametersParameterAccessor PATH_VARIABLE_ACCESSOR = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(PathParam.class), false);
	private static final AnnotatedParametersParameterAccessor REQUEST_PARAM_ACCESSOR = new AnnotatedParametersParameterAccessor(
			new AnnotationAttribute(QueryParam.class), false);

	private List<UriComponentsContributor> uriComponentsContributors = new ArrayList<UriComponentsContributor>();

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
	public JaxRsLinkBuilder linkTo(Class<?> service) {
		return JaxRsLinkBuilder.linkTo(service);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public JaxRsLinkBuilder linkTo(Class<?> service, Object... parameters) {
		return JaxRsLinkBuilder.linkTo(service, parameters);
	}

	@Override
	public JaxRsLinkBuilder linkTo(Method method, Object... parameters) {
		return JaxRsLinkBuilder.linkTo(method, parameters);
	}

	@Override
	public JaxRsLinkBuilder linkTo(Class<?> type, Method method, Object... parameters) {
		return JaxRsLinkBuilder.linkTo(type, method, parameters);
	}

	@Override
	public JaxRsLinkBuilder linkTo(Object invocationValue) {

		return new JaxRsLinkBuilder(LinkBuilderSupport.linkTo(uriComponentsContributors, DISCOVERER,
				PATH_VARIABLE_ACCESSOR, REQUEST_PARAM_ACCESSOR, invocationValue));
	}
}
