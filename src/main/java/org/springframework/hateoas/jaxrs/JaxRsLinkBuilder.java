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

import javax.ws.rs.Path;

import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.core.AnnotationMappingDiscoverer;
import org.springframework.hateoas.core.LinkBuilderSupport;
import org.springframework.hateoas.core.MappingDiscoverer;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * {@link LinkBuilder} to derive URI mappings from a JAX-RS {@link Path} annotation.
 * 
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
public class JaxRsLinkBuilder extends LinkBuilderSupport<JaxRsLinkBuilder> {

	private static final MappingDiscoverer DISCOVERER = new AnnotationMappingDiscoverer(Path.class);

	/**
	 * Creates a new {@link JaxRsLinkBuilder} from the given {@link UriComponentsBuilder}.
	 * 
	 * @param builder must not be {@literal null}.
	 */
	private JaxRsLinkBuilder(UriComponentsBuilder builder) {
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

		JaxRsLinkBuilder builder = new JaxRsLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		UriComponents uriComponents = UriComponentsBuilder.fromUriString(DISCOVERER.getMapping(service)).build();
		UriComponents expandedComponents = uriComponents.expand(parameters);
		return builder.slash(expandedComponents);
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
