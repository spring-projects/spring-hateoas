/*
 * Copyright 2012-2021 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static org.springframework.hateoas.server.core.EncodingUtils.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.lang.Nullable;
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
 * @author Kevin Conaway
 * @author Greg Turnquist
 */
public abstract class LinkBuilderSupport<T extends LinkBuilder> implements LinkBuilder {

	private final List<Affordance> affordances;

	private UriComponents components;

	/**
	 * Creates a new {@link LinkBuilderSupport} using the given {@link UriComponents}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	protected LinkBuilderSupport(UriComponents builder) {
		this(builder, Collections.emptyList());
	}

	protected LinkBuilderSupport(UriComponents components, List<Affordance> affordances) {

		Assert.notNull(components, "UriComponents must not be null!");
		Assert.notNull(affordances, "Affordances must not be null!");

		// this.builder = UriComponentsBuilder.newInstance().uriComponents(components);
		this.affordances = affordances;

		this.components = components;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#slash(java.lang.Object)
	 */
	public T slash(@Nullable Object object) {

		object = object instanceof Optional ? ((Optional<?>) object).orElse(null) : object;

		if (object == null) {
			return getThis();
		}

		String path = object.toString();

		if (path.endsWith("#")) {
			path = path.substring(0, path.length() - 1);
		}

		if (!StringUtils.hasText(path)) {
			return getThis();
		}

		path = path.startsWith("/") ? path : "/".concat(path);

		return slash(UriComponentsBuilder.fromUriString(path).build(), false);
	}

	protected T slash(UriComponents components, boolean encoded) {

		UriComponentsBuilder builder = UriComponentsBuilder.newInstance().uriComponents(this.components);

		for (String pathSegment : components.getPathSegments()) {
			builder.pathSegment(encoded ? pathSegment : encodePath(pathSegment));
		}

		String fragment = components.getFragment();

		if (fragment != null && !fragment.trim().isEmpty()) {
			builder.fragment(encoded ? fragment : encodeFragment(fragment));
		}

		return createNewInstance(builder.query(components.getQuery()).build(), affordances);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#toUri()
	 */
	public URI toUri() {
		return components.toUri().normalize();
	}

	public T addAffordances(Collection<Affordance> affordances) {

		List<Affordance> newAffordances = new ArrayList<>();
		newAffordances.addAll(this.affordances);
		newAffordances.addAll(affordances);

		return createNewInstance(components, newAffordances);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withRel(org.springframework.hateoas.LinkRelation)
	 */
	public Link withRel(LinkRelation rel) {

		return Link.of(toString(), rel) //
				.withAffordances(affordances);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilder#withSelfRel()
	 */
	public Link withSelfRel() {
		return withRel(IanaLinkRelations.SELF);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return components.toUriString();
	}

	/**
	 * Returns the current concrete instance.
	 *
	 * @return
	 */
	protected abstract T getThis();

	/**
	 * Creates a new instance of the sub-class.
	 */
	protected abstract T createNewInstance(UriComponents components, List<Affordance> affordances);

	public List<Affordance> getAffordances() {
		return this.affordances;
	}
}
