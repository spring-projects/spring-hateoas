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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.server.core.DummyInvocationUtils;
import org.springframework.hateoas.server.core.SpringAffordanceBuilder;
import org.springframework.hateoas.server.core.TemplateVariableAwareLinkBuilderSupport;
import org.springframework.util.Assert;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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
 * @author Lars Michele
 */
public class WebMvcLinkBuilder extends TemplateVariableAwareLinkBuilderSupport<WebMvcLinkBuilder> {

	private static final WebMvcLinkBuilderFactory FACTORY = new WebMvcLinkBuilderFactory();
	private static final DefaultUriBuilderFactory URI_FACTORY = new DefaultUriBuilderFactory();

	/**
	 * Creates a new {@link WebMvcLinkBuilder} using the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	WebMvcLinkBuilder(UriComponents components) {
		this(components, TemplateVariables.NONE, Collections.emptyList());
	}

	WebMvcLinkBuilder(UriComponents uriComponents, TemplateVariables variables, List<Affordance> affordances) {
		super(uriComponents, variables, affordances);
	}

	/**
	 * Creates a new {@link WebMvcLinkBuilder} with a base of the mapping annotated to the given controller class.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @return
	 */
	public static WebMvcLinkBuilder linkTo(Class<?> controller) {
		return linkTo(controller, new Object[0]);
	}

	/**
	 * Creates a new {@link WebMvcLinkBuilder} with a base of the mapping annotated to the given controller class. The
	 * additional parameters are used to fill up potentially available path variables in the class scop request mapping.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public static WebMvcLinkBuilder linkTo(Class<?> controller, Object... parameters) {

		Assert.notNull(controller, "Controller must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		var mapping = SpringAffordanceBuilder.DISCOVERER.getUriMapping(controller);
		var defaulted = mapping == null ? "/" : mapping.getMapping();

		var uri = URI_FACTORY.expand(defaulted, parameters);
		var uriComponents = UriComponentsBuilder.fromUri(uri).build();

		return new WebMvcLinkBuilder(UriComponentsBuilderFactory.getComponents()).slash(uriComponents, true);
	}

	/**
	 * Creates a new {@link WebMvcLinkBuilder} with a base of the mapping annotated to the given controller class.
	 * Parameter map is used to fill up potentially available path variables in the class scope request mapping.
	 *
	 * @param controller the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public static WebMvcLinkBuilder linkTo(Class<?> controller, Map<String, ?> parameters) {

		Assert.notNull(controller, "Controller must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		var mapping = SpringAffordanceBuilder.DISCOVERER.getUriMapping(controller);
		var defaulted = mapping == null ? "/" : mapping.getMapping();

		var uri = URI_FACTORY.expand(defaulted, parameters);
		var uriComponents = UriComponentsBuilder.fromUri(uri).build();

		return new WebMvcLinkBuilder(UriComponentsBuilderFactory.getComponents()).slash(uriComponents, true);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Method)
	 */
	public static WebMvcLinkBuilder linkTo(Method method) {

		Assert.notNull(method, "Method must not be null!");

		return linkTo(method.getDeclaringClass(), method, new Object[method.getParameterTypes().length]);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Method, Object...)
	 */
	public static WebMvcLinkBuilder linkTo(Method method, Object... parameters) {

		Assert.notNull(method, "Method must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Class<?>, Method)
	 */
	public static WebMvcLinkBuilder linkTo(Class<?> controller, Method method) {

		Assert.notNull(controller, "Controller type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		return linkTo(controller, method, new Object[method.getParameterTypes().length]);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Class<?>, Method, Object...)
	 */
	public static WebMvcLinkBuilder linkTo(Class<?> controller, Method method, Object... parameters) {

		Assert.notNull(controller, "Controller type must not be null!");
		Assert.notNull(method, "Method must not be null!");
		Assert.notNull(parameters, "Parameters must not be null!");

		int expected = method.getParameterTypes().length;
		int given = parameters.length;

		Assert.isTrue(expected == given,
				() -> String.format("Incorrect number of parameter values given. Expected %s, got %s!", expected, given));

		return linkTo(DummyInvocationUtils.getLastInvocationAware(controller, method, parameters));
	}

	/**
	 * Creates a {@link WebMvcLinkBuilder} pointing to a controller method. Hand in a dummy method invocation result you
	 * can create via {@link #methodOn(Class, Object...)} or {@link DummyInvocationUtils#methodOn(Class, Object...)}.
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
	public static WebMvcLinkBuilder linkTo(Object invocationValue) {
		return FACTORY.linkTo(invocationValue);
	}

	/**
	 * Extract a {@link Link} from the {@link WebMvcLinkBuilder} and look up the related {@link Affordance}. Should only
	 * be one.
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

		WebMvcLinkBuilder linkBuilder = linkTo(invocationValue);

		Assert.isTrue(linkBuilder.getAffordances().size() == 1, "A base can only have one affordance, itself");

		return linkBuilder.getAffordances().get(0);
	}

	/**
	 * Wrapper for {@link DummyInvocationUtils#methodOn(Class, Object...)} to be available in case you work with static
	 * imports of {@link WebMvcLinkBuilder}.
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
	protected WebMvcLinkBuilder getThis() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.TemplateVariableAwareLinkBuilderSupport#createNewInstance(org.springframework.web.util.UriComponents, java.util.List, org.springframework.hateoas.TemplateVariables)
	 */
	@Override
	protected WebMvcLinkBuilder createNewInstance(UriComponents components, List<Affordance> affordances,
			TemplateVariables variables) {
		return new WebMvcLinkBuilder(components, variables, affordances);
	}

	/**
	 * Returns a {@link UriComponentsBuilder} to continue to build the already built URI in a more fine grained way.
	 *
	 * @return
	 */
	public UriComponentsBuilder toUriComponentsBuilder() {
		return UriComponentsBuilder.fromUri(toUri());
	}
}
