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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * General helper to easily create a wrapper for a collection of entities.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class AbstractCollectionModel<S extends AbstractCollectionModel<S,T>, T> extends RepresentationModel<S> implements Iterable<T> {

	private final Collection<T> content;

	public AbstractCollectionModel() {
		this(new ArrayList<>(), Collections.emptyList());
	}

	/**
	 * Creates a {@link AbstractCollectionModel} instance with the given content and {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link AbstractCollectionModel}.
	 */
	protected AbstractCollectionModel(Iterable<T> content, Iterable<Link> links) {

		Assert.notNull(content, "Content must not be null!");

		this.content = new ArrayList<>();

		for (T element : content) {
			this.content.add(element);
		}

		this.add(links);
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
	 * @see org.springframework.hateoas.ResourceSupport#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object obj) {

		if (obj == this) {
			return true;
		}

		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}

		if (!super.equals(obj)) {
			return false;
		}

		AbstractCollectionModel<?, ?> that = (AbstractCollectionModel<?,?>) obj;
		return Objects.equals(this.content, that.content);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.ResourceSupport#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result += 17 * Objects.hashCode(content);
		return result;
	}

}
