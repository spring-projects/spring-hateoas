/*
 * Copyright 2012-2024 the original author or authors.
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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;

/**
 * Value object for links.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Jens Schauder
 * @author Viliam Durina
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(value = { "templated", "template" }, ignoreUnknown = true)
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

	private LinkRelation rel;
	private String href;
	private @Nullable String hreflang, media, title, type, deprecation, profile, name;
	private @JsonIgnore @Nullable UriTemplate template;
	private @JsonIgnore List<Affordance> affordances;

	/**
	 * Creates a new {@link Link} to the given URI with the given rel.
	 *
	 * @param href must not be {@literal null} or empty.
	 * @param rel must not be {@literal null} or empty.
	 */
	protected Link(String href, LinkRelation rel) {
		this(href, templateOrNull(href), rel, Collections.emptyList());
	}

	/**
	 * Creates a new Link from the given {@link UriTemplate} and rel.
	 *
	 * @param template must not be {@literal null}.
	 * @param rel must not be {@literal null} or empty.
	 */
	protected Link(UriTemplate template, LinkRelation rel) {
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

	private Link(LinkRelation rel, String href, @Nullable String hreflang, @Nullable String media, @Nullable String title,
			@Nullable String type, @Nullable String deprecation, @Nullable String profile, @Nullable String name,
			@Nullable UriTemplate template, List<Affordance> affordances) {

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
		return new Link(href, IanaLinkRelations.SELF);
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
		return new Link(href, LinkRelation.of(relation));
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
		return new Link(template, LinkRelation.of(relation));
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
	 * Creates a new {@link Link} with the given {@link Affordance}s.
	 *
	 * @param affordances must not be {@literal null}.
	 * @return will never be {@literal null}.
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

		UriTemplate template = this.template;

		return template == null ? Collections.emptyList() : template.getVariableNames();
	}

	/**
	 * Returns all {@link TemplateVariables} contained in the {@link Link}.
	 *
	 * @return
	 */
	@JsonIgnore
	public List<TemplateVariable> getVariables() {

		UriTemplate template = this.template;

		return template == null ? Collections.emptyList() : template.getVariables();
	}

	/**
	 * Returns whether or not the link is templated.
	 *
	 * @return
	 */
	public boolean isTemplated() {

		UriTemplate template = this.template;

		return template == null ? false : !template.getVariables().isEmpty();
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 *
	 * @param arguments
	 * @return
	 */
	@SuppressWarnings("null")
	public Link expand(Object... arguments) {

		UriTemplate template = this.template;

		return template == null ? this : of(template.expand(arguments).toString(), getRel());
	}

	/**
	 * Turns the current template into a {@link Link} by expanding it using the given parameters.
	 *
	 * @param arguments must not be {@literal null}.
	 * @return
	 */
	public Link expand(Map<String, ?> arguments) {

		UriTemplate template = this.template;

		return template == null ? this : of(template.expand(arguments).toString(), getRel());
	}

	/**
	 * Creates a new {@link Link} with the same href but given {@link LinkRelation}.
	 *
	 * @param relation must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public Link withRel(LinkRelation relation) {

		Assert.notNull(relation, "LinkRelation must not be null!");

		return new Link(relation, href, hreflang, media, title, type, deprecation, profile, name, template, affordances);
	}

	/**
	 * Creates a new {@link Link} with the same href but given {@link LinkRelation}.
	 *
	 * @param relation must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
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
	 * Internal method to parse and consume one link from input string.
	 *
	 * @param input The input string
	 * @param pos Position to start from. It must be a 1-element array. The element will be
	 *              mutated to point to the first non-consumed character (either ',' or the end of input).
	 * @return a non-null Link
	 */
	@NonNull
	static Link valueOfInt(@NonNull String input, @NonNull int[] pos) {
		assert pos.length == 1;
		int l = input.length();
		while (pos[0] < l && Character.isWhitespace(input.charAt(pos[0]))) {
			pos[0]++;
		}
		if (input.charAt(pos[0]) != '<') {
			throw new IllegalArgumentException("Expecting '<' at index " + pos[0]);
		}
		pos[0]++;
		int urlEnd = input.indexOf('>', pos[0]);
		if (urlEnd < 0) {
			throw new IllegalArgumentException("Missing closing '>' at index " + input.length());
		}
		String url = input.substring(pos[0], urlEnd);
		pos[0] = urlEnd + 1;

		// parse parameters
		Map<String, String> params = new HashMap<>();
		enum State { INITIAL, IN_KEY, BEFORE_VALUE, IN_VALUE };
		State state = State.INITIAL;
		StringBuilder
				key = new StringBuilder(),
				value = new StringBuilder();

		outer:
		while (pos[0] <= l) {
			boolean eoi = pos[0] == l; // EOI - end of input
			char ch = eoi ? 0 : input.charAt(pos[0]);
			switch (state) {
				// searching for the initial `;`
				case INITIAL:
					if (Character.isWhitespace(ch)) {
						pos[0]++;
					}
					else if (ch == ';') {
						state = State.IN_KEY;
						pos[0]++;
					}
					else {
						// if there's something else, it's the end of this link
						break outer;
					}
					break;

				// consuming the key up to `=`
				case IN_KEY:
					if (ch == '=') {
						state = State.BEFORE_VALUE;
					}
					// value isn't mandatory, so param separator, link separator, or end of input all create a new param
					else if (ch == ';' || ch == ',' || eoi) {
						if (!key.isEmpty()) {
							params.put(key.toString().trim(), "");
							key.setLength(0);
						}
					} else {
						key.append(ch);
					}
					pos[0]++;
					break;

				case BEFORE_VALUE:
					if (Character.isWhitespace(ch)) {
						pos[0]++;
					}
					else if (ch == '"' || ch == '\'') {
						consumeQuotedString(input, value, pos);
						params.putIfAbsent(key.toString().trim(), value.toString());
						key.setLength(0);
						value.setLength(0);
						state = State.INITIAL;
					} else {
						state = State.IN_VALUE;
					}
					break;

				case IN_VALUE:
					if (ch == ';' || ch == ',' || eoi) {
						params.putIfAbsent(key.toString().trim(), value.toString().trim());
						key.setLength(0);
						value.setLength(0);
						state = State.INITIAL;
					} else {
						value.append(ch);
						pos[0]++;
					}
					break;

				default:
					throw new AssertionError();
			}
		}

		String sRel = params.get("rel");
		if (!StringUtils.hasText(sRel)) {
			throw new IllegalArgumentException("Missing 'rel' attribute at index " + pos[0]);
		}
		LinkRelation rel = LinkRelation.of(sRel);
		String hrefLang = params.get("hreflang");
		String media = params.get("media");
		String title = params.get("title");
		String type = params.get("type");
		String deprecation = params.get("deprecation");
		String profile = params.get("profile");
		String name = params.get("name");

		return new Link(rel, url, hrefLang, media, title, type, deprecation, profile, name, templateOrNull(url),
				Collections.emptyList());
	}

	/**
	 * Consume a quoted string from `input`, adding its contents to `target`. The starting position should be at
	 * starting quote. After consuming, the ending position will be just after the last final quote.
	 */
	private static void consumeQuotedString(String input, StringBuilder target, int[] pos) {
		int l = input.length();
		char quotingChar = input.charAt(pos[0]);
		assert quotingChar == '"' || quotingChar == '\'';
		// skip quoting char
		pos[0]++;
		for (; pos[0] < l; pos[0]++) {
			char ch = input.charAt(pos[0]);
			if (ch == quotingChar) {
				pos[0]++; // consume the final quote
				return;
			}
			if (ch == '\\') {
				ch = input.charAt(++pos[0]);
			}
			target.append(ch);
		}
		throw new IllegalArgumentException("Missing final quote at index " + pos[0]);
	}

	/**
	 * Factory method to easily create {@link Link} instances from RFC-8288 compatible {@link String} representations of a
	 * link.
	 *
	 * @param element an RFC-8288 compatible representation of a link.
	 * @throws IllegalArgumentException if a {@link String} was given that does not adhere to RFC-8288.
	 * @throws IllegalArgumentException if no {@code rel} attribute could be found.
	 * @return The parsed link
	 * @deprecated Use {@link Links#parse(String)} instead. This method parses only the first link from a list of links.
	 */
	@Deprecated
	public static Link valueOf(String element) {
        return valueOfInt(element, new int[]{0});
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal href}.
	 *
	 * @param href can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withHref(String href) {

		return this.href == href ? this
				: new Link(this.rel, href, this.hreflang, this.media, this.title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal hreflang}.
	 *
	 * @param hreflang can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withHreflang(@Nullable String hreflang) {

		return this.hreflang == hreflang ? this
				: new Link(this.rel, this.href, hreflang, this.media, this.title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal media}.
	 *
	 * @param media can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withMedia(@Nullable String media) {

		return this.media == media ? this
				: new Link(this.rel, this.href, this.hreflang, media, this.title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal title}.
	 *
	 * @param title can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withTitle(@Nullable String title) {

		return this.title == title ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, title, this.type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal type}.
	 *
	 * @param type can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withType(@Nullable String type) {

		return this.type == type ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, this.title, type, this.deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal deprecation}.
	 *
	 * @param deprecation can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withDeprecation(@Nullable String deprecation) {

		return this.deprecation == deprecation ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, this.title, this.type, deprecation, this.profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal profile}.
	 *
	 * @param profile can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withProfile(@Nullable String profile) {

		return this.profile == profile ? this
				: new Link(this.rel, this.href, this.hreflang, this.media, this.title, this.type, this.deprecation, profile,
						this.name, this.template, this.affordances);
	}

	/**
	 * Create a new {@link Link} by copying all attributes and applying the new {@literal name}.
	 *
	 * @param name can be {@literal null}
	 * @return will never be {@literal null}.
	 */
	public Link withName(@Nullable String name) {

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

	@Nullable
	@JsonProperty
	public String getHreflang() {
		return this.hreflang;
	}

	@Nullable
	@JsonProperty
	public String getMedia() {
		return this.media;
	}

	@Nullable
	@JsonProperty
	public String getTitle() {
		return this.title;
	}

	@Nullable
	@JsonProperty
	public String getType() {
		return this.type;
	}

	@Nullable
	@JsonProperty
	public String getDeprecation() {
		return this.deprecation;
	}

	@Nullable
	@JsonProperty
	public String getProfile() {
		return this.profile;
	}

	@Nullable
	@JsonProperty
	public String getName() {
		return this.name;
	}

	@JsonProperty
	public UriTemplate getTemplate() {

		UriTemplate template = this.template;

		return template == null ? UriTemplate.of(href) : template;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object o) {

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
		StringBuilder result = new StringBuilder(64);
		result.append('<')
				// We only url-encode the `>`. We expect other special chars to already be escaped. `;` and `,` need not
				// be escaped within the URL
				.append(href.replace(">", "%3e"))
				.append(">;rel=");
		quoteParamValue(rel.value(), result);

		if (hreflang != null) {
			result.append(";hreflang=");
			quoteParamValue(hreflang, result);
		}

		if (media != null) {
			result.append(";media=");
			quoteParamValue(media, result);
		}

		if (title != null) {
			result.append(";title=");
			quoteParamValue(title, result);
		}

		if (type != null) {
			result.append(";type=");
			quoteParamValue(type, result);
		}

		if (deprecation != null) {
			result.append(";deprecation=");
			quoteParamValue(deprecation, result);
		}

		if (profile != null) {
			result.append(";profile=");
			quoteParamValue(profile, result);
		}

		if (name != null) {
			result.append(";name=");
			quoteParamValue(name, result);
		}

		return result.toString();
	}

	/**
	 * Quotes the given string `s` and appends the result to the `target`. This method appends the start quote, the
	 * escaped text, and the end quote.
	 *
	 * @param s Text to quote
	 * @param target StringBuilder to append to
	 */
	private void quoteParamValue(String s, StringBuilder target) {
		// we reserve extra 6 chars: two for the start and end quote, 2 is a reserve for potential escaped chars
		target.ensureCapacity(target.length() + s.length() + 2);
		target.append('"');
		for (int i = 0, l = s.length(); i < l; i++) {
			char ch = s.charAt(i);
			if (ch == '"' || ch == '\\') {
				target.append('\\');
			}
			target.append(ch);
		}
		target.append('"');
	}

	@Nullable
	private static UriTemplate templateOrNull(String href) {

		Assert.notNull(href, "Href must not be null!");

		return href.contains("{") ? UriTemplate.of(href) : null;
	}
}
