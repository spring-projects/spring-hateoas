/*
 * Copyright 2012-2021 the original author or authors.
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

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.lang.Nullable;
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
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;
	private static final Pattern URI_AND_ATTRIBUTES_PATTERN = Pattern.compile("<(.*)>;(.*)");

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
	private String href;
	private String hreflang;
	private String media;
	private String title;
	private String type;
	private String deprecation;
	private String profile;
	private String name;
	private @JsonIgnore @Nullable UriTemplate template;
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
		this(href, LinkRelation.of(rel));
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
		this(href, templateOrNull(href), rel, Collections.emptyList());
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

	private Link(String href, @Nullable UriTemplate template, LinkRelation rel, List<Affordance> affordances) {

		Assert.hasText(href, "Href must not be null or empty!");
		Assert.notNull(rel, "LinkRelation must not be null!");
		Assert.notNull(affordances, "Affordances must not be null!");

		this.href = href;
		this.template = template;
		this.rel = rel;
		this.affordances = affordances;
	}

	private Link(LinkRelation rel, String href, String hreflang, String media, String title, String type,
			String deprecation, String profile, String name, @Nullable UriTemplate template, List<Affordance> affordances) {

		this.rel = rel;
		this.href = href;
		this.hreflang = hreflang;
		this.media = media;
		this.title = title;
		this.type = type;
		this.deprecation = deprecation;
		this.profile = profile;
		this.name = name;
		this.template = template;
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
		return template == null ? Collections.emptyList() : template.getVariableNames();
	}

	/**
	 * Returns all {@link TemplateVariables} contained in the {@link Link}.
	 *
	 * @return
	 */
	@JsonIgnore
	public List<TemplateVariable> getVariables() {
		return template == null ? Collections.emptyList() : template.getVariables();
	}

	/**
	 * Returns whether or not the link is templated.
	 *
	 * @return
	 */
	public boolean isTemplated() {
		return template == null ? false : !template.getVariables().isEmpty();
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 *
	 * @param arguments
	 * @return
	 */
	public Link expand(Object... arguments) {
		return template == null ? this : of(template.expand(arguments).toString(), getRel());
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 *
	 * @param arguments must not be {@literal null}.
	 * @return
	 */
	public Link expand(Map<String, ?> arguments) {
		return template == null ? this : of(template.expand(arguments).toString(), getRel());
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

	/**
	 * Factory method to easily create {@link Link} instances from RFC-8288 compatible {@link String} representations of a
	 * link.
	 *
	 * @param element an RFC-8288 compatible representation of a link.
	 * @throws IllegalArgumentException if a {@link String} was given that does not adhere to RFC-8288.
	 * @throws IllegalArgumentException if no {@code rel} attribute could be found.
	 * @return
	 */
	public static Link valueOf(String element) {

		if (!StringUtils.hasText(element)) {
			throw new IllegalArgumentException(String.format("Given link header %s is not RFC-8288 compliant!", element));
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
			throw new IllegalArgumentException(String.format("Given link header %s is not RFC-8288 compliant!", element));
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

		String[] parts = source.split(";");
		Map<String, String> attributes = new HashMap<>();

		for (String part : parts) {

			int delimiter = part.indexOf('=');

			String key = part.substring(0, delimiter).trim();
			String value = part.substring(delimiter + 1).trim();

			// Potentially unquote value
			value = value.startsWith("\"") ? value.substring(1, value.length() - 1) : value;

			attributes.put(key, value);
		}

		return attributes;
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal href}.
	 *
	 * @param href
	 * @return
	 */
	public Link withHref(String href) {

		return this.href == href ? this
				: new Link(this.rel, href, this.hreflang, this.media, this.title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal hrefleng}.
	 *
	 * @param hreflang
	 * @return
	 */
	public Link withHreflang(String hreflang) {

		return this.hreflang == hreflang ? this
				: new Link(this.rel, this.href, hreflang, this.media, this.title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal media}.
	 *
	 * @param media
	 * @return
	 */
	public Link withMedia(String media) {

		return this.media == media ? this
				: new Link(this.rel, this.href, this.hreflang, media, this.title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal title}.
	 *
	 * @param title
	 * @return
	 */
	public Link withTitle(String title) {

		return this.title == title ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal type}.
	 *
	 * @param type
	 * @return
	 */
	public Link withType(String type) {

		return this.type == type ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, this.title, type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal deprecation}.
	 *
	 * @param deprecation
	 * @return
	 */
	public Link withDeprecation(String deprecation) {

		return this.deprecation == deprecation ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, this.title, this.type, deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal profile}.
	 *
	 * @param profile
	 * @return
	 */
	public Link withProfile(String profile) {

		return this.profile == profile ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, this.title, this.type, this.deprecation, profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal name}.
	 *
	 * @param name
	 * @return
	 */
	public Link withName(String name) {

		return this.name == name ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, this.title, this.type, this.deprecation,
						this.profile, name, this.template, this.affordances);
	}

	@JsonProperty
	public LinkRelation getRel() {
		return this.rel;
	}

	@JsonProperty
	public String getHref() {
		return this.href;
	}

	@JsonProperty
	public String getHreflang() {
		return this.hreflang;
	}

	@JsonProperty
	public String getMedia() {
		return this.media;
	}

	@JsonProperty
	public String getTitle() {
		return this.title;
	}

	@JsonProperty
	public String getType() {
		return this.type;
	}

	@JsonProperty
	public String getDeprecation() {
		return this.deprecation;
	}

	@JsonProperty
	public String getProfile() {
		return this.profile;
	}

	@JsonProperty
	public String getName() {
		return this.name;
	}

	@JsonProperty
	public UriTemplate getTemplate() {
		return template == null ? UriTemplate.of(href) : this.template;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Link link = (Link) o;
		return Objects.equals(this.rel, link.rel) && Objects.equals(this.href, link.href)
				&& Objects.equals(this.hreflang, link.hreflang) && Objects.equals(this.media, link.media)
				&& Objects.equals(this.title, link.title) && Objects.equals(this.type, link.type)
				&& Objects.equals(this.deprecation, link.deprecation) && Objects.equals(this.profile, link.profile)
				&& Objects.equals(this.name, link.name) && Objects.equals(this.affordances, link.affordances);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return Objects.hash(this.rel, this.href, this.hreflang, this.media, this.title, this.type, this.deprecation,
				this.profile, this.name, this.affordances);
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

	@Nullable
	private static UriTemplate templateOrNull(String href) {

		Assert.notNull(href, "Href must not be null!");

		return href.contains("{") ? UriTemplate.of(href) : null;
	}
}
