/*
 * Copyright 2012-2019 the original author or authors.
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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Value object for links.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Jens Schauder
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = "templated", ignoreUnknown = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@EqualsAndHashCode(of = { "rel", "href", "hreflang", "media", "title", "deprecation", "affordances" })
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;
	private static final String URI_PATTERN = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

	private static final Pattern URI_AND_ATTRIBUTES_PATTERN = Pattern.compile("<(.*)>;(.*)");
	private static final Pattern KEY_AND_VALUE_PATTERN = Pattern
			.compile("(\\w+)=\"(\\p{Lower}[\\p{Lower}\\p{Digit}.\\-\\s]*|" + URI_PATTERN + ")\"");

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

	/**
	 * @deprecated Use {@link IanaLinkRelation#SELF} instead.
	 */
	@Deprecated
	public static final String REL_SELF = IanaLinkRelation.SELF.value();

	/**
	 * @deprecated Use {@link IanaLinkRelation#FIRST} instead.
	 */
	@Deprecated
	public static final String REL_FIRST = IanaLinkRelation.FIRST.value();

	/**
	 * @deprecated Use {@link IanaLinkRelation#PREV} instead.
	 */
	@Deprecated
	public static final String REL_PREVIOUS = IanaLinkRelation.PREV.value();

	/**
	 * @deprecated Use {@link IanaLinkRelation#NEXT} instead.
	 */
	@Deprecated
	public static final String REL_NEXT = IanaLinkRelation.NEXT.value();

	/**
	 * @deprecated Use {@link IanaLinkRelation#LAST} instead.
	 */
	@Deprecated
	public static final String REL_LAST = IanaLinkRelation.LAST.value();

	private @Wither String rel;
	private @Wither String href;
	private @Wither String hreflang;
	private @Wither String media;
	private @Wither String title;
	private @Wither String type;
	private @Wither String deprecation;
	private @Wither String profile;
	private @JsonIgnore UriTemplate template;
	private @JsonIgnore List<Affordance> affordances;

	/**
	 * Creates a new link to the given URI with the self rel.
	 * 
	 * @see IanaLinkRelation#SELF
	 * @param href must not be {@literal null} or empty.
	 */
	public Link(String href) {
		this(href, IanaLinkRelation.SELF.value());
	}

	/**
	 * Creates a new {@link Link} to the given URI with the given rel.
	 * 
	 * @param href must not be {@literal null} or empty.
	 * @param rel must not be {@literal null} or empty.
	 */
	public Link(String href, String rel) {
		this(new UriTemplate(href), rel);
	}

	/**
	 * Creates a new Link from the given {@link UriTemplate} and rel.
	 * 
	 * @param template must not be {@literal null}.
	 * @param rel must not be {@literal null} or empty.
	 */
	public Link(UriTemplate template, String rel) {

		Assert.notNull(template, "UriTemplate must not be null!");
		Assert.hasText(rel, "Rel must not be null or empty!");

		this.template = template;
		this.href = template.toString();
		this.rel = rel;
		this.affordances = new ArrayList<>();
	}

	public Link(String href, String rel, List<Affordance> affordances) {

		this(href, rel);

		Assert.notNull(affordances, "affordances must not be null!");

		this.affordances = affordances;
	}

	/**
	 * Empty constructor required by the marshalling framework.
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
		return withRel(IanaLinkRelation.SELF.value());
	}

	/**
	 * Create new {@link Link} with an additional {@link Affordance}.
	 *
	 * @param affordance must not be {@literal null}.
	 * @return
	 */
	public Link andAffordance(Affordance affordance) {

		Assert.notNull(affordance, "Affordance must not be null!");

		List<Affordance> newAffordances = new ArrayList<>();
		newAffordances.addAll(this.affordances);
		newAffordances.add(affordance);

		return withAffordances(newAffordances);
	}

	/**
	 * Convenience method when chaining an existing {@link Link}.
	 * 
	 * @param name
	 * @param httpMethod
	 * @param inputType
	 * @param queryMethodParameters
	 * @param outputType
	 * @return
	 */
	public Link andAffordance(String name, HttpMethod httpMethod, ResolvableType inputType, List<QueryParameter> queryMethodParameters, ResolvableType outputType) {
		return andAffordance(new Affordance(name, this, httpMethod, inputType, queryMethodParameters, outputType));
	}

	/**
	 * Convenience method when chaining an existing {@link Link}. Defaults the name of the affordance to verb + classname, e.g.
	 * {@literal <httpMethod=HttpMethod.PUT, inputType=Employee>} produces {@literal <name=putEmployee>}.
	 *
	 * @param httpMethod
	 * @param inputType
	 * @param queryMethodParameters
	 * @param outputType
	 * @return
	 */
	public Link andAffordance(HttpMethod httpMethod, ResolvableType inputType, List<QueryParameter> queryMethodParameters, ResolvableType outputType) {
		return andAffordance(httpMethod.toString().toLowerCase() + inputType.resolve().getSimpleName(), httpMethod, inputType, queryMethodParameters, outputType);
	}

	/**
	 * Convenience method when chaining an existing {@link Link}. Defaults the name of the affordance to verb + classname, e.g.
	 * {@literal <httpMethod=HttpMethod.PUT, inputType=Employee>} produces {@literal <name=putEmployee>}.
	 *
	 * @param httpMethod
	 * @param inputType
	 * @param queryMethodParameters
	 * @param outputType
	 * @return
	 */
	public Link andAffordance(HttpMethod httpMethod, Class<?> inputType, List<QueryParameter> queryMethodParameters, Class<?> outputType) {
		return andAffordance(httpMethod, ResolvableType.forClass(inputType), queryMethodParameters, ResolvableType.forClass(outputType));
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
				this.profile, this.template, affordances);
	}

	/**
	 * Returns the variable names contained in the template.
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<String> getVariableNames() {
		return getUriTemplate().getVariableNames();
	}

	/**
	 * Returns all {@link TemplateVariables} contained in the {@link Link}.
	 * 
	 * @return
	 */
	@JsonIgnore
	public List<TemplateVariable> getVariables() {
		return getUriTemplate().getVariables();
	}

	/**
	 * Returns whether or not the link is templated.
	 * 
	 * @return
	 */
	public boolean isTemplated() {
		return !getUriTemplate().getVariables().isEmpty();
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 * 
	 * @param arguments
	 * @return
	 */
	public Link expand(Object... arguments) {
		return new Link(getUriTemplate().expand(arguments).toString(), getRel());
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 * 
	 * @param arguments must not be {@literal null}.
	 * @return
	 */
	public Link expand(Map<String, ? extends Object> arguments) {
		return new Link(getUriTemplate().expand(arguments).toString(), getRel());
	}

	/**
	 * Returns whether the current {@link Link} has the given link relation.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	public boolean hasRel(String rel) {

		Assert.hasText(rel, "Link relation must not be null or empty!");

		return this.rel.equals(rel);
	}

	private UriTemplate getUriTemplate() {

		if (template == null) {
			this.template = new UriTemplate(href);
		}

		return template;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String linkString = String.format("<%s>;rel=\"%s\"", href, rel);

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

		return linkString;
	}

	/**
	 * Factory method to easily create {@link Link} instances from RFC-5988 compatible {@link String} representations of a
	 * link. Will return {@literal null} if input {@link String} is either empty or {@literal null}.
	 * 
	 * @param element an RFC-5899 compatible representation of a link.
	 * @throws IllegalArgumentException if a non-empty {@link String} was given that does not adhere to RFC-5899.
	 * @throws IllegalArgumentException if no {@code rel} attribute could be found.
	 * @return
	 */
	public static Link valueOf(String element) {

		if (!StringUtils.hasText(element)) {
			return null;
		}

		Matcher matcher = URI_AND_ATTRIBUTES_PATTERN.matcher(element);

		if (matcher.find()) {

			Map<String, String> attributes = getAttributeMap(matcher.group(2));

			if (!attributes.containsKey("rel")) {
				throw new IllegalArgumentException("Link does not provide a rel attribute!");
			}

			Link link = new Link(matcher.group(1), attributes.get("rel"));

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
