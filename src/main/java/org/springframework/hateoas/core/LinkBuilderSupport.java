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
package org.springframework.hateoas.core;

import static org.springframework.web.util.UriComponentsBuilder.*;

import java.net.URI;

import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Base class to implement {@link LinkBuilder}s based on a Spring MVC {@link UriComponentsBuilder}.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Kamill Sokol
 */
public abstract class LinkBuilderSupport<T extends LinkBuilder> implements LinkBuilder {

	private final UriComponents uriComponents;

	/**
	 * Creates a new {@link LinkBuilderSupport} using the given {@link UriComponentsBuilder}.
	 * 
	 * @param builder must not be {@literal null}.
	 */
	public LinkBuilderSupport(UriComponentsBuilder builder) {

		Assert.notNull(builder);
		this.uriComponents = builder.build();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(java.lang.Object)
	 */
	public T slash(Object object) {

		if (object == null) {
			return getThis();
		}

		if (object instanceof Identifiable) {
			return slash((Identifiable<?>) object);
		}

		String path = object.toString();

		if (path.endsWith("#")) {
			path = path.substring(0, path.length() - 1);
		}

		if (!StringUtils.hasText(path)) {
			return getThis();
		}

		String uriString = uriComponents.toUriString();
		UriComponentsBuilder builder = uriString.isEmpty() ? fromUri(uriComponents.toUri())
				: fromUriString(uriString);

		UriComponents components = UriComponentsBuilder.fromUriString(path).build();

		for (String pathSegment : components.getPathSegments()) {
			builder.pathSegment(pathSegment);
		}

		String fragment = components.getFragment();
		if (StringUtils.hasText(fragment)) {
			builder.fragment(fragment);
		}

		return createNewInstance(builder.query(components.getQuery()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(org.springframework.hateoas.Identifiable)
	 */
	public T slash(Identifiable<?> identifyable) {

		if (identifyable == null) {
			return getThis();
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
		return withRel(Link.REL_SELF);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toUri().normalize().toASCIIString();
	}

	/**
	 * Returns the current concrete instance.
	 * 
	 * @return
	 */
	protected abstract T getThis();

	/**
	 * Creates a new instance of the sub-class.
	 * 
	 * @param builder will never be {@literal null}.
	 * @return
	 */
	protected abstract T createNewInstance(UriComponentsBuilder builder);
}
