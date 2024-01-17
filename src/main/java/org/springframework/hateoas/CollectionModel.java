/*
 * Copyright 2012-2024 the original author or authors.
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
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * General helper to easily create a wrapper for a collection of entities.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class CollectionModel<T> extends RepresentationModel<CollectionModel<T>>
		implements Iterable<T>, ResolvableTypeProvider {

	private final Collection<T> content;
	private final @Nullable ResolvableType fallbackType;
	private ResolvableType fullType;

	/**
	 * Creates an empty {@link CollectionModel} instance.
	 */
	protected CollectionModel() {
		this(Collections.emptyList());
	}

	protected CollectionModel(Iterable<T> content) {
		this(content, Links.NONE, null);
	}

	protected CollectionModel(Iterable<T> content, Iterable<Link> links, @Nullable ResolvableType fallbackType) {

		Assert.notNull(content, "Content must not be null!");
		Assert.notNull(links, "Links must not be null!");

		this.content = new ArrayList<>();

		for (T element : content) {
			this.content.add(element);
		}

		this.add(links);
		this.fallbackType = fallbackType;
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
	 * Creates a new empty collection model with the given type defined as fallback type.
	 *
	 * @param <T>
	 * @return
	 * @since 1.4
	 * @see #withFallbackType(Class, Class...)
	 */
	public static <T> CollectionModel<T> empty(Class<T> elementType, Class<?>... generics) {
		return empty(ResolvableType.forClassWithGenerics(elementType, generics));
	}

	/**
	 * Creates a new empty collection model with the given type defined as fallback type.
	 *
	 * @param <T>
	 * @return
	 * @since 1.4
	 * @see #withFallbackType(ParameterizedTypeReference)
	 */
	public static <T> CollectionModel<T> empty(ParameterizedTypeReference<T> type) {
		return empty(ResolvableType.forType(type.getType()));
	}

	/**
	 * Creates a new empty collection model with the given type defined as fallback type.
	 *
	 * @param <T>
	 * @return
	 * @since 1.4
	 * @see #withFallbackType(ResolvableType)
	 */
	public static <T> CollectionModel<T> empty(ResolvableType elementType) {
		return new CollectionModel<>(Collections.emptyList(), Collections.emptyList(), elementType);
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
	 * @see #withFallbackType(Class, Class...)
	 * @see #withFallbackType(ResolvableType)
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
	 * @see #withFallbackType(Class, Class...)
	 * @see #withFallbackType(ResolvableType)
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
	 * @see #withFallbackType(Class, Class...)
	 * @see #withFallbackType(ResolvableType)
	 */
	public static <T> CollectionModel<T> of(Iterable<T> content, Iterable<Link> links) {
		return new CollectionModel<>(content, links, null);
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

	/**
	 * Declares the given type as fallback element type in case the underlying collection is empty. This allows client
	 * components to still apply type matches at runtime.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 */
	public CollectionModel<T> withFallbackType(Class<? super T> type, Class<?>... generics) {

		Assert.notNull(type, "Fallback type must not be null!");
		Assert.notNull(generics, "Generics must not be null!");

		return withFallbackType(ResolvableType.forClassWithGenerics(type, generics));
	}

	/**
	 * Declares the given type as fallback element type in case the underlying collection is empty. This allows client
	 * components to still apply type matches at runtime.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 */
	public CollectionModel<T> withFallbackType(ParameterizedTypeReference<?> type) {

		Assert.notNull(type, "Fallback type must not be null!");

		return withFallbackType(ResolvableType.forType(type));
	}

	/**
	 * Declares the given type as fallback element type in case the underlying collection is empty. This allows client
	 * components to still apply type matches at runtime.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 */
	public CollectionModel<T> withFallbackType(ResolvableType type) {

		Assert.notNull(type, "Fallback type must not be null!");

		return new CollectionModel<>(content, getLinks(), type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.core.ResolvableTypeProvider#getResolvableType()
	 */
	@NonNull
	@Override
	@JsonIgnore
	public ResolvableType getResolvableType() {

		if (fullType == null) {

			ResolvableType elementType = deriveElementType(this.content, fallbackType);
			Class<?> type = this.getClass();

			this.fullType = elementType == null || type.getTypeParameters().length == 0 //
					? ResolvableType.forClass(type) //
					: ResolvableType.forClassWithGenerics(type, elementType);
		}

		return fullType;
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

		return String.format("CollectionModel { content: %s, fallbackType: %s, %s }", //
				getContent(), fallbackType, super.toString());
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

		return Objects.equals(this.content, that.content)
				&& Objects.equals(this.fallbackType, that.fallbackType)
				&& super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(content, fallbackType);
	}

	/**
	 * Determines the most common element type from the given elements defaulting to the given fallback type.
	 *
	 * @param elements must not be {@literal null}.
	 * @param fallbackType can be {@literal null}.
	 * @return
	 */
	@Nullable
	private static ResolvableType deriveElementType(Collection<?> elements, @Nullable ResolvableType fallbackType) {

		if (elements.isEmpty()) {
			return fallbackType;
		}

		return elements.stream()
				.filter(it -> it != null)
				.<Class<?>> map(Object::getClass)
				.reduce(ClassUtils::determineCommonAncestor)
				.map(ResolvableType::forClass)
				.orElse(fallbackType);
	}
}
