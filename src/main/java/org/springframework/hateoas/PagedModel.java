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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO to implement binding response representations of pageable collections.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class PagedModel<T> extends CollectionModel<T> {

	public static PagedModel<?> NO_PAGE = new PagedModel<>();

	// Not final to allow population by Jackson
	private @Nullable PageMetadata metadata;
	private @Nullable ResolvableType fallbackType;

	/**
	 * Default constructor to allow instantiation by reflection.
	 */
	protected PagedModel() {
		this(new ArrayList<>(), null);
	}

	protected PagedModel(Collection<T> content, @Nullable PageMetadata metadata) {
		this(content, metadata, Links.NONE);
	}

	protected PagedModel(Collection<T> content, @Nullable PageMetadata metadata, Iterable<Link> links) {
		this(content, metadata, links, null);
	}

	protected PagedModel(Collection<T> content, @Nullable PageMetadata metadata, Iterable<Link> links,
			@Nullable ResolvableType fallbackType) {

		super(content, links, fallbackType);

		this.metadata = metadata;
		this.fallbackType = fallbackType;
	}

	/**
	 * Creates an empty {@link PagedModel}.
	 *
	 * @param <T>
	 * @return will never be {@literal null}.
	 * @since 1.1
	 */
	public static <T> PagedModel<T> empty() {
		return empty(Collections.emptyList());
	}

	/**
	 * Creates an empty {@link PagedModel} with the given fallback type.
	 *
	 * @param <T>
	 * @param fallbackElementType must not be {@literal null}.
	 * @param generics must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 * @see #withFallbackType(Class, Class...)
	 */
	public static <T> PagedModel<T> empty(Class<T> fallbackElementType, Class<?> generics) {
		return empty(ResolvableType.forClassWithGenerics(fallbackElementType, generics));
	}

	/**
	 * Creates an empty {@link PagedModel} with the given fallback type.
	 *
	 * @param <T>
	 * @param fallbackElementType must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 * @see #withFallbackType(ParameterizedTypeReference)
	 */
	public static <T> PagedModel<T> empty(ParameterizedTypeReference<T> fallbackElementType) {
		return empty(ResolvableType.forType(fallbackElementType));
	}

	/**
	 * Creates an empty {@link PagedModel} with the given fallback type.
	 *
	 * @param <T>
	 * @param fallbackElementType must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 * @see #withFallbackType(ResolvableType)
	 */
	public static <T> PagedModel<T> empty(ResolvableType fallbackElementType) {
		return new PagedModel<>(Collections.emptyList(), null, Collections.emptyList(), fallbackElementType);
	}

	/**
	 * Creates an empty {@link PagedModel} with the given links.
	 *
	 * @param <T>
	 * @param links must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> PagedModel<T> empty(Link... links) {
		return empty((PageMetadata) null, links);
	}

	/**
	 * Creates an empty {@link PagedModel} with the given links.
	 *
	 * @param <T>
	 * @param links must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> PagedModel<T> empty(Iterable<Link> links) {
		return empty((PageMetadata) null, links);
	}

	/**
	 * Creates an empty {@link PagedModel} with the given {@link PageMetadata}.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> PagedModel<T> empty(@Nullable PageMetadata metadata) {
		return empty(metadata, Collections.emptyList());
	}

	/**
	 * Creates an empty {@link PagedModel} with the given {@link PageMetadata} and fallback type.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param fallbackType must not be {@literal null}.
	 * @param generics must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @since 1.4
	 * @see #withFallbackType(Class, Class...)
	 */
	public static <T> PagedModel<T> empty(@Nullable PageMetadata metadata, Class<?> fallbackType, Class<?>... generics) {

		Assert.notNull(fallbackType, "Fallback type must not be null!");
		Assert.notNull(generics, "Generics must not be null!");

		return empty(metadata, ResolvableType.forClassWithGenerics(fallbackType, generics));
	}

	/**
	 * Creates an empty {@link PagedModel} with the given {@link PageMetadata} and fallback type.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @return
	 * @since 1.4
	 * @see #withFallbackType(ParameterizedTypeReference)
	 */
	public static <T> PagedModel<T> empty(@Nullable PageMetadata metadata, ParameterizedTypeReference<T> fallbackType) {

		Assert.notNull(fallbackType, "Fallback type must not be null!");

		return empty(metadata, ResolvableType.forType(fallbackType));
	}

	/**
	 * Creates an empty {@link PagedModel} with the given {@link PageMetadata} and fallback type.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param fallbackType must not be {@literal null}.
	 * @return
	 * @since 1.4
	 * @see #withFallbackType(ResolvableType)
	 */
	public static <T> PagedModel<T> empty(@Nullable PageMetadata metadata, ResolvableType fallbackType) {

		Assert.notNull(fallbackType, "Fallback type must not be null!");

		return new PagedModel<>(Collections.emptyList(), metadata, Collections.emptyList(), fallbackType);
	}

	/**
	 * Creates an empty {@link PagedModel} with the given {@link PageMetadata} and links.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> PagedModel<T> empty(@Nullable PageMetadata metadata, Link... links) {
		return empty(Arrays.asList(links));
	}

	/**
	 * Creates an empty {@link PagedModel} with the given {@link PageMetadata} and links.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> PagedModel<T> empty(@Nullable PageMetadata metadata, Iterable<Link> links) {
		return of(Collections.emptyList(), metadata, links);
	}

	/**
	 * Creates a new {@link PagedModel} from the given content, {@link PageMetadata} and {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata can be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static <T> PagedModel<T> of(Collection<T> content, @Nullable PageMetadata metadata) {
		return new PagedModel<>(content, metadata);
	}

	/**
	 * Creates a new {@link PagedModel} from the given content, {@link PageMetadata} and {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata can be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static <T> PagedModel<T> of(Collection<T> content, @Nullable PageMetadata metadata, Link... links) {
		return new PagedModel<>(content, metadata, List.of(links));
	}

	/**
	 * Creates a new {@link PagedModel} from the given content {@link PageMetadata} and {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata can be {@literal null}.
	 * @param links must not be {@literal null}.
	 */
	public static <T> PagedModel<T> of(Collection<T> content, @Nullable PageMetadata metadata, Iterable<Link> links) {
		return new PagedModel<>(content, metadata, links);
	}

	/**
	 * Returns the pagination metadata.
	 *
	 * @return the metadata
	 */
	@JsonProperty("page")
	public @Nullable PageMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Factory method to easily create a {@link PagedModel} instance from a set of entities and pagination metadata.
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityModel<S>, S> PagedModel<T> wrap(Iterable<S> content, PageMetadata metadata) {

		Assert.notNull(content, "Content must not be null!");
		ArrayList<T> resources = new ArrayList<>();

		for (S element : content) {
			resources.add((T) EntityModel.of(element));
		}

		return PagedModel.of(resources, metadata);
	}

	/**
	 * Returns the Link pointing to the next page (if set).
	 *
	 * @return
	 */
	@JsonIgnore
	public Optional<Link> getNextLink() {
		return getLink(IanaLinkRelations.NEXT);
	}

	/**
	 * Returns the Link pointing to the previous page (if set).
	 *
	 * @return
	 */
	@JsonIgnore
	public Optional<Link> getPreviousLink() {
		return getLink(IanaLinkRelations.PREV);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.CollectionModel#withFallbackType(java.lang.Class, java.lang.Class[])
	 */
	@Override
	public PagedModel<T> withFallbackType(Class<? super T> type, Class<?>... generics) {
		return withFallbackType(ResolvableType.forClassWithGenerics(type, generics));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.CollectionModel#withFallbackType(org.springframework.core.ParameterizedTypeReference)
	 */
	@Override
	public PagedModel<T> withFallbackType(ParameterizedTypeReference<?> type) {
		return withFallbackType(ResolvableType.forType(type));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.CollectionModel#withFallbackType(org.springframework.core.ResolvableType)
	 */
	@Override
	public PagedModel<T> withFallbackType(ResolvableType type) {
		return new PagedModel<>(getContent(), metadata, getLinks(), type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#toString()
	 */
	@Override
	public String toString() {
		return String.format("PagedModel { content: %s, fallbackType: %s, metadata: %s, links: %s }", //
				getContent(), fallbackType, metadata, getLinks());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.CollectionModel#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}

		PagedModel<?> that = (PagedModel<?>) obj;

		return Objects.equals(this.metadata, that.metadata) //
				&& super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.CollectionModel#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(metadata);
	}

	/**
	 * Value object for pagination metadata.
	 *
	 * @author Oliver Gierke
	 */
	public static class PageMetadata {

		@JsonProperty private long size;
		@JsonProperty private long totalElements;
		@JsonProperty private long totalPages;
		@JsonProperty private long number;

		protected PageMetadata() {

		}

		/**
		 * Creates a new {@link PageMetadata} from the given size, number, total elements and total pages.
		 *
		 * @param size
		 * @param number zero-indexed, must be less than totalPages
		 * @param totalElements
		 * @param totalPages
		 */
		public PageMetadata(long size, long number, long totalElements, long totalPages) {

			Assert.isTrue(size > -1, "Size must not be negative!");
			Assert.isTrue(number > -1, "Number must not be negative!");
			Assert.isTrue(totalElements > -1, "Total elements must not be negative!");
			Assert.isTrue(totalPages > -1, "Total pages must not be negative!");

			this.size = size;
			this.number = number;
			this.totalElements = totalElements;
			this.totalPages = totalPages;
		}

		/**
		 * Creates a new {@link PageMetadata} from the given size, number and total elements.
		 *
		 * @param size the size of the page
		 * @param number the number of the page
		 * @param totalElements the total number of elements available
		 */
		public PageMetadata(long size, long number, long totalElements) {
			this(size, number, totalElements, size == 0 ? 0 : (long) Math.ceil((double) totalElements / (double) size));
		}

		/**
		 * Returns the requested size of the page.
		 *
		 * @return the size a positive long.
		 */
		public long getSize() {
			return size;
		}

		/**
		 * Returns the total number of elements available.
		 *
		 * @return the totalElements a positive long.
		 */
		public long getTotalElements() {
			return totalElements;
		}

		/**
		 * Returns how many pages are available in total.
		 *
		 * @return the totalPages a positive long.
		 */
		public long getTotalPages() {
			return totalPages;
		}

		/**
		 * Returns the number of the current page.
		 *
		 * @return the number a positive long.
		 */
		public long getNumber() {
			return number;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("Metadata { number: %d, total pages: %d, total elements: %d, size: %d }", number, totalPages,
					totalElements, size);
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(@Nullable Object obj) {

			if (this == obj) {
				return true;
			}

			if (obj == null || !obj.getClass().equals(getClass())) {
				return false;
			}

			PageMetadata that = (PageMetadata) obj;

			return this.number == that.number && this.size == that.size && this.totalElements == that.totalElements
					&& this.totalPages == that.totalPages;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {

			int result = 17;
			result += 31 * (int) (this.number ^ this.number >>> 32);
			result += 31 * (int) (this.size ^ this.size >>> 32);
			result += 31 * (int) (this.totalElements ^ this.totalElements >>> 32);
			result += 31 * (int) (this.totalPages ^ this.totalPages >>> 32);
			return result;
		}
	}
}
