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
package org.springframework.hateoas.mvc;

import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Builder to ease building {@link Link} instances pointing to Spring MVC controllers.
 *
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
public class ControllerLinkBuilder extends LinkBuilderSupport<ControllerLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(RequestMapping.class);
	private static final ControllerLinkBuilderFactory FACTORY = new ControllerLinkBuilderFactory();

	/**
	 * Creates a new {@link ControllerLinkBuilder} using the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	ControllerLinkBuilder(UriComponentsBuilder builder) {
		super(builder);
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

		Assert.notNull(controller);

		ControllerLinkBuilder builder = new ControllerLinkBuilder(getBuilder());
		String mapping = DISCOVERER.getMapping(controller);

		UriComponents uriComponents = UriComponentsBuilder.fromUriString(mapping == null ? "/" : mapping).build();
		UriComponents expandedComponents = uriComponents.expand(parameters);

		return builder.slash(expandedComponents);
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

		UriTemplate template = new UriTemplate(DISCOVERER.getMapping(controller, method));
		URI uri = template.expand(parameters);

		return new ControllerLinkBuilder(getBuilder()).slash(uri);
	}

	/**
	 * Creates a {@link ControllerLinkBuilder} pointing to a controller method. Hand in a dummy method invocation result
	 * you can create via {@link #methodOn(Class, Object...)} or {@link DummyInvocationUtils#methodOn(Class, Object...)}.
	 *
	 * <pre>
	 * @RequestMapping("/customers")
	 * class CustomerController {
	 *
	 *   @RequestMapping("/{id}/addresses")
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
}
