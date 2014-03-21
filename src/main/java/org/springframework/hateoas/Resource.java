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

import java.util.Arrays;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * A simple {@link Resource} wrapping a domain object and adding links to it.
 * 
 * @author Oliver Gierke
 */
@XmlRootElement
public class Resource<T> extends ResourceSupport {

	private final T content;

	/**
	 * Creates an empty {@link Resource}.
	 */
	Resource() {
		this.content = null;
	}

	/**
	 * Creates a new {@link Resource} with the given content and {@link Link}s (optional).
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link Resource}.
	 */
	public Resource(T content, Link... links) {
		this(content, Arrays.asList(links));
	}

	/**
	 * Creates a new {@link Resource} with the given content and {@link Link}s.
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link Resource}.
	 */
	public Resource(T content, Iterable<Link> links) {

		Assert.notNull(content, "Content must not be null!");
		Assert.isTrue(!(content instanceof Collection), "Content must not be a collection! Use Resources instead!");
		this.content = content;
		this.add(links);
	}

	/**
	 * Returns the underlying entity.
	 * 
	 * @return the content
	 */
	@JsonUnwrapped
	@XmlAnyElement
	public T getContent() {
		return content;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceSupport#toString()
	 */
	@Override
	public String toString() {
		return String.format("Resource { content: %s, %s }", getContent(), super.toString());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceSupport#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}

		Resource<?> that = (Resource<?>) obj;

		boolean contentEqual = this.content == null ? that.content == null : this.content.equals(that.content);
		return contentEqual ? super.equals(obj) : false;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceSupport#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = super.hashCode();
		result += content == null ? 0 : 17 * content.hashCode();
		return result;
	}
}
