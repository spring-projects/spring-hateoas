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
package org.springframework.hateoas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for DTOs to collect links.
 *
 * @author Oliver Gierke
 * @author Johhny Lim
 * @author Greg Turnquist
 */
public class RepresentationModel<T extends RepresentationModel<? extends T>> {

	private final List<Link> links;

	public RepresentationModel() {
		this.links = new ArrayList<>();
	}

	public RepresentationModel(Link initialLink) {

		Assert.notNull(initialLink, "initialLink must not be null!");

		this.links = new ArrayList<>();
		this.links.add(initialLink);
	}

	public RepresentationModel(Iterable<Link> initialLinks) {

		Assert.notNull(initialLinks, "initialLinks must not be null!");

		this.links = new ArrayList<>();

		for (Link link : initialLinks) {
			this.links.add(link);
		}
	}

	/**
	 * Creates a new {@link RepresentationModel} for the given content object and no links.
	 *
	 * @param object can be {@literal null}.
	 * @return
	 * @see #of(Object, Iterable)
	 */
	public static <T> RepresentationModel<?> of(@Nullable T object) {
		return of(object, Collections.emptyList());
	}

	/**
	 * Creates a new {@link RepresentationModel} for the given content object and links. Will return a simple
	 * {@link RepresentationModel} if the content is {@literal null}, a {@link CollectionModel} in case the given content
	 * object is a {@link Collection} or an {@link EntityModel} otherwise.
	 *
	 * @param object can be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public static <T> RepresentationModel<?> of(@Nullable T object, Iterable<Link> links) {

		if (object == null) {
			return new RepresentationModel<>(links);
		}

		if (Collection.class.isInstance(object)) {
			return CollectionModel.of((Collection<?>) object, links);
		}

		return EntityModel.of(object, links);
	}

	/**
	 * Adds the given link to the resource.
	 *
	 * @param link
	 */
	@SuppressWarnings("unchecked")
	public T add(Link link) {

		Assert.notNull(link, "Link must not be null!");

		this.links.add(link);

		return (T) this;
	}

	/**
	 * Adds all given {@link Link}s to the resource.
	 *
	 * @param links must not be {@literal null}.
	 * @see Links
	 */
	@SuppressWarnings("unchecked")
	public T add(Iterable<Link> links) {

		Assert.notNull(links, "Given links must not be null!");

		links.forEach(this::add);

		return (T) this;
	}

	/**
	 * Adds all given {@link Link}s to the resource.
	 *
	 * @param links must not be {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	public T add(Link... links) {

		Assert.notNull(links, "Given links must not be null!");

		add(Arrays.asList(links));

		return (T) this;
	}

	/**
	 * Adds the {@link Link} produced by the given Supplier if the guard is {@literal true}.
	 *
	 * @param guard whether to add the {@link Link} produced by the given {@link Supplier}.
	 * @param link the {@link Link} to add in case the guard is {@literal true}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T addIf(boolean guard, Supplier<Link> link) {

		if (guard) {
			add(link.get());
		}

		return (T) this;
	}

	/**
	 * Adds all {@link Link}s produced by the given Supplier if the guard is {@literal true}.
	 *
	 * @param guard whether to add the {@link Link}s produced by the given {@link Supplier}.
	 * @param links the {@link Link}s to add in case the guard is {@literal true}.
	 * @return
	 * @see Links
	 */
	@SuppressWarnings("unchecked")
	public T addAllIf(boolean guard, Supplier<? extends Iterable<Link>> links) {

		if (guard) {
			add(links.get());
		}

		return (T) this;
	}

	/**
	 * Returns whether the resource contains {@link Link}s at all.
	 *
	 * @return
	 */
	public boolean hasLinks() {
		return !this.links.isEmpty();
	}

	/**
	 * Returns whether the resource contains a {@link Link} with the given rel.
	 *
	 * @param rel
	 * @return
	 */
	public boolean hasLink(String rel) {
		return getLink(rel).isPresent();
	}

	public boolean hasLink(LinkRelation rel) {
		return hasLink(rel.value());
	}

	/**
	 * Returns all {@link Link}s contained in this resource.
	 *
	 * @return
	 */
	@JsonProperty("links")
	public Links getLinks() {
		return Links.of(links);
	}

	/**
	 * Removes all {@link Link}s added to the resource so far.
	 */
	@SuppressWarnings("unchecked")
	public T removeLinks() {

		this.links.clear();

		return (T) this;
	}

	/**
	 * Returns the link with the given relation.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return the link with the given relation or {@link Optional#empty()} if none found.
	 */
	public Optional<Link> getLink(String relation) {
		return getLink(LinkRelation.of(relation));
	}

	/**
	 * Returns the link with the given {@link LinkRelation}.
	 *
	 * @param relation
	 * @return
	 */
	public Optional<Link> getLink(LinkRelation relation) {

		return links.stream() //
				.filter(it -> it.hasRel(relation)) //
				.findFirst();
	}

	/**
	 * Returns the link with the given relation.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return the link with the given relation.
	 * @throws IllegalArgumentException in case no link with the given relation can be found.
	 */
	public Link getRequiredLink(String relation) {

		return getLink(relation) //
				.orElseThrow(() -> new IllegalArgumentException(String.format("No link with rel %s found!", relation)));
	}

	/**
	 * Returns the link with the given relation.
	 *
	 * @param relation must not be {@literal null}.
	 * @return the link with the given relation.
	 * @throws IllegalArgumentException in case no link with the given relation can be found.
	 */
	public Link getRequiredLink(LinkRelation relation) {

		Assert.notNull(relation, "Link relation must not be null!");

		return getRequiredLink(relation.value());
	}

	/**
	 * Returns all {@link Link}s with the given relation.
	 *
	 * @param relation must not be {@literal null}.
	 * @return the links in a {@link List}
	 */
	public List<Link> getLinks(String relation) {

		Assert.hasText(relation, "Link relation must not be null or empty!");

		return links.stream() //
				.filter(link -> link.hasRel(relation)) //
				.collect(Collectors.toList());
	}

	/**
	 * Returns all {@link Link}s with the given relation.
	 *
	 * @param relation must not be {@literal null}.
	 * @return the links in a {@link List}
	 */
	public List<Link> getLinks(LinkRelation relation) {

		Assert.notNull(relation, "Link relation must not be null!");

		return getLinks(relation.value());
	}

	/**
	 * Replaces the link(s) with the given {@link LinkRelation} with the mapper applied to each of the links.
	 *
	 * @param relation the {@link LinkRelation} to select the source link(s), must not be {@literal null}.
	 * @param mapper the {@link Function} to apply to the current link, must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.3
	 */
	public T mapLink(LinkRelation relation, Function<Link, Link> mapper) {

		getLinks(relation).forEach(it -> {

			links.remove(it);
			links.add(mapper.apply(it));
		});

		return (T) this;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("links: %s", links.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(@Nullable Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || !obj.getClass().equals(this.getClass())) {
			return false;
		}

		T that = (T) obj;

		return getLinks().equals(that.getLinks());
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.links.hashCode();
	}
}
