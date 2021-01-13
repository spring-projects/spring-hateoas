/*
 * Copyright 2013-2021 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Value object to represent a list of {@link Link}s.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class Links implements Iterable<Link> {

	public static final Links NONE = new Links(Collections.emptyList());
	private static final Pattern LINK_HEADER_PATTERN = Pattern.compile("(<[^>]*>(;\\s*\\w+=\"[^\"]*\")+)");

	private final List<Link> links;

	private Links(Iterable<Link> links) {

		Assert.notNull(links, "Links must not be null!");

		this.links = StreamSupport.stream(links.spliterator(), false) //
				.collect(Collectors.toList());
	}

	private Links(Link... links) {
		this(Arrays.asList(links));
	}

	/**
	 * Creates a new {@link Links} instance from the given {@link Link}s.
	 *
	 * @param links
	 */
	public static Links of(Link... links) {
		return new Links(links);
	}

	/**
	 * Creates a new {@link Links} instance from the given {@link Link}s.
	 *
	 * @param links
	 */
	public static Links of(Iterable<Link> links) {
		return Links.class.isInstance(links) ? Links.class.cast(links) : new Links(links);
	}

	/**
	 * Creates a {@link Links} instance from the given RFC-8288-compatible link format.
	 *
	 * @param source a comma separated list of {@link Link} representations.
	 * @return the {@link Links} represented by the given {@link String}.
	 * @deprecated use {@link #parse(String)} instead
	 */
	@Deprecated
	public static Links valueOf(String source) {
		return parse(source);
	}

	/**
	 * Creates a {@link Links} instance from the given RFC-8288-compatible link format.
	 *
	 * @param source a comma separated list of {@link Link} representations.
	 * @return the {@link Links} represented by the given {@link String}.
	 */
	public static Links parse(@Nullable String source) {

		if (!StringUtils.hasText(source)) {
			return NONE;
		}

		Matcher matcher = LINK_HEADER_PATTERN.matcher(source);
		List<Link> links = new ArrayList<>();

		while (matcher.find()) {

			Link link = Link.valueOf(matcher.group());

			if (link != null) {
				links.add(link);
			}
		}

		return new Links(links);
	}

	/**
	 * Creates a new {@link Links} instance with all given {@link Link}s added. For conditional adding see
	 * {@link #merge(Link...)}.
	 *
	 * @param links must not be {@literal null}.
	 * @return
	 * @see #merge(Link...)
	 * @see #merge(MergeMode, Link...)
	 */
	public Links and(Link... links) {

		Assert.notNull(links, "Links must not be null!");

		return and(Arrays.asList(links));
	}

	/**
	 * Adds the given links if the given condition is {@literal true}. The given {@link Link}s will only be resolved if
	 * the given condition is {@literal true}. Essentially syntactic sugar to write:<br />
	 * <code>
	 * if (a > 3) {
	 *   links = links.and(…);
	 * }
	 * </code> as <code>
	 * links = link.andIf(a > 3, …);
	 * </code>
	 *
	 * @param condition
	 * @param links must not be {@literal null}.
	 * @return
	 */
	@SafeVarargs
	public final Links andIf(boolean condition, Link... links) {
		return condition ? and(links) : this;
	}

	/**
	 * Adds the given links if the given condition is {@literal true}. The given {@link Supplier}s will only be resolved
	 * if the given condition is {@literal true}. Essentially syntactic sugar to write:<br />
	 * <code>
	 * if (a > 3) {
	 *   links = links.and(…);
	 * }
	 * </code> as <code>
	 * links = link.andIf(a > 3, …);
	 * </code>
	 *
	 * @param condition
	 * @param links must not be {@literal null}.
	 * @return
	 */
	@SafeVarargs
	public final Links andIf(boolean condition, Supplier<Link>... links) {

		Assert.notNull(links, "Links must not be null!");

		return andIf(condition, Stream.of(links).map(Supplier::get));
	}

	/**
	 * Adds the given links if the given condition is {@literal true}. The given {@link Stream} will only be resolved if
	 * the given condition is {@literal true}. Essentially syntactic sugar to write:<br />
	 * <code>
	 * if (a > 3) {
	 *   links = links.and(…);
	 * }
	 * </code> as <code>
	 * links = link.andIf(a > 3, …);
	 * </code>
	 *
	 * @param condition
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public final Links andIf(boolean condition, Stream<Link> links) {

		Assert.notNull(links, "Links must not be null!");

		return condition ? and(links.collect(Collectors.toList())) : this;
	}

	/**
	 * Creates a new {@link Links} instance with all given {@link Link}s added. For conditional adding see
	 * {@link #merge(Iterable)}.
	 *
	 * @param links must not be {@literal null}.
	 * @return
	 * @see #merge(Iterable)
	 * @see #merge(MergeMode, Iterable)
	 */
	public Links and(Iterable<Link> links) {

		List<Link> newLinks = new ArrayList<>(this.links);
		links.forEach(newLinks::add);

		return Links.of(newLinks);
	}

	/**
	 * Creates a new {@link Links} instance with all given {@link Link}s added. For conditional adding see
	 * {@link #merge(Iterable)}.
	 *
	 * @param links must not be {@literal null}.
	 * @return
	 * @see #merge(Iterable)
	 * @see #merge(MergeMode, Iterable)
	 */
	public Links and(Stream<Link> links) {
		return and(links.collect(Collectors.toList()));
	}

	/**
	 * Merges the current {@link Links} with the given ones, skipping {@link Link}s already contained in the current
	 * instance. For unconditional combination see {@link #and(Link...)}.
	 *
	 * @param links the {@link Link}s to be merged, must not be {@literal null}.
	 * @return
	 * @see MergeMode#SKIP_BY_EQUALITY
	 * @see #and(Link...)
	 */
	public Links merge(Link... links) {
		return merge(Arrays.asList(links));
	}

	/**
	 * Merges the current {@link Links} with the given ones, skipping {@link Link}s already contained in the current
	 * instance. For unconditional combination see {@link #and(Stream)}.
	 *
	 * @param links the {@link Link}s to be merged, must not be {@literal null}.
	 * @return
	 * @see MergeMode#SKIP_BY_EQUALITY
	 * @see #and(Stream)
	 */
	public Links merge(Stream<Link> links) {
		return merge(links.collect(Collectors.toList()));
	}

	/**
	 * Merges the current {@link Links} with the given ones, skipping {@link Link}s already contained in the current
	 * instance. For unconditional combination see {@link #and(Iterable)}.
	 *
	 * @param links the {@link Link}s to be merged, must not be {@literal null}.
	 * @return
	 * @see MergeMode#SKIP_BY_EQUALITY
	 * @see #and(Iterable)
	 */
	public Links merge(Iterable<Link> links) {
		return merge(MergeMode.SKIP_BY_EQUALITY, links);
	}

	/**
	 * Merges the current {@link Links} with the given ones applying the given {@link MergeMode}.
	 *
	 * @param mode must not be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public Links merge(MergeMode mode, Link... links) {
		return merge(mode, Arrays.asList(links));
	}

	/**
	 * Merges the current {@link Links} with the given ones applying the given {@link MergeMode}.
	 *
	 * @param mode must not be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public Links merge(MergeMode mode, Stream<Link> links) {
		return merge(mode, links.collect(Collectors.toList()));
	}

	/**
	 * Merges the current {@link Links} with the given ones applying the given {@link MergeMode}.
	 *
	 * @param mode must not be {@literal null}.
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public Links merge(MergeMode mode, Iterable<Link> links) {

		Assert.notNull(mode, "MergeMode must not be null!");
		Assert.notNull(links, "Links must not be null!");

		List<Link> newLinks = MergeMode.REPLACE_BY_REL.equals(mode) //
				? allWithoutRels(links)
				: new ArrayList<>(this.links);

		links.forEach(it -> {

			if (MergeMode.REPLACE_BY_REL.equals(mode)) {
				newLinks.add(it);
			}

			if (MergeMode.SKIP_BY_EQUALITY.equals(mode) && !this.links.contains(it)) {
				newLinks.add(it);
			}

			if (MergeMode.SKIP_BY_REL.equals(mode) && !this.hasLink(it.getRel())) {
				newLinks.add(it);
			}
		});

		return new Links(newLinks);
	}

	/**
	 * Returns a {@link Links} with all {@link Link}s with the given {@link LinkRelation} removed.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	public Links without(LinkRelation relation) {

		Assert.notNull(relation, "LinkRelation must not be null!");

		return this.links.stream() //
				.filter(it -> !it.hasRel(relation)) //
				.collect(Links.collector());
	}

	/**
	 * Returns a {@link Link} with the given relation if contained in the current {@link Links} instance,
	 * {@link Optional#empty()} otherwise.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return
	 */
	public Optional<Link> getLink(String relation) {
		return getLink(LinkRelation.of(relation));
	}

	/**
	 * Returns the {@link Link} with the given rel.
	 *
	 * @param rel the relation type to lookup a link for.
	 * @return the link with the given rel or {@literal Optional#empty()} if none found.
	 */
	public Optional<Link> getLink(LinkRelation rel) {

		return links.stream() //
				.filter(it -> it.hasRel(rel)) //
				.findFirst();
	}

	/**
	 * Returns the {@link Link} with the given relation.
	 *
	 * @param rel the relation type to lookup a link for.
	 * @return
	 * @throws IllegalArgumentException if no link with the given relation was present.
	 * @since 1.0
	 */
	public Link getRequiredLink(String rel) {
		return getRequiredLink(LinkRelation.of(rel));
	}

	/**
	 * Returns the {@link Link} with the given relation.
	 *
	 * @param relation the relation type to lookup a link for.
	 * @return
	 * @throws IllegalArgumentException if no link with the given relation was present.
	 */
	public Link getRequiredLink(LinkRelation relation) {

		return getLink(relation) //
				.orElseThrow(() -> new IllegalArgumentException(String.format("Couldn't find link with rel '%s'!", relation)));
	}

	/**
	 * Returns whether the {@link Links} container contains a {@link Link} with the given relation.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return
	 */
	public boolean hasLink(String relation) {
		return getLink(relation).isPresent();
	}

	/**
	 * Returns whether the current {@link Links} contains a {@link Link} with the given relation.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	public boolean hasLink(LinkRelation relation) {
		return getLink(relation).isPresent();
	}

	/**
	 * Returns whether the {@link Links} container is empty.
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return links.isEmpty();
	}

	/**
	 * Returns whether the current {@link Links} has the given size.
	 *
	 * @param size
	 * @return
	 */
	public boolean hasSize(long size) {
		return links.size() == size;
	}

	/**
	 * Returns whether the {@link Links} contain a single {@link Link}.
	 *
	 * @return
	 */
	public boolean hasSingleLink() {
		return hasSize(1);
	}

	/**
	 * Creates a {@link Stream} of the current {@link Links}.
	 *
	 * @return
	 */
	public Stream<Link> stream() {
		return this.links.stream();
	}

	/**
	 * Returns the current {@link Links} as {@link List}.
	 *
	 * @return
	 */
	@JsonValue
	public List<Link> toList() {
		return this.links;
	}

	/**
	 * Returns whether the current {@link Links} contain all given {@link Link}s (but potentially others).
	 *
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public boolean contains(Link... links) {
		return this.links.containsAll(Links.of(links).toList());
	}

	/**
	 * Returns whether the current {@link Links} contain all given {@link Link}s (but potentially others).
	 *
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public boolean contains(Iterable<Link> links) {
		return this.links.containsAll(Links.of(links).toList());
	}

	/**
	 * Returns whether the current {@link Links} instance contains exactly the same {@link Link} as the given one.
	 *
	 * @param links must not be {@literal null}.
	 * @return
	 */
	public boolean containsSameLinksAs(Iterable<Link> links) {

		Links other = Links.of(links);

		return this.links.size() != other.links.size() ? false : this.links.containsAll(other.links);
	}

	/**
	 * Creates a new {@link Collector} to collect a {@link Stream} of {@link Link}s into a {@link Links} instance.
	 *
	 * @return will never be {@literal null}.
	 */
	public static Collector<Link, ?, Links> collector() {
		return Collectors.collectingAndThen(Collectors.toList(), Links::of);
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
	public boolean equals(@Nullable Object obj) {

		if (!(obj instanceof Links)) {
			return false;
		}

		Links that = (Links) obj;

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

	private List<Link> allWithoutRels(Iterable<Link> links) {

		Set<LinkRelation> toFilter = StreamSupport.stream(links.spliterator(), false) //
				.map(Link::getRel) //
				.collect(Collectors.toSet());

		return this.links.stream() //
				.filter(it -> !toFilter.contains(it.getRel())) //
				.collect(Collectors.toList());
	}

	/**
	 * The mode how to merge two {@link Links} instances.
	 *
	 * @author Oliver Drotbohm
	 */
	public enum MergeMode {

		/**
		 * Skips to add the same links on merge. Multiple links with the same link relation might appear.
		 */
		SKIP_BY_EQUALITY,

		/**
		 * Skips to add links with the same link relation, i.e. existing ones with the same relation are preferred.
		 */
		SKIP_BY_REL,

		/**
		 * Replaces existing links with the same link relation.
		 */
		REPLACE_BY_REL
	}
}
