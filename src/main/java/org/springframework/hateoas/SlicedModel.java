/*
 * Copyright 2022 the original author or authors.
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

import java.util.*;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO to implement binding response representations of Slice collections.
 *
 * @author Michael Schout
 */
public class SlicedModel<T> extends CollectionModel<T> {
	public static SlicedModel<?> NO_SLICE = new SlicedModel<>();

	private final SliceMetadata metadata;
	private final @Nullable ResolvableType fallbackType;

	/**
	 * Default constructor to allow instantiation by reflection.
	 */
	protected SlicedModel() {
		this(new ArrayList<>(), null);
	}

	protected SlicedModel(Collection<T> content, @Nullable SliceMetadata metadata) {
		this(content, metadata, Links.NONE);
	}

	protected SlicedModel(Collection<T> content, @Nullable SliceMetadata metadata, Iterable<Link> links) {
		this(content, metadata, links, null);
	}

	protected SlicedModel(Collection<T> content, @Nullable SliceMetadata metadata, Iterable<Link> links,
			@Nullable ResolvableType fallbackType) {
		super(content, links, fallbackType);

		this.metadata = metadata;
		this.fallbackType = fallbackType;
	}

	/**
	 * Creates an empty {@link SlicedModel}.
	 *
	 * @param <T>
	 * @return will never be {@literal null}.
	 */
	public static <T> SlicedModel<T> empty() {
		return empty(Collections.emptyList());
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given fallback type.
	 *
	 * @param <T>
	 * @param fallbackElementType must not be {@literal null}.
	 * @param generics must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #withFallbackType(Class, Class...)
	 */
	public static <T> SlicedModel<T> empty(Class<T> fallbackElementType, Class<?> generics) {
		return empty(ResolvableType.forClassWithGenerics(fallbackElementType, generics));
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given fallback type.
	 *
	 * @param <T>
	 * @param fallbackElementType must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #withFallbackType(ParameterizedTypeReference)
	 */
	public static <T> SlicedModel<T> empty(ParameterizedTypeReference<T> fallbackElementType) {
		return empty(ResolvableType.forType(fallbackElementType));
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given fallback type.
	 *
	 * @param <T>
	 * @param fallbackElementType must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #withFallbackType(ResolvableType)
	 */
	public static <T> SlicedModel<T> empty(ResolvableType fallbackElementType) {
		return new SlicedModel<>(Collections.emptyList(), null, Collections.emptyList(), fallbackElementType);
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given links.
	 *
	 * @param <T>
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public static <T> SlicedModel<T> empty(Link... links) {
		return empty(null, links);
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given links.
	 *
	 * @param <T>
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public static <T> SlicedModel<T> empty(Iterable<Link> links) {
		return empty(null, links);
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given {@link SliceMetadata}.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @return
	 */
	public static <T> SlicedModel<T> empty(@Nullable SliceMetadata metadata) {
		return empty(metadata, Collections.emptyList());
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given {@link SliceMetadata} and fallback
	 * type.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param fallbackType must not be {@literal null}.
	 * @param generics must not be {@literal null}.
	 * @return will never be {@literal null}.
	 * @see #withFallbackType(Class, Class...)
	 */
	public static <T> SlicedModel<T> empty(@Nullable SliceMetadata metadata, Class<?> fallbackType,
			Class<?>... generics) {

		Assert.notNull(fallbackType, "Fallback type must not be null!");
		Assert.notNull(generics, "Generics must not be null!");

		return empty(metadata, ResolvableType.forClassWithGenerics(fallbackType, generics));
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given {@link SliceMetadata} and fallback
	 * type.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @return
	 * @see #withFallbackType(ParameterizedTypeReference)
	 */
	public static <T> SlicedModel<T> empty(@Nullable SliceMetadata metadata,
			ParameterizedTypeReference<T> fallbackType) {

		Assert.notNull(fallbackType, "Fallback type must not be null!");

		return empty(metadata, ResolvableType.forType(fallbackType));
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given {@link SliceMetadata} and fallback
	 * type.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param fallbackType must not be {@literal null}.
	 * @return
	 * @see #withFallbackType(ResolvableType)
	 */
	public static <T> SlicedModel<T> empty(@Nullable SliceMetadata metadata, ResolvableType fallbackType) {

		Assert.notNull(fallbackType, "Fallback type must not be null!");

		return new SlicedModel<>(Collections.emptyList(), metadata, Collections.emptyList(), fallbackType);
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given {@link SliceMetadata} and links.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public static <T> SlicedModel<T> empty(@Nullable SliceMetadata metadata, Link... links) {
		return empty(Arrays.asList(links));
	}

	/**
	 * Creates an empty {@link SlicedModel} with the given {@link SliceMetadata} and links.
	 *
	 * @param <T>
	 * @param metadata can be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public static <T> SlicedModel<T> empty(@Nullable SliceMetadata metadata, Iterable<Link> links) {
		return of(Collections.emptyList(), metadata, links);
	}

	/**
	 * Creates a new {@link SlicedModel} from the given content, {@link SliceMetadata} and
	 * {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata can be {@literal null}.
	 */
	public static <T> SlicedModel<T> of(Collection<T> content, @Nullable SliceMetadata metadata) {
		return new SlicedModel<>(content, metadata);
	}

	/**
	 * Creates a new {@link SlicedModel} from the given content, {@link SliceMetadata} and
	 * {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata can be {@literal null}.
	 * @param links
	 */
	public static <T> SlicedModel<T> of(Collection<T> content, @Nullable SliceMetadata metadata, Link... links) {
		return new SlicedModel<>(content, metadata, Arrays.asList(links));
	}

	/**
	 * Creates a new {@link SlicedModel} from the given content {@link SliceMetadata} and
	 * {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata can be {@literal null}.
	 * @param links
	 */
	public static <T> SlicedModel<T> of(Collection<T> content, @Nullable SliceMetadata metadata, Iterable<Link> links) {
		return new SlicedModel<>(content, metadata, links);
	}

	/**
	 * Factory method to easily create a {@link SlicedModel} instance from a set of entities
	 * and pagination metadata.
	 *
	 * @param content must not be {@literal null}.
	 * @param metadata
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityModel<S>, S> SlicedModel<T> wrap(Iterable<S> content, SliceMetadata metadata) {
		Assert.notNull(content, "Content must not be null!");
		ArrayList<T> resources = new ArrayList<>();

		for (S element : content) {
			resources.add((T) EntityModel.of(element));
		}

		return SlicedModel.of(resources, metadata);
	}

	/**
	 * Returns the pagination metadata.
	 *
	 * @return the metadata
	 */
	@JsonProperty("page")
	@Nullable
	public SliceMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Returns the Link pointing to the next slice (if set).
	 *
	 * @return
	 */
	@JsonIgnore
	public Optional<Link> getNextLink() {
		return getLink(IanaLinkRelations.NEXT);
	}

	/**
	 * Returns the Link pointing to the previous slice (if set).
	 *
	 * @return
	 */
	@JsonIgnore
	public Optional<Link> getPreviousLink() {
		return getLink(IanaLinkRelations.PREV);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.hateoas.CollectionModel#withFallbackType(java.lang.Class,
	 * java.lang.Class[])
	 */
	@Override
	public SlicedModel<T> withFallbackType(Class<? super T> type, Class<?>... generics) {
		return withFallbackType(ResolvableType.forClassWithGenerics(type, generics));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.hateoas.CollectionModel#withFallbackType(org.springframework.
	 * core.ParameterizedTypeReference)
	 */
	@Override
	public SlicedModel<T> withFallbackType(ParameterizedTypeReference<?> type) {
		return withFallbackType(ResolvableType.forType(type));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.hateoas.CollectionModel#withFallbackType(org.springframework.
	 * core.ResolvableType)
	 */
	@Override
	public SlicedModel<T> withFallbackType(ResolvableType type) {
		return new SlicedModel<>(getContent(), metadata, getLinks(), type);
	}

	@Override
	public String toString() {
		return String.format("SlicedModel { content: %s, fallbackType: %s, metadata: %s, links: %s }", //
				getContent(), fallbackType, metadata, getLinks());
	}

	/*
	 * (non-Javadoc)
	 *
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

		SlicedModel<?> that = (SlicedModel<?>) obj;

		return Objects.equals(this.metadata, that.metadata) //
				&& super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.hateoas.CollectionModel#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(metadata);
	}

	/**
	 * Value object for slice metadata.
	 *
	 * @author Michael Schout
	 */
	public static class SliceMetadata {
		@JsonProperty
		private long size;

		@JsonProperty
		private long number;

		protected SliceMetadata() {
		}

		/**
		 * Creates a new {@link SliceMetadata} from the given size, and slice number.
		 *
		 * @param size
		 * @param number zero-indexed slice number
		 */
		public SliceMetadata(long size, long number) {
			Assert.isTrue(size > -1, "Size must not be negative!");
			Assert.isTrue(number > -1, "Number must not be negative!");

			this.size = size;
			this.number = number;
		}

		/**
		 * Returns the requested size of the slice.
		 *
		 * @return the size a positive long.
		 */
		public long getSize() {
			return size;
		}

		/**
		 * Returns the number of the current slice.
		 *
		 * @return the number a positive long.
		 */
		public long getNumber() {
			return number;
		}

		@Override
		public String toString() {
			return String.format("Metadata: { number: %d, size %d )", number, size);
		}

		/*
		 * (non-Javadoc)
		 *
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

			SliceMetadata that = (SliceMetadata) obj;

			return this.number == that.number && this.size == that.size;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			int result = 17;
			result += 31 * (int) (this.number ^ this.number >>> 32);
			result += 31 * (int) (this.size ^ this.size >>> 32);

			return result;
		}
	}
}
