/*
 * Copyright 2018 the original author or authors.
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

import static org.springframework.hateoas.TemplateVariable.VariableType.*;
import static org.springframework.hateoas.TemplateVariables.*;
import static org.springframework.hateoas.core.DummyInvocationUtils.*;
import static org.springframework.hateoas.core.EncodingUtils.*;
import static org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor.*;
import static org.springframework.util.StringUtils.*;
import static org.springframework.web.util.UriComponents.UriTemplateVariables.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.AffordanceModelFactory;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * @author Greg Turnquist
 */
public class DynamicLinkBuilder extends LinkBuilderSupport<DynamicLinkBuilder> {

	private static final String REQUEST_ATTRIBUTES_MISSING = "Could not find current request via RequestContextHolder. Is this being called from a Spring MVC handler?";

	private final SpringMvcAffordanceBuilder affordanceBuilder;
	private final CachingAnnotationMappingDiscoverer discoverer;
	private final CustomUriTemplateHandler handler;
	private final AnnotatedParametersParameterAccessor pathVariableAccessor;
	private final AnnotatedParametersParameterAccessor requestParamParameterAccessor = new RequestParamParameterAccessor();

	private final TemplateVariables variables;

	private List<UriComponentsContributor> uriComponentsContributors = new ArrayList<>();

