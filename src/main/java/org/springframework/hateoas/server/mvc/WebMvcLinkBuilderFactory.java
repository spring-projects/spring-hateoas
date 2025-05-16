/*
 * Copyright 2012-2024 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import jakarta.servlet.ServletContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.server.MethodLinkBuilderFactory;
import org.springframework.hateoas.server.core.AdditionalUriHandler;
import org.springframework.hateoas.server.core.LinkBuilderSupport;
import org.springframework.hateoas.server.core.MethodInvocation;
import org.springframework.hateoas.server.core.MethodParameters;
import org.springframework.hateoas.server.core.SpringAffordanceBuilder;
import org.springframework.hateoas.server.core.UriMapping;
import org.springframework.hateoas.server.core.WebHandler;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.mvc.condition.NameValueExpression;
import org.springframework.web.servlet.mvc.condition.ParamsRequestCondition;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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
 * @author Réda Housni Alaoui
 */
public class WebMvcLinkBuilderFactory implements MethodLinkBuilderFactory<WebMvcLinkBuilder> {

	private static ConversionService FALLBACK_CONVERSION_SERVICE = new DefaultFormattingConversionService();

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
	public WebMvcLinkBuilder linkTo(Class<?> controller) {
		return WebMvcLinkBuilder.linkTo(controller);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public WebMvcLinkBuilder linkTo(Class<?> controller, Object... parameters) {
		return WebMvcLinkBuilder.linkTo(controller, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.util.Map)
	 */
	@Override
	public WebMvcLinkBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {
		return WebMvcLinkBuilder.linkTo(controller, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.MethodLinkBuilderFactory#linkTo(java.lang.reflect.Method)
	 */
	@Override
	public WebMvcLinkBuilder linkTo(Method method) {
		return WebMvcLinkBuilder.linkTo(method);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public WebMvcLinkBuilder linkTo(Method method, Object... parameters) {
		return WebMvcLinkBuilder.linkTo(method, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.MethodLinkBuilderFactory#linkTo(java.lang.Class, java.lang.reflect.Method)
	 */
	@Override
	public WebMvcLinkBuilder linkTo(Class<?> type, Method method) {
		return WebMvcLinkBuilder.linkTo(type, method);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.Class, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public WebMvcLinkBuilder linkTo(Class<?> controller, Method method, Object... parameters) {
		return WebMvcLinkBuilder.linkTo(controller, method, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(java.lang.Object)
	 */
	@Override
	public WebMvcLinkBuilder linkTo(Object invocationValue) {

		Function<UriMapping, UriComponentsBuilder> builderFactory = mapping -> UriComponentsBuilderFactory
				.forMapping(mapping);

		return WebHandler.linkTo(invocationValue, WebMvcLinkBuilder::new,
				new UriComponentsContributorsAdditionalUriHandler(uriComponentsContributors), builderFactory, getConversionService());
	}

	private static class UriComponentsContributorsAdditionalUriHandler implements AdditionalUriHandler {

		private final List<UriComponentsContributor> uriComponentsContributors;

		private UriComponentsContributorsAdditionalUriHandler(List<UriComponentsContributor> uriComponentsContributors) {
			this.uriComponentsContributors = uriComponentsContributors;
		}

		@Override
		public UriComponentsBuilder apply(UriComponentsBuilder builder, MethodInvocation invocation) {
			String[] primaryParams = SpringAffordanceBuilder.DISCOVERER.getParams(invocation.getMethod());

			if (primaryParams.length > 0) {

				ParamsRequestCondition paramsRequestCondition = new ParamsRequestCondition(primaryParams);

				for (NameValueExpression<String> expression : paramsRequestCondition.getExpressions()) {

					if (expression.isNegated()) {
						continue;
					}

					String value = expression.getValue();

					if (value == null) {
						continue;
					}

					builder.queryParam(expression.getName(), value);
				}
			}

			MethodParameters parameters = MethodParameters.of(invocation.getMethod());
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

		@Override
		public TemplateVariables apply(TemplateVariables templateVariables, UriComponents uriComponents, MethodInvocation invocation) {
			MethodParameters parameters = MethodParameters.of(invocation.getMethod());

			for (MethodParameter parameter : parameters.getParameters()) {

				for (UriComponentsContributor contributor : uriComponentsContributors) {
					if (contributor.supportsParameter(parameter)) {
						templateVariables = contributor.enhance(templateVariables, uriComponents, parameter);
					}
				}
			}

			return templateVariables;
		}
	}

	private static Supplier<ConversionService> getConversionService() {

		return () -> {

			RequestAttributes attributes = RequestContextHolder.getRequestAttributes();

			if (attributes == null || !ServletRequestAttributes.class.isInstance(attributes)) {
				return FALLBACK_CONVERSION_SERVICE;
			}

			ServletContext servletContext = ((ServletRequestAttributes) attributes).getRequest().getServletContext();
			WebApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

			return context == null || !context.containsBean("mvcConversionService")
					? FALLBACK_CONVERSION_SERVICE
					: context.getBean("mvcConversionService", ConversionService.class);
		};
	}
}
