/*
 * Copyright 2012 the original author or authors.
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

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.mvc.LinkComponents;
import org.springframework.hateoas.mvc.UriComponentsLinkBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import javax.ws.rs.Path;

/**
 * {@link LinkBuilder} to derive URI mappings from a JAX-RS {@link Path} annotation.
 *
 * @author Oliver Gierke
 */
public class JaxRsLinkBuilder extends UriComponentsLinkBuilder<JaxRsLinkBuilder> {

	/**
	 * Creates a new {@link JaxRsLinkBuilder} from the given {@link LinkComponents}.
	 *
	 * @param linkComponents must not be {@literal null}.
	 */
	private JaxRsLinkBuilder(LinkComponents linkComponents) {
		super(linkComponents);
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

		Path annotation = AnnotationUtils.findAnnotation(service, Path.class);
		String path = (String) AnnotationUtils.getValue(annotation);

		JaxRsLinkBuilder builder = new JaxRsLinkBuilder(new LinkComponents(ServletUriComponentsBuilder.fromCurrentServletMapping().build(), null));

		UriTemplate template = new UriTemplate(path);
		return builder.slash(template.expand(parameters));
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
	protected JaxRsLinkBuilder createNewInstance(LinkComponents linkComponents) {
		return new JaxRsLinkBuilder(linkComponents);
	}
}