	/**
	 * Creates a new {@link ControllerLinkBuilder} using the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	DynamicLinkBuilder(UriComponentsBuilder builder) {

		super(builder);

		this.variables = TemplateVariables.NONE;

		List<AffordanceModelFactory> factories = SpringFactoriesLoader.loadFactories(AffordanceModelFactory.class,
			ControllerLinkBuilder.class.getClassLoader());
		PluginRegistry<? extends AffordanceModelFactory, MediaType> modelFactories = OrderAwarePluginRegistry
			.create(factories);
		this.affordanceBuilder = new SpringMvcAffordanceBuilder(modelFactories);
		this.discoverer = new CachingAnnotationMappingDiscoverer(new AnnotationMappingDiscoverer(RequestMapping.class));
		this.handler = new CustomUriTemplateHandler();
		this.pathVariableAccessor = new AnnotatedParametersParameterAccessor(new AnnotationAttribute(PathVariable.class));
	}

	DynamicLinkBuilder(UriComponents uriComponents, TemplateVariables variables, MethodInvocation invocation) {

		super(uriComponents);

		this.variables = variables;

		List<AffordanceModelFactory> factories = SpringFactoriesLoader.loadFactories(AffordanceModelFactory.class,
			ControllerLinkBuilder.class.getClassLoader());
		PluginRegistry<? extends AffordanceModelFactory, MediaType> modelFactories = OrderAwarePluginRegistry
			.create(factories);
		this.affordanceBuilder = new SpringMvcAffordanceBuilder(modelFactories);
		this.discoverer = new CachingAnnotationMappingDiscoverer(new AnnotationMappingDiscoverer(RequestMapping.class));
		this.handler = new CustomUriTemplateHandler();
		this.pathVariableAccessor = new AnnotatedParametersParameterAccessor(new AnnotationAttribute(PathVariable.class));

		this.addAffordances(findAffordances(invocation, uriComponents));
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @return
	 */
	public DynamicLinkBuilder linkTo(Class<?> controller) {
		return linkTo(controller, new Object[0]);
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class. The
	 * additional parameters are used to fill up potentially available path variables in the class scop request mapping.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public DynamicLinkBuilder linkTo(Class<?> controller, Object... parameters) {

		Assert.notNull(controller, "Controller must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		String mapping = this.discoverer.getMapping(controller);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapping == null ? "/" : mapping);
		UriComponents uriComponents = this.handler.expandAndEncode(builder, parameters);

		return new DynamicLinkBuilder(getBuilder()).slash(uriComponents, true);
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class.
	 * Parameter map is used to fill up potentially available path variables in the class scope request mapping.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public DynamicLinkBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {

		Assert.notNull(controller, "Controller must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		String mapping = this.discoverer.getMapping(controller);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapping == null ? "/" : mapping);
		UriComponents uriComponents = this.handler.expandAndEncode(builder, parameters);

		return new DynamicLinkBuilder(getBuilder()).slash(uriComponents, true);
	}

	public DynamicLinkBuilder linkTo(Method method, Object... parameters) {
		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	public DynamicLinkBuilder linkTo(Class<?> controller, Method method, Object... parameters) {

		Assert.notNull(controller, "Controller type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		UriTemplate template = this.discoverer.getMappingAsUriTemplate(controller, method);
		URI uri = template.expand(parameters);

		return new DynamicLinkBuilder(getBuilder()).slash(uri);
	}

	public DynamicLinkBuilder linkTo(Object invocationValue) {

		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationAware invocations = (LastInvocationAware) invocationValue;

		MethodInvocation invocation = invocations.getLastInvocation();
		Iterator<Object> classMappingParameters = invocations.getObjectParameters();
		Method method = invocation.getMethod();

		String mapping = this.discoverer.getMapping(invocation.getTargetType(), method);

		UriComponentsBuilder builder = ControllerLinkBuilder.getBuilder().path(mapping);

		UriTemplate template = new UriTemplate(mapping);
		Map<String, Object> values = new HashMap<>();
		Iterator<String> names = template.getVariableNames().iterator();

		while (classMappingParameters.hasNext()) {
			values.put(names.next(), encodePath(classMappingParameters.next()));
		}

		for (BoundMethodParameter parameter : this.pathVariableAccessor.getBoundParameters(invocation)) {
			values.put(parameter.getVariableName(), encodePath(parameter.asString()));
		}

		List<String> optionalEmptyParameters = new ArrayList<>();

		for (BoundMethodParameter parameter : this.requestParamParameterAccessor.getBoundParameters(invocation)) {

			bindRequestParameters(builder, parameter);

			if (SKIP_VALUE.equals(parameter.getValue())) {

				values.put(parameter.getVariableName(), SKIP_VALUE);

				if (!parameter.isRequired()) {
					optionalEmptyParameters.add(parameter.getVariableName());
				}
			}
		}

		for (String variable : template.getVariableNames()) {
			if (!values.containsKey(variable)) {
				values.put(variable, SKIP_VALUE);
			}
		}

		UriComponents components = applyUriComponentsContributer(builder, invocation).buildAndExpand(values);
		TemplateVariables variables = NONE;

		for (String parameter : optionalEmptyParameters) {

			boolean previousRequestParameter = components.getQueryParams().isEmpty() && variables.equals(NONE);
			TemplateVariable variable = new TemplateVariable(parameter,
				previousRequestParameter ? REQUEST_PARAM : REQUEST_PARAM_CONTINUED);
			variables = variables.concat(variable);
		}

		return new DynamicLinkBuilder(components, variables, invocation);
	}

	public Affordance afford(Object invocationValue) {

		DynamicLinkBuilder linkBuilder = linkTo(invocationValue);

		Assert.isTrue(linkBuilder.getAffordances().size() == 1, "A base can only have one affordance, itself");

		return linkBuilder.getAffordances().get(0);
	}

	public <T> T methodOn(Class<T> controller, Object... parameters) {
		return DummyInvocationUtils.methodOn(controller, parameters);
	}

	@Override
	protected DynamicLinkBuilder getThis() {
		return this;
	}

	@Override
	protected DynamicLinkBuilder createNewInstance(UriComponentsBuilder builder) {
		return new DynamicLinkBuilder(builder);
	}

	@Override
	public String toString() {

		String result = super.toString();

		if (variables == TemplateVariables.NONE) {
			return result;
		}

		if (!result.contains("#")) {
			return result.concat(variables.toString());
		}

		String[] parts = result.split("#");
		return parts[0].concat(variables.toString()).concat("#").concat(parts[0]);
	}

	/**
	 * Returns a {@link UriComponentsBuilder} obtained from the current servlet mapping with scheme tweaked in case the
	 * request contains an {@code X-Forwarded-Ssl} header, which is not (yet) supported by the underlying
	 * {@link UriComponentsBuilder}. If no {@link RequestContextHolder} exists (you're outside a Spring Web call), fall
	 * back to relative URIs.
	 *
	 * @return
	 */
	UriComponentsBuilder getBuilder() {

		if (RequestContextHolder.getRequestAttributes() == null) {
			return UriComponentsBuilder.fromPath("/");
		}

		HttpServletRequest request = getCurrentRequest();
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromServletMapping(request);

		// special case handling for X-Forwarded-Ssl:
		// apply it, but only if X-Forwarded-Proto is unset.

		String forwardedSsl = request.getHeader("X-Forwarded-Ssl");
		ForwardedHeader forwarded = ForwardedHeader.of(request.getHeader(ForwardedHeader.NAME));
		String proto = hasText(forwarded.getProto()) ? forwarded.getProto() : request.getHeader("X-Forwarded-Proto");

		if (!hasText(proto) && hasText(forwardedSsl) && forwardedSsl.equalsIgnoreCase("on")) {
			builder.scheme("https");
		}

		return builder;
	}

	/**
	 * Copy of {@link ServletUriComponentsBuilder#getCurrentRequest()} until SPR-10110 gets fixed.
	 *
	 * @return
	 */
	private static HttpServletRequest getCurrentRequest() {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Assert.state(requestAttributes != null, REQUEST_ATTRIBUTES_MISSING);
		Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
		HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
		return servletRequest;
	}


	private Collection<Affordance> findAffordances(MethodInvocation invocation, UriComponents components) {
		return this.affordanceBuilder.create(invocation, discoverer, components);
	}

	@RequiredArgsConstructor
	private static class CachingAnnotationMappingDiscoverer implements MappingDiscoverer {

		private final @Delegate AnnotationMappingDiscoverer delegate;
		private final Map<String, UriTemplate> templates = new ConcurrentReferenceHashMap<>();

		public UriTemplate getMappingAsUriTemplate(Class<?> type, Method method) {

			String mapping = delegate.getMapping(type, method);

			return templates.computeIfAbsent(mapping, UriTemplate::new);
		}
	}

	private static class CustomUriTemplateHandler extends DefaultUriTemplateHandler {

		public CustomUriTemplateHandler() {
			setStrictEncoding(true);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.web.util.DefaultUriTemplateHandler#expandAndEncode(org.springframework.web.util.UriComponentsBuilder, java.util.Map)
		 */
		@Override
		public UriComponents expandAndEncode(UriComponentsBuilder builder, Map<String, ?> uriVariables) {
			return super.expandAndEncode(builder, uriVariables);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.web.util.DefaultUriTemplateHandler#expandAndEncode(org.springframework.web.util.UriComponentsBuilder, java.lang.Object[])
		 */
		@Override
		public UriComponents expandAndEncode(UriComponentsBuilder builder, Object[] uriVariables) {
			return super.expandAndEncode(builder, uriVariables);
		}
	}

	private static void bindRequestParameters(UriComponentsBuilder builder, BoundMethodParameter parameter) {

		Object value = parameter.getValue();
		String key = parameter.getVariableName();

		if (value instanceof MultiValueMap) {

			MultiValueMap<String, String> requestParams = (MultiValueMap<String, String>) value;

			for (Map.Entry<String, List<String>> multiValueEntry : requestParams.entrySet()) {
				for (String singleEntryValue : multiValueEntry.getValue()) {
					builder.queryParam(multiValueEntry.getKey(), encodeParameter(singleEntryValue));
				}
			}

		} else if (value instanceof Map) {

			Map<String, String> requestParams = (Map<String, String>) value;

			for (Map.Entry<String, String> requestParamEntry : requestParams.entrySet()) {
				builder.queryParam(requestParamEntry.getKey(), encodeParameter(requestParamEntry.getValue()));
			}

		} else if (value instanceof Collection) {

			for (Object element : (Collection<?>) value) {
				builder.queryParam(key, encodeParameter(element));
			}

		} else if (SKIP_VALUE.equals(value)) {

			if (parameter.isRequired()) {
				builder.queryParam(key, String.format("{%s}", parameter.getVariableName()));
			}

		} else {
			builder.queryParam(key, encodeParameter(parameter.asString()));
		}
	}

	/**
	 * Applies the configured {@link UriComponentsContributor}s to the given {@link UriComponentsBuilder}.
	 *
	 * @param builder will never be {@literal null}.
	 * @param invocation will never be {@literal null}.
	 * @return
	 */
	protected UriComponentsBuilder applyUriComponentsContributer(UriComponentsBuilder builder,
																 MethodInvocation invocation) {

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
	}


	/**
	 * Custom extension of {@link AnnotatedParametersParameterAccessor} for {@link RequestParam} to allow {@literal null}
	 * values handed in for optional request parameters.
	 *
	 * @author Oliver Gierke
	 */
	private static class RequestParamParameterAccessor extends AnnotatedParametersParameterAccessor {

		public RequestParamParameterAccessor() {
			super(new AnnotationAttribute(RequestParam.class));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor#createParameter(org.springframework.core.MethodParameter, java.lang.Object, org.springframework.hateoas.core.AnnotationAttribute)
		 */
		@Override
		protected BoundMethodParameter createParameter(final MethodParameter parameter, Object value,
													   AnnotationAttribute attribute) {

			return new BoundMethodParameter(parameter, value, attribute) {

				/*
				 * (non-Javadoc)
				 * @see org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor.BoundMethodParameter#isRequired()
				 */
				@Override
				public boolean isRequired() {

					RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);

					if (parameter.isOptional()) {
						return false;
					}

					return annotation.required() //
						&& annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE);
				}
			};
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mvc.AnnotatedParametersParameterAccessor#verifyParameterValue(org.springframework.core.MethodParameter, java.lang.Object)
		 */
		@Override
		protected Object verifyParameterValue(MethodParameter parameter, Object value) {

			RequestParam annotation = parameter.getParameterAnnotation(RequestParam.class);

			value = ObjectUtils.unwrapOptional(value);

			if (value != null) {
				return value;
			}

			if (!annotation.required() || parameter.isOptional()) {
				return SKIP_VALUE;
			}

			return annotation.defaultValue().equals(ValueConstants.DEFAULT_NONE) ? SKIP_VALUE : null;
		}
	}

}
