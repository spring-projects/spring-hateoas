/*
 * Copyright 2016 the original author or authors.
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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.forms.FieldUtils;
import org.springframework.hateoas.forms.Form;
import org.springframework.hateoas.forms.FormBuilderSupport;
import org.springframework.hateoas.forms.Property;
import org.springframework.hateoas.forms.PropertyBuilder;
import org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor.BoundMethodParameter;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Builder to ease building {@link Form} instances pointing to Spring MVC controllers.
 *
 */
public class ControllerFormBuilder extends FormBuilderSupport<ControllerFormBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);

	private static final ControllerFormBuilderFactory FACTORY = new ControllerFormBuilderFactory();

	private List<PropertyBuilder> propertyBuilders = new ArrayList<PropertyBuilder>();

	private RequestMethod[] method;

	private Object requestBody;

	private String contentType;

	public ControllerFormBuilder(UriComponentsBuilder builder) {
		super(builder);
	}

	public ControllerFormBuilder(UriComponentsBuilder builder, RequestMethod[] method, String contentType,
			Object requestBody, List<PropertyBuilder> propertyBuilders) {
		super(builder);
		this.method = method;
		this.contentType = contentType;
		this.requestBody = requestBody;
		this.propertyBuilders = propertyBuilders;
	}

	public PropertyBuilder property(String fieldName) {
		Assert.notNull(fieldName, "fieldName cannot be null");
		Class<?> fieldType = null;
		if (requestBody != null) {
			fieldType = FieldUtils.findFieldClass(requestBody.getClass(), fieldName);
		}

		PropertyBuilder propertyBuilder = new PropertyBuilder(fieldName, fieldType, this);
		propertyBuilders.add(propertyBuilder);
		return propertyBuilder;
	}

	/*
	 * @see org.springframework.hateoas.FormBuilderFactory#formTo(Method, Object...)
	 */
	public static ControllerFormBuilder formTo(Method method, Object... parameters) {
		return formTo(method.getDeclaringClass(), method, parameters);
	}

	/*
	 * @see org.springframework.hateoas.FormBuilderFactory#formTo(Class<?>, Method, Object...)
	 */
	public static ControllerFormBuilder formTo(Class<?> controller, Method method, Object... parameters) {

		Assert.notNull(controller, "Controller type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		UriTemplate template = new UriTemplate(DISCOVERER.getMapping(controller, method));
		URI uri = template.expand(parameters);

		// TODO: extract requestBody from parameters with info from method.getParameterAnnotations()
		ControllerFormBuilder formBuilder = new ControllerFormBuilder(getBuilder()).slash(uri);
		formBuilder.method(getMethod(method));
		return formBuilder;
	}

	public static ControllerFormBuilder formTo(Object invocationValue) {
		ControllerFormBuilder formBuilder = FACTORY.formTo(invocationValue);

		LastInvocationAware invocations = (LastInvocationAware) invocationValue;
		MethodInvocation invocation = invocations.getLastInvocation();

		Object requestBody = null;
		for (BoundMethodParameter parameter : ControllerFormBuilderFactory.REQUEST_BODY_ACCESSOR
				.getBoundParameters(invocation)) {
			requestBody = parameter.getValue();
		}
		formBuilder.body(requestBody);
		formBuilder.method(getMethod(invocation.getMethod()));
		formBuilder.contentType(getContentType(invocation.getMethod()));
		return formBuilder;
	}

	public static <T> T methodOn(Class<T> controller, Object... parameters) {
		return DummyInvocationUtils.methodOn(controller, parameters);
	}

	private static RequestMethod[] getMethod(Method method) {
		RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		if (requestMapping != null && requestMapping.method().length > 0) {
			return requestMapping.method();
		}
		return new RequestMethod[] { RequestMethod.GET };
	}

	private static String getContentType(Method method) {
		RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		if (requestMapping != null && requestMapping.consumes().length > 0) {
			for (String consume : requestMapping.consumes()) {
				if (MediaType.APPLICATION_JSON_VALUE.equals(consume)
						|| MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(consume)) {
					return consume;
				}
			}
		}

		return null;
	}

	@Override
	protected ControllerFormBuilder getThis() {
		return this;
	}

	@Override
	protected ControllerFormBuilder createNewInstance(UriComponentsBuilder builder) {
		return new ControllerFormBuilder(builder, method, contentType, requestBody, propertyBuilders);
	}

	static UriComponentsBuilder getBuilder() {

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

		}
		else {
			builder.host(hostToUse);
			builder.port(-1); // reset port if it was forwarded from default port
		}

		String port = request.getHeader("X-Forwarded-Port");

		if (StringUtils.hasText(port)) {
			builder.port(Integer.parseInt(port));
		}

		return builder;
	}

	private static HttpServletRequest getCurrentRequest() {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
		Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
		HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
		return servletRequest;
	}

	@Override
	public List<Property> getProperties() {
		List<Property> properties = new ArrayList<Property>();
		for (PropertyBuilder builder : propertyBuilders) {
			properties.add(builder.build());
		}
		return properties;
	}

	@Override
	public Object getBody() {
		return requestBody;
	}

	public ControllerFormBuilder body(Object requestBody) {
		this.requestBody = requestBody;
		return this;
	}

	@Override
	public RequestMethod[] getMethod() {
		return method;
	}

	public ControllerFormBuilder method(RequestMethod[] method) {
		this.method = method;
		return this;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	public ControllerFormBuilder contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}
}
