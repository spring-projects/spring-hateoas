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
public class CollectionModel<T> extends RepresentationModel<CollectionModel<T>> implements Iterable<T> {

	private final Collection<T> content;

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

		Assert.notNull(content, "Content must not be null!");

		this.content = new ArrayList<>();

		for (T element : content) {
			this.content.add(element);
		}

		this.add(links);
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

	/**
	 * Returns the underlying elements.
	 *
	 * @return the content will never be {@literal null}.
	 */
	@JsonProperty("content")
	public Collection<T> getContent() {
		return Collections.unmodifiableCollection(content);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return content.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#toString()
	 */
	@Override
	public String toString() {
		return String.format("CollectionModel { content: %s, %s }", getContent(), super.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object obj) {

		if (obj == this) {
			return true;
		}

		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}

		CollectionModel<?> that = (CollectionModel<?>) obj;

		boolean contentEqual = this.content == null ? that.content == null : this.content.equals(that.content);
		return contentEqual && super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = super.hashCode();
		result += content == null ? 0 : 17 * content.hashCode();

		return result;
	}
}
