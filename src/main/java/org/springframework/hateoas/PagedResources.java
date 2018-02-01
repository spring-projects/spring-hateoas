/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springframework.hateoas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO to implement binding response representations of pageable collections.
 * 
 * @author Oliver Gierke
 */
public class PagedResources<T> extends Resources<T> {

	public static PagedResources<?> NO_PAGE = new PagedResources<>();

	private PageMetadata metadata;

	/**
	 * Default constructor to allow instantiation by reflection.
	 */
	protected PagedResources() {
		this(new ArrayList<T>(), null);
	}

	/**
	 * Creates a new {@link PagedResources} from the given content, {@link PageMetadata} and {@link Link}s (optional).
	 * 
	 * @param content must not be {@literal null}.
	 * @param metadata
	 * @param links
	 */
	public PagedResources(Collection<T> content, PageMetadata metadata, Link... links) {
		this(content, metadata, Arrays.asList(links));
	}

	/**
	 * Creates a new {@link PagedResources} from the given content {@link PageMetadata} and {@link Link}s.
	 * 
	 * @param content must not be {@literal null}.
	 * @param metadata
	 * @param links
	 */
	public PagedResources(Collection<T> content, PageMetadata metadata, Iterable<Link> links) {
		super(content, links);
		this.metadata = metadata;
	}

	/**
	 * Returns the pagination metadata.
	 * 
	 * @return the metadata
	 */
	@JsonProperty("page")
	public PageMetadata getMetadata() {
		return metadata;
	}

	/**
	 * Factory method to easily create a {@link PagedResources} instance from a set of entities and pagination metadata.
	 * 
	 * @param content must not be {@literal null}.
	 * @param metadata
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Resource<S>, S> PagedResources<T> wrap(Iterable<S> content, PageMetadata metadata) {

		Assert.notNull(content, "Content must not be null!");
		ArrayList<T> resources = new ArrayList<T>();

		for (S element : content) {
			resources.add((T) new Resource<S>(element));
		}

		return new PagedResources<T>(resources, metadata);
	}

	/**
	 * Returns the Link pointing to the next page (if set).
	 * 
	 * @return
	 */
	@JsonIgnore
	public Optional<Link> getNextLink() {
		return getLink(Link.REL_NEXT);
	}

	/**
	 * Returns the Link pointing to the previous page (if set).
	 * 
	 * @return
	 */
	@JsonIgnore
	public Optional<Link> getPreviousLink() {
		return getLink(Link.REL_PREVIOUS);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceSupport#toString()
	 */
	@Override
	public String toString() {
		return String.format("PagedResource { content: %s, metadata: %s, links: %s }", getContent(), metadata, getLinks());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Resources#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}

		PagedResources<?> that = (PagedResources<?>) obj;
		boolean metadataEquals = this.metadata == null ? that.metadata == null : this.metadata.equals(that.metadata);

		return metadataEquals ? super.equals(obj) : false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.Resources#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = super.hashCode();
		result += this.metadata == null ? 0 : 31 * this.metadata.hashCode();
		return result;
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
		public boolean equals(Object obj) {

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
