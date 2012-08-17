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
package org.springframework.hateoas;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * DTO to implement binding response representations of pageable collections.
 * 
 * @author Oliver Gierke
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class PagedResources<T extends Resource<?>> extends Resources<T> {

	@JsonProperty("page")
	private PageMetadata pageMetadata;

	/**
	 * Returns the pagination metadata.
	 * 
	 * @return the pageMetadata
	 */
	public PageMetadata getMetadata() {
		return pageMetadata;
	}

	/**
	 * Value object for pagination metadata.
	 * 
	 * @author Oliver Gierke
	 */
	@JsonAutoDetect(fieldVisibility = Visibility.ANY)
	public static class PageMetadata {

		@XmlAttribute
		@JsonProperty
		private long size;

		@XmlAttribute
		@JsonProperty
		private long totalElements;

		@XmlAttribute
		@JsonProperty
		private long totalPages;

		@XmlAttribute
		@JsonProperty
		private long number;

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
			return String.format("Page metadata [number: %d, total pages: %d, total elements: %d, size: %d]", number,
					totalPages, totalElements, size);
		}
	}
}
