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
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.util.Assert;

/**
 * Base class for DTOs to collect links.
 * 
 * @author Oliver Gierke
 */
public class ResourceSupport implements Identifiable<Link> {

	@XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
	private List<Link> links;

	public ResourceSupport() {
		this.links = new ArrayList<Link>();
	}

	/**
	 * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
	 */
	public Link getId() {
		return getLink(Link.REL_SELF);
	}

	/**
	 * Adds the given link to the resource.
	 * 
	 * @param link
	 */
	public void add(Link link) {
		Assert.notNull(link, "Link must not be null!");
		this.links.add(link);
	}

	public void add(Iterable<Link> links) {
		Assert.notNull(links, "Given links must not be null!");
		for (Link candidate : links) {
			add(candidate);
		}
	}

	public boolean hasLinks() {
		return !this.links.isEmpty();
	}

	public boolean hasLink(String rel) {
		return getLink(rel) != null;
	}

	public List<Link> getLinks() {
		return Collections.unmodifiableList(links);
	}

	/**
	 * Returns the link with the given rel.
	 * 
	 * @param rel
	 * @return the link with the given rel or {@literal null} if none found.
	 */
	public Link getLink(String rel) {

		for (Link link : links) {
			if (link.getRel().equals(rel)) {
				return link;
			}
		}

		return null;
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

		if (!obj.getClass().equals(this.getClass())) {
			return false;
		}

		ResourceSupport that = (ResourceSupport) obj;

		return this.links.equals(that.links);
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return this.links.hashCode();
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.links.toString();
	}
}
