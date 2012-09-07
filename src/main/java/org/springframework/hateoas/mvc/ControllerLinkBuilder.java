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
import org.springframework.hateoas.LinkBuilder;
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
public class ControllerLinkBuilder implements LinkBuilder {

	private final UriComponents uriComponents;

	/**
	 * Creates a new {@link ControllerLinkBuilder}.
	 * 
	 * @param uriComponents must not be {@literal null}.
	 */
	private ControllerLinkBuilder(UriComponentsBuilder builder) {
		Assert.notNull(builder);
		this.uriComponents = builder.build();
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class.
	 * 
	 * @param controller must not be {@literal null}.
	 * @return
	 */
	public static ControllerLinkBuilder linkTo(Class<?> controller) {
		return linkTo(controller, new Object[0]);
	}

	/**
	 * Creates a new {@link ControllerLinkBuilder} with a base of the mapping annotated to the given controller class. The
	 * additional parameters are used to fill up potentially available path variables in the class scop request mapping.
	 * 
	 * @param controller must not be {@literal null}.
	 * @param parameters
	 * @return
	 */
	public static ControllerLinkBuilder linkTo(Class<?> controller, Object... parameters) {

		Assert.notNull(controller);

		RequestMapping annotation = AnnotationUtils.findAnnotation(controller, RequestMapping.class);
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(java.lang.Object)
	 */
	public ControllerLinkBuilder slash(Object object) {

		if (object == null) {
			return this;
		}

		String[] segments = StringUtils.tokenizeToStringArray(object.toString(), "/");
		return new ControllerLinkBuilder(UriComponentsBuilder.fromUri(uriComponents.toUri()).pathSegment(segments));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(org.springframework.hateoas.Identifiable)
	 */
	public ControllerLinkBuilder slash(Identifiable<?> identifyable) {

		if (identifyable == null) {
			return this;
		}

		return slash(identifyable.getId());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#toUri()
	 */
	public URI toUri() {
		return uriComponents.encode().toUri();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withRel(java.lang.String)
	 */
	public Link withRel(String rel) {
		return new Link(this.toString(), rel);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withSelfRel()
	 */
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
