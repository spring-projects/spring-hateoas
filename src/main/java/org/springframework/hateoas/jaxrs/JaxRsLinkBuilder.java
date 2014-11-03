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
package org.springframework.hateoas.jaxrs;

import java.lang.reflect.Method;
import java.net.URI;

import javax.ws.rs.Path;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * {@link LinkBuilder} to derive URI mappings from a JAX-RS {@link Path} annotation.
 *
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
public class JaxRsLinkBuilder extends LinkBuilderSupport<JaxRsLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(Path.class);
	private static final JaxRsLinkBuilderFactory FACTORY = new JaxRsLinkBuilderFactory();

	/**
	 * Creates a new {@link JaxRsLinkBuilder} from the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	JaxRsLinkBuilder(UriComponentsBuilder builder) {
		super(builder);
	}

	/**
	 * Creates a {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class.
	 *
	 * @param service the class to discover the annotation on, must not be {@literal null}.
	 * @return
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> service) {
		return linkTo(service, new Object[0]);
	}

	/**
	 * Creates a new {@link JaxRsLinkBuilder} instance to link to the {@link Path} mapping tied to the given class binding
	 * the given parameters to the URI template.
	 *
	 * @param service the class to discover the annotation on, must not be {@literal null}.
	 * @param parameters additional parameters to bind to the URI template declared in the annotation, must not be
	 *          {@literal null}.
	 * @return
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> service, Object... parameters) {

		JaxRsLinkBuilder builder = new JaxRsLinkBuilder(getBuilder());
		String mapping = DISCOVERER.getMapping(service);

		UriComponents uriComponents = UriComponentsBuilder.fromUriString(mapping == null ? "/" : mapping).build();
		UriComponents expandedComponents = uriComponents.expand(parameters);
		return builder.slash(expandedComponents);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Method, Object...)
	 */
	public static JaxRsLinkBuilder linkTo(Method method, Object... parameters) {
		return linkTo(method.getDeclaringClass(), method, parameters);
	}

	/*
	 * @see org.springframework.hateoas.MethodLinkBuilderFactory#linkTo(Class<?>, Method, Object...)
	 */
	public static JaxRsLinkBuilder linkTo(Class<?> controller, Method method, Object... parameters) {

		Assert.notNull(controller, "Controller type must not be null!");
		Assert.notNull(method, "Method must not be null!");

		UriTemplate template = new UriTemplate(DISCOVERER.getMapping(controller, method));
		URI uri = template.expand(parameters);

		return new JaxRsLinkBuilder(getBuilder()).slash(uri);
	}

	/**
	 * Creates a {@link JaxRsLinkBuilder} pointing to a controller method. Hand in a dummy method invocation result
	 * you can create via {@link #methodOn(Class, Object...)} or {@link DummyInvocationUtils#methodOn(Class, Object...)}.
	 *
	 * <pre>
	 * @Path("/customers")
	 * class CustomerController {
	 *
	 *   @Path("/{id}/addresses")
	 *   HttpEntity&lt;Addresses&gt; showAddresses(@PathParam( "id" ) Long id) { … }
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
	public static JaxRsLinkBuilder linkTo(Object invocationValue) {
		return FACTORY.linkTo(invocationValue);
	}

	/**
	 * Wrapper for {@link DummyInvocationUtils#methodOn(Class, Object...)} to be available in case you work with static
	 * imports of {@link JaxRsLinkBuilder}.
	 *
	 * @param controller must not be {@literal null}.
	 * @param parameters parameters to extend template variables in the type level mapping.
	 * @return
	 */
	public static <T> T methodOn(Class<T> controller, Object... parameters) {
		return DummyInvocationUtils.methodOn(controller, parameters);
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
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#getThis()
	 */
	@Override
	protected JaxRsLinkBuilder getThis() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriComponentsLinkBuilder#createNewInstance(org.springframework.web.util.UriComponentsBuilder)
	 */
	@Override
	protected JaxRsLinkBuilder createNewInstance(UriComponentsBuilder builder) {
		return new JaxRsLinkBuilder(builder);
	}
}
