/*
 * Copyright 2012-2018 the original author or authors.
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

import java.util.Arrays;
import java.util.Collection;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * A simple {@link EntityModel} wrapping a domain object and adding links to it.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class EntityModel<T> extends RepresentationModel<EntityModel<T>> {

	private final T content;

	/**
	 * Creates an empty {@link EntityModel}.
	 */
	protected EntityModel() {
		this.content = null;
	}

	/**
	 * Creates a new {@link EntityModel} with the given content and {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link EntityModel}.
	 */
	public EntityModel(T content, Link... links) {
		this(content, Arrays.asList(links));
	}

	/**
	 * Creates a new {@link EntityModel} with the given content and {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link EntityModel}.
	 */
	public EntityModel(@Nullable T content, Iterable<Link> links) {

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
	@Nullable
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
	public boolean equals(@Nullable Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}

		EntityModel<?> that = (EntityModel<?>) obj;

		boolean contentEqual = this.content == null ? that.content == null : this.content.equals(that.content);
		return contentEqual && super.equals(obj);
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
