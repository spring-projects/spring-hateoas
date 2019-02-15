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

import static org.springframework.hateoas.mvc.ForwardedHeader.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.DefaultUriTemplateHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Builder to ease building {@link Link} instances pointing to Spring MVC controllers.
 *
 * @author Oliver Gierke
 * @author Kamill Sokol
 * @author Greg Turnquist
 * @author Kevin Conaway
 * @author Andrew Naydyonock
 * @author Oliver Trosien
 * @author Greg Turnquist
 */
public class ControllerLinkBuilder extends LinkBuilderSupport<ControllerLinkBuilder> {

	private static final String REQUEST_ATTRIBUTES_MISSING = "Could not find current request via RequestContextHolder. Is this being called from a Spring MVC handler?";
	private static final CachingAnnotationMappingDiscoverer DISCOVERER = new CachingAnnotationMappingDiscoverer(
			new AnnotationMappingDiscoverer(RequestMapping.class));
	private static final ControllerLinkBuilderFactory FACTORY = new ControllerLinkBuilderFactory();
	private static final CustomUriTemplateHandler HANDLER = new CustomUriTemplateHandler();

	private final TemplateVariables variables;

	/**
	 * Creates a new {@link ControllerLinkBuilder} using the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	ControllerLinkBuilder(UriComponentsBuilder builder) {

		super(builder);

		this.variables = TemplateVariables.NONE;
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} using the given {@link UriComponents}.
	 *
	 * @param uriComponents must not be {@literal null}.
	 */
	ControllerLinkBuilder(UriComponents uriComponents) {
		this(uriComponents, TemplateVariables.NONE, null);
	}

