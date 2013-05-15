/*
 * Copyright 2013 the original author or authors.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * Value object to represent a list of {@link Link}s.
 * 
 * @author Oliver Gierke
 */
public class Links implements Iterable<Link> {

	static final Links NO_LINKS = new Links(Collections.<Link> emptyList());

	private final List<Link> links;

	/**
	 * Creates a new {@link Links} instance from the given {@link Link}s.
	 * 
	 * @param links
	 */
	public Links(List<Link> links) {
		this.links = links == null ? Collections.<Link> emptyList() : Collections.unmodifiableList(links);
	}

	/**
	 * Creates a new {@link Links} instance from the given {@link Link}s.
	 * 
	 * @param links
	 */
	public Links(Link... links) {
		this(Arrays.asList(links));
	}

	/**
	 * Returns the {@link Link} with the given rel.
	 * 
	 * @param rel the relation type to lookup a link for.
	 * @return the {@link Link} with the given rel or {@literal null} if none found.
	 */
	public Link getLink(String rel) {

		for (Link link : links) {
			if (link.getRel().equals(rel)) {
				return link;
			}
		}

		return null;
	}

	/**
	 * Returns all {@link Links} with the given relation type.
	 * 
	 * @return the links
	 */
	public List<Link> getLinks(String rel) {

		List<Link> result = new ArrayList<Link>();

		for (Link link : links) {
			if (link.getRel().endsWith(rel)) {
				result.add(link);
			}
		}

		return result;
	}

	/**
	 * Returns whether the {@link Links} container contains a {@link Link} with the given rel.
	 * 
	 * @param rel
	 * @return
	 */
	public boolean hasLink(String rel) {
		return getLink(rel) != null;
	}

	/**
	 * Creates a {@link Links} instance from the given RFC5988-compatible link format.
	 * 
	 * @param source a comma separated list of {@link Link} representations.
	 * @return the {@link Links} represented by the given {@link String}.
	 */
	public static Links valueOf(String source) {

		if (!StringUtils.hasText(source)) {
			return NO_LINKS;
		}

		String[] elements = source.split(",");
		List<Link> links = new ArrayList<Link>(elements.length);

		for (String element : elements) {

			Link link = Link.valueOf(element);

			if (link != null) {
				links.add(link);
			}
		}

		return new Links(links);
	}

	/**
	 * Returns whether the {@link Links} containter is empty.
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return links.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return StringUtils.collectionToCommaDelimitedString(links);
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Link> iterator() {
		return links.iterator();
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {

		if (!(arg0 instanceof Links)) {
			return false;
		}

		Links that = (Links) arg0;

		return this.links.equals(that.links);
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = 17;
		result += 31 * links.hashCode();

		return result;
	}
}
