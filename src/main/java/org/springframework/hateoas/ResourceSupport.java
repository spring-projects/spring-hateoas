/*
 * Copyright 2012-2017 the original author or authors.
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Base class for DTOs to collect links.
 * 
 * @author Oliver Gierke
 * @author Johhny Lim
 */
public class ResourceSupport implements Identifiable<Link> {

	private final List<Link> links;

	public ResourceSupport() {
		this.links = new ArrayList<>();
	}

	/**
	 * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
	 */
	@JsonIgnore
	public Optional<Link> getId() {
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

	/**
	 * Adds all given {@link Link}s to the resource.
	 * 
	 * @param links
	 */
	public void add(Iterable<Link> links) {
		Assert.notNull(links, "Given links must not be null!");
		links.forEach(this::add);
	}

	/**
	 * Adds all given {@link Link}s to the resource.
	 *
	 * @param links must not be {@literal null}.
	 */
	public void add(Link... links) {
		Assert.notNull(links, "Given links must not be null!");
		add(Arrays.asList(links));
	}

	/**
	 * Returns whether the resource contains {@link Link}s at all.
	 * 
	 * @return
	 */
	public boolean hasLinks() {
		return !this.links.isEmpty();
	}

	/**
	 * Returns whether the resource contains a {@link Link} with the given rel.
	 * 
	 * @param rel
	 * @return
	 */
	public boolean hasLink(String rel) {
		return getLink(rel).isPresent();
	}

	/**
	 * Returns all {@link Link}s contained in this resource.
	 * 
	 * @return
	 */
	@JsonProperty("links")
	public List<Link> getLinks() {
		return links;
	}

	/**
	 * Removes all {@link Link}s added to the resource so far.
	 */
	public void removeLinks() {
		this.links.clear();
	}

	/**
	 * Returns the link with the given rel.
	 * 
	 * @param rel
	 * @return the link with the given rel or {@link Optional#empty()} if none found.
	 */
	public Optional<Link> getLink(String rel) {

		return links.stream() //
				.filter(link -> link.hasRel(rel)) //
				.findFirst();
	}

	/**
	 * Returns the link with the given rel.
	 * 
	 * @param rel
	 * @return the link with the given rel.
	 * @throws IllegalArgumentException in case no link with the given rel can be found.
	 */
	public Link getRequiredLink(String rel) {

		return getLink(rel) //
				.orElseThrow(() -> new IllegalArgumentException(String.format("No link with rel %s found!", rel)));
	}

	/**
	 * Returns all {@link Link}s with the given relation type.
	 *
	 * @return the links in a {@link List}
	 */
	public List<Link> getLinks(String rel) {

		return links.stream() //
				.filter(link -> link.hasRel(rel)) //
				.collect(Collectors.toList());
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("links: %s", links.toString());
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

		if (obj == null || !obj.getClass().equals(this.getClass())) {
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
}
