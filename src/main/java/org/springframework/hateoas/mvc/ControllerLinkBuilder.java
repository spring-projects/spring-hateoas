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
package org.springframework.hateoas.mvc;

import java.net.URI;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

/**
 * Builder to ease building {@link Link} instances pointing to Spring MVC controllers.
 * 
 * @author Oliver Gierke
 */
public class ControllerLinkBuilder {

	private final UriComponents builder;

	/**
	 * Creates a new {@link ControllerLinkBuilder}.
	 * 
	 * @param builder must not be {@literal null}.
	 */
	private ControllerLinkBuilder(UriComponentsBuilder builder) {
		Assert.notNull(builder);
		this.builder = builder.build();
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class.
	 * 
	 * @param controller
	 * @return
	 */
	public static ControllerLinkBuilder linkTo(Class<?> controller) {
		return linkTo(controller, new Object[0]);
	}

	public static ControllerLinkBuilder linkTo(Class<?> controller, Object... parameters) {

		RequestMapping annotation = controller.getAnnotation(RequestMapping.class);
		String[] mapping = annotation == null ? new String[0] : (String[]) AnnotationUtils.getValue(annotation);

		if (mapping.length > 1) {
			throw new IllegalStateException("Multiple controller mappings defined! Unable to build URI!");
		}

		ControllerLinkBuilder builder = new ControllerLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping());

		if (mapping.length == 0) {
			return builder;
		}

		UriTemplate template = new UriTemplate(mapping[0]);
		return builder.slash(template.expand(parameters));
	}

	/**
	 * Adds the given object's {@link String} representation as sub-resource to the current URI.
	 * 
	 * @param object
	 * @return
	 */
	public ControllerLinkBuilder slash(Object object) {

		if (object == null) {
			return this;
		}

		String[] segments = StringUtils.tokenizeToStringArray(object.toString(), "/");
		return new ControllerLinkBuilder(UriComponentsBuilder.fromUri(builder.toUri()).pathSegment(segments));
	}

	/**
	 * Adds the given {@link AbstractEntity}'s id as sub-resource. Will simply return the current builder if the given
	 * entity is {@literal null}.
	 * 
	 * @param identifyable
	 * @return
	 */
	public ControllerLinkBuilder slash(Identifiable<?> identifyable) {

		if (identifyable == null) {
			return this;
		}

		return slash(identifyable.getId());
	}

	/**
	 * Returns a URI resulting from the builder.
	 * 
	 * @return
	 */
	public URI toUri() {
		return builder.encode().toUri();
	}

	public Link withRel(String rel) {
		return new Link(this.toString(), rel);
	}

	public Link withSelfRel() {
		return new Link(this.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toUri().normalize().toASCIIString();
	}
}
