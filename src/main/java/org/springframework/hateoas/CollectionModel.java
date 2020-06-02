/*
 * Copyright 2012-2020 the original author or authors.
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
import java.util.Iterator;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * General helper to easily create a wrapper for a collection of entities.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class CollectionModel<T> extends AbstractCollectionModel<CollectionModel<T>, T> implements Iterable<T> {

	/**
	 * Creates an empty {@link CollectionModel} instance.
	 */
	protected CollectionModel() {
		this(new ArrayList<>());
	}

	/**
	 * Creates a {@link CollectionModel} instance with the given content and {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link CollectionModel}.
	 * @deprecated since 1.1, use {@link #of(Iterable, Link...)} instead.
	 */
	@Deprecated
	public CollectionModel(Iterable<T> content, Link... links) {
		this(content, Arrays.asList(links));
	}

	/**
	 * Creates a {@link CollectionModel} instance with the given content and {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link CollectionModel}.
	 * @deprecated since 1.1, use {@link #of(Iterable, Iterable)} instead.
	 */
	@Deprecated
	public CollectionModel(Iterable<T> content, Iterable<Link> links) {
		super(content, links);
	}

	/**
	 * Creates a new empty collection model.
	 *
	 * @param <T>
	 * @return
	 * @since 1.1
	 */
	public static <T> CollectionModel<T> empty() {
		return of(Collections.emptyList());
	}

	/**
	 * Creates a new empty collection model with the given links.
	 *
	 * @param <T>
	 * @param links must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> CollectionModel<T> empty(Link... links) {
		return of(Collections.emptyList(), links);
	}

	/**
	 * Creates a new empty collection model with the given links.
	 *
	 * @param <T>
	 * @param links must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> CollectionModel<T> empty(Iterable<Link> links) {
		return of(Collections.emptyList(), links);
	}

	/**
	 * Creates a {@link CollectionModel} instance with the given content.
	 *
	 * @param content must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> CollectionModel<T> of(Iterable<T> content) {
		return of(content, Collections.emptyList());
	}

	/**
	 * Creates a {@link CollectionModel} instance with the given content and {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link CollectionModel}.
	 * @return
	 * @since 1.1
	 */
	public static <T> CollectionModel<T> of(Iterable<T> content, Link... links) {
		return of(content, Arrays.asList(links));
	}

	/**
	 * s Creates a {@link CollectionModel} instance with the given content and {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link CollectionModel}.
	 * @return
	 * @since 1.1
	 */
	public static <T> CollectionModel<T> of(Iterable<T> content, Iterable<Link> links) {
		return new CollectionModel<>(content, links);
	}

	/**
	 * Creates a new {@link CollectionModel} instance by wrapping the given domain class instances into a
	 * {@link EntityModel}.
	 *
	 * @param content must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityModel<S>, S> CollectionModel<T> wrap(Iterable<S> content) {

		Assert.notNull(content, "Content must not be null!");

		ArrayList<T> resources = new ArrayList<>();

		for (S element : content) {
			resources.add((T) EntityModel.of(element));
		}

		return CollectionModel.of(resources);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceSupport#toString()
	 */
	@Override
	public String toString() {
		return String.format("Resources { content: %s, %s }", getContent(), super.toString());
	}

}
