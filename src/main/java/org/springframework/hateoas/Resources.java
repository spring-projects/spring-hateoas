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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.util.Assert;

/**
 * General helper to easily create a wrapper for a collection of entities.
 * 
 * @author Oliver Gierke
 */
@XmlRootElement(name = "entities")
public class Resources<T extends Resource<?>> extends ResourceSupport implements Iterable<T> {

	@XmlAnyElement
	@XmlElementWrapper
	@JsonProperty("content")
	private final Collection<T> content;

	/**
	 * Creates an empty {@link Resources} instance.
	 */
	protected Resources() {
		this(new ArrayList<T>());
	}

	/**
	 * Creates a {@link Resources} instance with the given content.
	 * 
	 * @param content must not be {@literal null}.
	 * @param links the links to be added to the {@link Resources}.
	 */
	public Resources(Collection<T> content, Link... links) {

		Assert.notNull(content);

		this.content = new ArrayList<T>();
		this.content.addAll(content);
		this.add(Arrays.asList(links));
	}

	/**
	 * Creates a new {@link Resources} instance by wrapping the given domain class instances into a {@link Resource}.
	 * 
	 * @param content must not be {@literal null}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Resource<S>, S> Resources<T> fromEntities(Collection<S> content) {

		Assert.notNull(content);
		ArrayList<T> resources = new ArrayList<T>();

		for (S element : content) {
			resources.add((T) new Resource<S>(element));
		}

		return new Resources<T>(resources);
	}

	/**
	 * Returns the underlying {@link Resource}s.
	 * 
	 * @return the content will never be {@literal null}.
	 */
	public Collection<T> getContent() {
		return content;
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
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}

		Resources<?> that = (Resources<?>) obj;

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
