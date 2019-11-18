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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object for links.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Jens Schauder
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = { "templated", "template" }, ignoreUnknown = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter(onMethod = @__(@JsonProperty))
@EqualsAndHashCode(
		of = { "rel", "href", "hreflang", "media", "title", "type", "deprecation", "profile", "name", "affordances" })
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;
	private static final String URI_PATTERN = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	private static final Pattern URI_AND_ATTRIBUTES_PATTERN = Pattern.compile("<(.*)>;(.*)");
	private static final Pattern KEY_AND_VALUE_PATTERN = Pattern
			.compile("(\\w+)=\"(\\p{Lower}[\\p{Lower}\\p{Digit}.\\-\\s]*|" + URI_PATTERN + ")\"");

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

	/**
	 * @deprecated Use {@link IanaLinkRelations#SELF} instead.
	 */
	public static final @Deprecated LinkRelation REL_SELF = IanaLinkRelations.SELF;

	/**
	 * @deprecated Use {@link IanaLinkRelations#FIRST} instead.
	 */
	public static final @Deprecated LinkRelation REL_FIRST = IanaLinkRelations.FIRST;

	/**
	 * @deprecated Use {@link IanaLinkRelations#PREV} instead.
	 */
	public static final @Deprecated LinkRelation REL_PREVIOUS = IanaLinkRelations.PREV;

	/**
	 * @deprecated Use {@link IanaLinkRelations#NEXT} instead.
	 */
	public static final @Deprecated LinkRelation REL_NEXT = IanaLinkRelations.NEXT;

	/**
	 * @deprecated Use {@link IanaLinkRelations#LAST} instead.
	 */
	public static final @Deprecated LinkRelation REL_LAST = IanaLinkRelations.LAST;

	private LinkRelation rel;
	private @Wither String href;
	private @Wither String hreflang;
	private @Wither String media;
	private @Wither String title;
	private @Wither String type;
	private @Wither String deprecation;
	private @Wither String profile;
	private @Wither String name;
	private @JsonIgnore UriTemplate template;
	private @JsonIgnore List<Affordance> affordances;

	/**
	 * Creates a new link to the given URI with the self rel.
	 *
	 * @see IanaLinkRelations#SELF
	 * @param href must not be {@literal null} or empty.
	 * @deprecated since 1.1, use {@link #of(String)}
	 */
	@Deprecated
	public Link(String href) {
		this(href, IanaLinkRelations.SELF);
	}

	/**
	 * Creates a new {@link Link} to the given URI with the given rel.
	 *
	 * @param href must not be {@literal null} or empty.
	 * @param rel must not be {@literal null} or empty.
	 * @deprecated since 1.1, use {@link #of(String, String)}.
	 */
	@Deprecated
	public Link(String href, String rel) {
		this(UriTemplate.of(href), LinkRelation.of(rel));
	}

	/**
	 * Creates a new {@link Link} to the given URI with the given rel.
	 *
	 * @param href must not be {@literal null} or empty.
	 * @param rel must not be {@literal null} or empty.
	 * @deprecated since 1.1, use {@link #of(String, LinkRelation)}.
	 */
	@Deprecated
	public Link(String href, LinkRelation rel) {
		this(UriTemplate.of(href), rel);
	}

	/**
	 * Creates a new Link from the given {@link UriTemplate} and rel.
	 *
	 * @param template must not be {@literal null}.
	 * @param rel must not be {@literal null} or empty.
	 * @deprecated since 1.1, use {@link #of(UriTemplate, String)}.
	 */
	@Deprecated
	public Link(UriTemplate template, String rel) {
		this(template, LinkRelation.of(rel));
	}

	/**
	 * Creates a new Link from the given {@link UriTemplate} and rel.
	 *
	 * @param template must not be {@literal null}.
	 * @param rel must not be {@literal null} or empty.
	 * @deprecated since 1.1, use {@link #of(UriTemplate, LinkRelation)}.
	 */
	@Deprecated
	public Link(UriTemplate template, LinkRelation rel) {
		this(template, rel, Collections.emptyList());
	}

	/**
	 * Creates a new Link from the given {@link UriTemplate}, link relation and affordances.
	 *
	 * @param template must not be {@literal null}.
	 * @param rel must not be {@literal null} or empty.
	 */
	private Link(UriTemplate template, LinkRelation rel, List<Affordance> affordances) {

		Assert.notNull(template, "UriTemplate must not be null!");
		Assert.notNull(rel, "LinkRelation must not be null!");
		Assert.notNull(affordances, "Affordances must not be null!");

		this.template = template;
		this.rel = rel;
		this.href = template.toString();
		this.affordances = affordances;
	}

	/**
	 * Creates a new link to the given URI with the self relation.
	 *
	 * @see IanaLinkRelations#SELF
	 * @param href must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	public static Link of(String href) {
		return new Link(href);
	}

	/**
	 * Creates a new {@link Link} to the given href with the given relation.
	 *
	 * @param href must not be {@literal null} or empty.
	 * @param relation must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	@Deprecated
	public static Link of(String href, String relation) {
		return new Link(href, relation);
	}

	/**
	 * Creates a new {@link Link} to the given href and {@link LinkRelation}.
	 *
	 * @param href must not be {@literal null} or empty.
	 * @param relation must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static Link of(String href, LinkRelation relation) {
		return new Link(href, relation);
	}

	/**
	 * Creates a new {@link Link} to the given {@link UriTemplate} and link relation.
	 *
	 * @param template must not be {@literal null}.
	 * @param relation must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	public static Link of(UriTemplate template, String relation) {
		return new Link(template, relation);
	}

	/**
	 * Creates a new {@link Link} to the given {@link UriTemplate} and {@link LinkRelation}.
	 *
	 * @param template must not be {@literal null}.
	 * @param relation must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static Link of(UriTemplate template, LinkRelation relation) {
		return new Link(template, relation);
	}

	/**
	 * Empty constructor required by the marshaling framework.
	 */
	protected Link() {
		this.affordances = new ArrayList<>();
	}

	/**
	 * Returns safe copy of {@link Affordance}s.
	 *
	 * @return
	 */
	public List<Affordance> getAffordances() {
		return Collections.unmodifiableList(this.affordances);
	}

	/**
	 * Returns a {@link Link} pointing to the same URI but with the {@code self} relation.
	 *
	 * @return
	 */
	public Link withSelfRel() {
		return withRel(IanaLinkRelations.SELF);
	}

	/**
	 * Create new {@link Link} with an additional {@link Affordance}.
	 *
	 * @param affordance must not be {@literal null}.
	 * @return
	 */
	public Link andAffordance(Affordance affordance) {

		Assert.notNull(affordance, "Affordance must not be null!");

		List<Affordance> newAffordances = new ArrayList<>(this.affordances);
		newAffordances.add(affordance);

		return withAffordances(newAffordances);
	}

	/**
	 * Create new {@link Link} with additional {@link Affordance}s.
	 *
	 * @param affordances must not be {@literal null}.
	 * @return
	 */
	public Link andAffordances(List<Affordance> affordances) {

		List<Affordance> newAffordances = new ArrayList<>();
		newAffordances.addAll(this.affordances);
		newAffordances.addAll(affordances);

		return withAffordances(newAffordances);
	}

	/**
	 * Creats a new {@link Link} with the given {@link Affordance}s.
	 *
	 * @param affordances must not be {@literal null}.
	 * @return
	 */
	public Link withAffordances(List<Affordance> affordances) {

		return new Link(this.rel, this.href, this.hreflang, this.media, this.title, this.type, this.deprecation,
				this.profile, this.name, this.template, affordances);
	}

	/**
	 * Returns the variable names contained in the template.
	 *
	 * @return
	 */
	@JsonIgnore
	public List<String> getVariableNames() {
		return template.getVariableNames();
	}

	/**
	 * Returns all {@link TemplateVariables} contained in the {@link Link}.
	 *
	 * @return
	 */
	@JsonIgnore
	public List<TemplateVariable> getVariables() {
		return template.getVariables();
	}

	/**
	 * Returns whether or not the link is templated.
	 *
	 * @return
	 */
	public boolean isTemplated() {
		return !template.getVariables().isEmpty();
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 *
	 * @param arguments
	 * @return
	 */
	public Link expand(Object... arguments) {
		return of(template.expand(arguments).toString(), getRel());
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 *
	 * @param arguments must not be {@literal null}.
	 * @return
	 */
	public Link expand(Map<String, ?> arguments) {
		return of(template.expand(arguments).toString(), getRel());
	}

	/**
	 * Creates a new {@link Link} with the same href but given {@link LinkRelation}.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	public Link withRel(LinkRelation relation) {

		Assert.notNull(relation, "LinkRelation must not be null!");

		return new Link(relation, href, hreflang, media, title, type, deprecation, profile, name, template, affordances);
	}

	/**
	 * Creates a new {@link Link} with the same href but given {@link LinkRelation}.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return
	 */
	public Link withRel(String relation) {
		return withRel(LinkRelation.of(relation));
	}

	/**
	 * Returns whether the current {@link Link} has the given link relation.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	public boolean hasRel(String rel) {

		Assert.hasText(rel, "Link relation must not be null or empty!");

		return hasRel(LinkRelation.of(rel));
	}

	/**
	 * Returns whether the {@link Link} has the given {@link LinkRelation}.
	 *
	 * @param rel must not be {@literal null}.
	 * @return
	 */
	public boolean hasRel(LinkRelation rel) {

		Assert.notNull(rel, "Link relation must not be null!");

		return this.rel.isSameAs(rel);
	}

	/**
	 * Returns the current href as URI after expanding the links without any arguments, i.e. all optional URI
	 * {@link TemplateVariable}s will be dropped. If the href contains mandatory {@link TemplateVariable}s, the URI
	 * creation will fail with an {@link IllegalStateException}.
	 *
	 * @return will never be {@literal null}.
	 * @throws IllegalStateException in case the href contains mandatory URI {@link TemplateVariable}s.
	 */
	public URI toUri() {

		try {
			return URI.create(expand().getHref());
		} catch (IllegalArgumentException o_O) {
			throw new IllegalStateException(o_O);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String linkString = String.format("<%s>;rel=\"%s\"", href, rel.value());

		if (hreflang != null) {
			linkString += ";hreflang=\"" + hreflang + "\"";
		}

		if (media != null) {
			linkString += ";media=\"" + media + "\"";
		}

		if (title != null) {
			linkString += ";title=\"" + title + "\"";
		}

		if (type != null) {
			linkString += ";type=\"" + type + "\"";
		}

		if (deprecation != null) {
			linkString += ";deprecation=\"" + deprecation + "\"";
		}

		if (profile != null) {
			linkString += ";profile=\"" + profile + "\"";
		}

		if (name != null) {
			linkString += ";name=\"" + name + "\"";
		}

		return linkString;
	}

	/**
	 * Factory method to easily create {@link Link} instances from RFC-5988 compatible {@link String} representations of a
	 * link.
	 *
	 * @param element an RFC-5899 compatible representation of a link.
	 * @throws IllegalArgumentException if a {@link String} was given that does not adhere to RFC-5899.
	 * @throws IllegalArgumentException if no {@code rel} attribute could be found.
	 * @return
	 */
	public static Link valueOf(String element) {

		if (!StringUtils.hasText(element)) {
			throw new IllegalArgumentException(String.format("Given link header %s is not RFC5988 compliant!", element));
		}

		Matcher matcher = URI_AND_ATTRIBUTES_PATTERN.matcher(element);

		if (matcher.find()) {

			Map<String, String> attributes = getAttributeMap(matcher.group(2));

			if (!attributes.containsKey("rel")) {
				throw new IllegalArgumentException("Link does not provide a rel attribute!");
			}

			Link link = of(matcher.group(1), attributes.get("rel"));

			if (attributes.containsKey("hreflang")) {
				link = link.withHreflang(attributes.get("hreflang"));
			}

			if (attributes.containsKey("media")) {
				link = link.withMedia(attributes.get("media"));
			}

			if (attributes.containsKey("title")) {
				link = link.withTitle(attributes.get("title"));
			}

			if (attributes.containsKey("type")) {
				link = link.withType(attributes.get("type"));
			}

			if (attributes.containsKey("deprecation")) {
				link = link.withDeprecation(attributes.get("deprecation"));
			}

			if (attributes.containsKey("profile")) {
				link = link.withProfile(attributes.get("profile"));
			}

			if (attributes.containsKey("name")) {
				link = link.withName(attributes.get("name"));
			}

			return link;

		} else {
			throw new IllegalArgumentException(String.format("Given link header %s is not RFC5988 compliant!", element));
		}
	}

	/**
	 * Parses the links attributes from the given source {@link String}.
	 *
	 * @param source
	 * @return
	 */
	private static Map<String, String> getAttributeMap(String source) {

		if (!StringUtils.hasText(source)) {
			return Collections.emptyMap();
		}

		Map<String, String> attributes = new HashMap<>();
		Matcher matcher = KEY_AND_VALUE_PATTERN.matcher(source);

		while (matcher.find()) {
			attributes.put(matcher.group(1), matcher.group(2));
		}

		return attributes;
	}
}
