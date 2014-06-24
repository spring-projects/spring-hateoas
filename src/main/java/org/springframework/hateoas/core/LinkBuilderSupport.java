/*
 * Copyright 2012-2014 the original author or authors.
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

import static org.springframework.web.util.UriComponentsBuilder.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.core.AnnotatedParametersParameterAccessor.BoundMethodParameter;
import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.hateoas.mvc.UriComponentsContributor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Base class to implement {@link LinkBuilder}s based on a Spring MVC {@link UriComponentsBuilder}.
 *
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
public abstract class LinkBuilderSupport<T extends LinkBuilder> implements LinkBuilder {

	private final UriComponents uriComponents;

	/**
	 * Creates a new {@link LinkBuilderSupport} using the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	public LinkBuilderSupport(UriComponentsBuilder builder) {

		Assert.notNull(builder);
		this.uriComponents = builder.build();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(java.lang.Object)
	 */
	@Override
	public T slash(Object object) {

		if (object == null) {
			return getThis();
		}

		if (object instanceof Identifiable) {
			return slash((Identifiable<?>) object);
		}

		String path = object.toString();

		if (path.endsWith("#")) {
			path = path.substring(0, path.length() - 1);
		}

		if (!StringUtils.hasText(path)) {
			return getThis();
		}

		String uriString = uriComponents.toUriString();
		UriComponentsBuilder builder = uriString.isEmpty() ? fromUri(uriComponents.toUri())
				: fromUriString(uriString);

		UriComponents components = UriComponentsBuilder.fromUriString(path).build();

		for (String pathSegment : components.getPathSegments()) {
			builder.pathSegment(pathSegment);
		}

		String fragment = components.getFragment();
		if (StringUtils.hasText(fragment)) {
			builder.fragment(fragment);
		}

		return createNewInstance(builder.query(components.getQuery()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(org.springframework.hateoas.Identifiable)
	 */
	@Override
	public T slash(Identifiable<?> identifyable) {

		if (identifyable == null) {
			return getThis();
		}

		return slash(identifyable.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#toUri()
	 */
	@Override
	public URI toUri() {
		return uriComponents.encode().toUri();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withRel(java.lang.String)
	 */
	@Override
	public Link withRel(String rel) {
		return new Link(this.toString(), rel);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withSelfRel()
	 */
	@Override
	public Link withSelfRel() {
		return withRel(Link.REL_SELF);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toUri().normalize().toASCIIString();
	}

	/**
	 * Returns the current concrete instance.
	 *
	 * @return
	 */
	protected abstract T getThis();

	/**
	 * Creates a new instance of the sub-class.
	 *
	 * @param builder will never be {@literal null}.
	 * @return
	 */
	protected abstract T createNewInstance(UriComponentsBuilder builder);

	public static UriComponentsBuilder linkTo(List<UriComponentsContributor> uriComponentsContributors, MappingDiscoverer discoverer, AnnotatedParametersParameterAccessor pathVariableAccessor, AnnotatedParametersParameterAccessor requestParamAccessor, Object invocationValue) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;

		MethodInvocation invocation = invocations.getLastInvocation();
		Iterator<Object> classMappingParameters = invocations.getObjectParameters();
		Method method = invocation.getMethod();

		String mapping = discoverer.getMapping(invocation.getTargetType(), method);
		UriComponentsBuilder builder = LinkBuilderSupport.getBuilder().path(mapping);

		UriTemplate template = new UriTemplate(mapping);
		Map<String, Object> values = new HashMap<String, Object>();

		Iterator<String> names = template.getVariableNames().iterator();
		while (classMappingParameters.hasNext()) {
			values.put(names.next(), classMappingParameters.next());
		}

		for (BoundMethodParameter parameter : pathVariableAccessor.getBoundParameters(invocation)) {
			values.put(parameter.getVariableName(), parameter.asString());
		}

		for (BoundMethodParameter parameter : requestParamAccessor.getBoundParameters(invocation)) {

			Object value = parameter.getValue();
			String key = parameter.getVariableName();

			if (value instanceof Collection) {
				for (Object element : (Collection<?>) value) {
					builder.queryParam(key, element);
				}
			} else {
				builder.queryParam(key, parameter.asString());
			}
		}

		UriComponents components = applyUriComponentsContributer(uriComponentsContributors, builder, invocation).buildAndExpand(values);
		return UriComponentsBuilder.fromUriString(components.toUriString());
	}

	/**
	 * Applies the configured {@link UriComponentsContributor}s to the given {@link UriComponentsBuilder}.
	 *
	 * @param builder will never be {@literal null}.
	 * @param invocation will never be {@literal null}.
	 * @return
	 */
	protected static UriComponentsBuilder applyUriComponentsContributer(List<UriComponentsContributor> uriComponentsContributors, UriComponentsBuilder builder, MethodInvocation invocation) {

		MethodParameters parameters = new MethodParameters(invocation.getMethod());
		Iterator<Object> parameterValues = Arrays.asList(invocation.getArguments()).iterator();

		for (MethodParameter parameter : parameters.getParameters()) {
			Object parameterValue = parameterValues.next();
			for (UriComponentsContributor contributor : uriComponentsContributors) {
				if (contributor.supportsParameter(parameter)) {
					contributor.enhance(builder, parameter, parameterValue);
				}
			}
		}

		return builder;
	}

	/**
	 * Returns a {@link UriComponentsBuilder} obtained from the current servlet mapping with the host tweaked in case the
	 * request contains an {@code X-Forwarded-Host} header and the scheme tweaked in case the request contains an
	 * {@code X-Forwarded-Ssl} header
	 *
	 * @return
	 */
	protected static UriComponentsBuilder getBuilder() {

		HttpServletRequest request = getCurrentRequest();
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromServletMapping(request);

		String forwardedSsl = request.getHeader("X-Forwarded-Ssl");

		if (StringUtils.hasText(forwardedSsl) && forwardedSsl.equalsIgnoreCase("on")) {
			builder.scheme("https");
		}

		String host = request.getHeader("X-Forwarded-Host");

		if (!StringUtils.hasText(host)) {
			return builder;
		}

		String[] hosts = StringUtils.commaDelimitedListToStringArray(host);
		String hostToUse = hosts[0];

		if (hostToUse.contains(":")) {

			String[] hostAndPort = StringUtils.split(hostToUse, ":");

			builder.host(hostAndPort[0]);
			builder.port(Integer.parseInt(hostAndPort[1]));

		} else {
			builder.host(hostToUse);
			builder.port(-1); // reset port if it was forwarded from default port
		}

		String port = request.getHeader("X-Forwarded-Port");

		if (StringUtils.hasText(port)) {
			builder.port(Integer.parseInt(port));
		}

		return builder;
	}

	/**
	 * Copy of {@link ServletUriComponentsBuilder#getCurrentRequest()} until SPR-10110 gets fixed.
	 *
	 * @return
	 */
	@SuppressWarnings("null")
	private static HttpServletRequest getCurrentRequest() {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
		Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
		HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
		return servletRequest;
	}
}