	ControllerLinkBuilder(UriComponents uriComponents, TemplateVariables variables, MethodInvocation invocation) {

		super(uriComponents);

		this.variables = variables;
		this.addAffordances(findAffordances(invocation, uriComponents));
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @return
	 */
	public static ControllerLinkBuilder linkTo(Class<?> controller) {
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
	public static ControllerLinkBuilder linkTo(Class<?> controller, Object... parameters) {

		Assert.notNull(controller, "Controller must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		String mapping = DISCOVERER.getMapping(controller);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapping == null ? "/" : mapping);
		UriComponents uriComponents = HANDLER.expandAndEncode(builder, parameters);

		return new ControllerLinkBuilder(getBuilder()).slash(uriComponents, true);
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
	public static ControllerLinkBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {

		Assert.notNull(controller, "Controller must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		String mapping = DISCOVERER.getMapping(controller);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(mapping == null ? "/" : mapping);
		UriComponents uriComponents = HANDLER.expandAndEncode(builder, parameters);

		return new ControllerLinkBuilder(getBuilder()).slash(uriComponents, true);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Method, Object...)
	 */
	public static ControllerLinkBuilder linkTo(Method method, Object... parameters) {
		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Class<?>, Method, Object...)
	 */
	public static ControllerLinkBuilder linkTo(Class<?> controller, Method method, Object... parameters) {

		Assert.notNull(controller, "Controller type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		UriTemplate template = DISCOVERER.getMappingAsUriTemplate(controller, method);
		URI uri = template.expand(parameters);

		return new ControllerLinkBuilder(getBuilder()).slash(uri);
	}

	/**
	 * Creates a {@link ControllerLinkBuilder} pointing to a controller method. Hand in a dummy method invocation result
	 * you can create via {@link #methodOn(Class, Object...)} or {@link DummyInvocationUtils#methodOn(Class, Object...)}.
	 *
	 * <pre>
	 * &#64;RequestMapping("/customers")
	 * class CustomerController {
	 *
	 *   &#64;RequestMapping("/{id}/addresses")
	 *   HttpEntity&lt;Addresses&gt; showAddresses(@PathVariable Long id) { … }
	 * }
	 *
	 * Link link = linkTo(methodOn(CustomerController.class).showAddresses(2L)).withRel("addresses");
	 * </pre>
	 *
	 * The resulting {@link Link} instance will point to {@code /customers/2/addresses} and have a rel of
	 * {@code addresses}. For more details on the method invocation constraints, see
	 * {@link DummyInvocationUtils#methodOn(Class, Object...)}.
	 *
	 * @param invocationValue
	 * @return
	 */
	public static ControllerLinkBuilder linkTo(Object invocationValue) {
		return FACTORY.linkTo(invocationValue);
	}

	/**
	 * Extract a {@link Link} from the {@link ControllerLinkBuilder} and look up the related {@link Affordance}. Should
	 * only be one.
	 *
	 * <pre>
	 * Link findOneLink = linkTo(methodOn(EmployeeController.class).findOne(id)).withSelfRel()
	 * 		.andAffordance(afford(methodOn(EmployeeController.class).updateEmployee(null, id)));
	 * </pre>
	 *
	 * This takes a link and adds an {@link Affordance} based on another Spring MVC handler method.
	 *
	 * @param invocationValue
	 * @return
	 */
	public static Affordance afford(Object invocationValue) {

		ControllerLinkBuilder linkBuilder = linkTo(invocationValue);

		Assert.isTrue(linkBuilder.getAffordances().size() == 1, "A base can only have one affordance, itself");

		return linkBuilder.getAffordances().get(0);
	}

	/**
	 * Wrapper for {@link DummyInvocationUtils#methodOn(Class, Object...)} to be available in case you work with static
	 * imports of {@link ControllerLinkBuilder}.
	 *
	 * @param controller must not be {@literal null}.
	 * @param parameters parameters to extend template variables in the type level mapping.
	 * @return
	 */
	public static <T> T methodOn(Class<T> controller, Object... parameters) {
		return DummyInvocationUtils.methodOn(controller, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#getThis()
	 */
	@Override
	protected ControllerLinkBuilder getThis() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#createNewInstance(org.springframework.web.util.UriComponentsBuilder)
	 */
	@Override
	protected ControllerLinkBuilder createNewInstance(UriComponentsBuilder builder) {
		return new ControllerLinkBuilder(builder);
	}

	/**
	 * Returns a {@link UriComponentsBuilder} to continue to build the already built URI in a more fine grained way.
	 *
	 * @return
	 */
	public UriComponentsBuilder toUriComponentsBuilder() {
		return UriComponentsBuilder.fromUri(toUri());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.LinkBuilderSupport#toString()
	 */
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
	public static UriComponentsBuilder getBuilder() {

		if (RequestContextHolder.getRequestAttributes() == null) {
			return UriComponentsBuilder.fromPath("/");
		}

		HttpServletRequest request = getCurrentRequest();
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromServletMapping(request);

		// Spring 5.1 can handle X-Forwarded-Ssl headers...
		if (isSpringAtLeast5_1()) {
			return builder;
		} else {
			return handleXForwardedSslHeader(request, builder);
		}
	}

	/**
	 * Check if the current version of Spring Framework is 5.1 or higher.
	 *
	 * @return
	 */
	private static boolean isSpringAtLeast5_1() {

		String versionOfSpringFramework = ApplicationContext.class.getPackage().getImplementationVersion();

		String[] parts = versionOfSpringFramework.split("\\.");
		int majorVersion = Integer.parseInt(parts[0]);
		int minorVersion = Integer.parseInt(parts[1]);

		return majorVersion >= 5 && minorVersion >= 1 || majorVersion > 5;
	}

	/**
	 * Copy of {@link ServletUriComponentsBuilder#getCurrentRequest()} until SPR-10110 gets fixed.
	 *
	 * @return
	 */
	@SuppressWarnings("null")
	private static HttpServletRequest getCurrentRequest() {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Assert.state(requestAttributes != null, REQUEST_ATTRIBUTES_MISSING);
		Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
		HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
		return servletRequest;
	}

	/**
	 * Look up {@link Affordance}s based on the {@link MethodInvocation} and {@link UriComponents}.
	 *
	 * @param invocation
	 * @param components
	 * @return
	 */
	private static Collection<Affordance> findAffordances(MethodInvocation invocation, UriComponents components) {
		return SpringMvcAffordanceBuilder.create(invocation, DISCOVERER, components);
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
}
