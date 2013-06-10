/*
 * Copyright 2012-2013 the original author or authors.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Value object for links.
 * 
 * @author Oliver Gierke
 */
@XmlType(name = "link", namespace = Link.ATOM_NAMESPACE)
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;
	private static final String ATOM_LINK_SPEC_URL = "http://tools.ietf.org/html/rfc4287#section-4.2.7";
	private static Pattern ATOM_MEDIA_TYPE_PATTERN = Pattern.compile(".+/.+");
	// private static final String ATOM_LINK_HREF = "href";
	private static final String ATOM_LINK_REL = "rel";
	private static final String ATOM_LINK_TITLE = "title";
	private static final String ATOM_LINK_TYPE = "type";

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	public static final String REL_SELF = "self";
	public static final String REL_FIRST = "first";
	public static final String REL_PREVIOUS = "prev";
	public static final String REL_NEXT = "next";
	public static final String REL_LAST = "last";

	@XmlAttribute
	private String rel;
	@XmlAttribute
	private String href;
	@XmlAttribute
	private String title;
	@XmlAttribute
	private String type;

	/**
	 * Creates a new link to the given URI with the self rel.
	 * 
	 * @see #REL_SELF
	 * @param href
	 *            must not be {@literal null} or empty.
	 */
	public Link(String href) {
		this(href, REL_SELF);
	}

	/**
	 * Creates a new {@link Link} to the given URI with the given rel.
	 * 
	 * @param href
	 *            must not be {@literal null} or empty.
	 * @param rel
	 *            must not be {@literal null} or empty.
	 */
	public Link(String href, String rel) {
		this(href, rel, null, null);
	}

	/**
	 * Creates a new {@link Link} to the given URI with the given rel.
	 * 
	 * @param href
	 *            must not be {@literal null} or empty.
	 * @param rel
	 *            must not be {@literal null} or empty.
	 * @param title
	 *            may be {@literal null} or empty.
	 * @param type
	 *            may be {@literal null} or empty.
	 */
	public Link(String href, String rel, String title, String type) {

		Assert.hasText(href, "Href must not be null or empty!");
		Assert.hasText(rel, "Rel must not be null or empty!");
		Assert.isTrue((title == null) || StringUtils.hasText(title), "Title must not be empty!");
		Assert.isTrue((type == null) || isAtomMediaType(type), "Type must be valid atom media type! (see " + ATOM_LINK_SPEC_URL + ")");

		this.href = href;
		this.rel = rel;
		this.title = title;
		this.type = type;
	}

	/**
	 * returns check whether passed string is valid atom media type per
	 * {@value #ATOM_LINK_SPEC_URL}
	 * 
	 * @param type
	 * @return
	 */
	private boolean isAtomMediaType(String type) {
		return ATOM_MEDIA_TYPE_PATTERN.matcher(type).matches();
	}

	/**
	 * Empty constructor required by the marshalling framework.
	 */
	protected Link() {

	}

	/**
	 * Returns the actual URI the link is pointing to.
	 * 
	 * @return
	 */
	public String getHref() {
		return href;
	}

	/**
	 * Returns the rel of the link.
	 * 
	 * @return
	 */
	public String getRel() {
		return rel;
	}

	/**
	 * Returns the title of the link (may be null)
	 * 
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the type of the link (may be null)
	 * 
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns a {@link Link} pointing to the same URI but with the given
	 * relation.
	 * 
	 * @param rel
	 *            must not be {@literal null} or empty.
	 * @return
	 */
	public Link withRel(String rel) {
		return new Link(href, rel);
	}

	/**
	 * Returns a {@link Link} pointing to the same URI but with the {@code self}
	 * relation.
	 * 
	 * @return
	 */
	public Link withSelfRel() {
		return withRel(Link.REL_SELF);
	}

	/**
	 * Returns a {@link Link} based on current Link, but with given title
	 * 
	 * @param title
	 *            may be {@literal null} or non-empty.
	 * @return
	 */
	public Link withTitle(String title) {
		return new Link(href, rel, title, type);
	}

	/**
	 * Returns a {@link Link} based on current Link, but with given title
	 * 
	 * @param type
	 *            may be {@literal null} or valid atom media type per
	 *            {@value #ATOM_LINK_SPEC_URL}
	 * @return
	 */
	public Link withType(String type) {
		return new Link(href, rel, title, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Link)) {
			return false;
		}

		Link that = (Link) obj;

		return this.href.equals(that.href) && this.rel.equals(that.rel) && ObjectUtils.nullSafeEquals(this.title, that.title)
				&& ObjectUtils.nullSafeEquals(this.type, that.type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		
		int result = 17;
		result += 31 * href.hashCode();
		result += 31 * rel.hashCode();
		result += 31 * ObjectUtils.nullSafeHashCode(title);
		result += 31 * ObjectUtils.nullSafeHashCode(type);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = String.format("<%s>;%s=\"%s\"", href, ATOM_LINK_REL, rel);
		if (title != null) {
			result += String.format("%s;%s=\"%s\"", result, ATOM_LINK_TITLE, title);
		}
		if (type != null) {
			result += String.format("%s;%s=\"%s\"", result, ATOM_LINK_TYPE, type);
		}
		return result;
	}

	/**
	 * Factory method to easily create {@link Link} instances from RFC-5988
	 * compatible {@link String} representations of a link. Will return
	 * {@literal null} if an empty or {@literal null} {@link String} is given.
	 * 
	 * @param element
	 *            an RFC-5988 compatible representation of a link.
	 * @throws IllegalArgumentException
	 *             if a non-empty {@link String} was given that does not adhere
	 *             to RFC-5988.
	 * @throws IllegalArgumentException
	 *             if no {@code rel} attribute could be found.
	 * @return
	 */
	public static Link valueOf(String element) {

		if (!StringUtils.hasText(element)) {
			return null;
		}

		Pattern uriAndAttributes = Pattern.compile("<(.*)>;(.*)");
		Matcher matcher = uriAndAttributes.matcher(element);

		if (matcher.find()) {

			Map<String, String> attributes = getAttributeMap(matcher.group(2));

			if (!attributes.containsKey(ATOM_LINK_REL)) {
				throw new IllegalArgumentException("Link does not provide a rel attribute!");
			}

			return new Link(matcher.group(1), attributes.get(ATOM_LINK_REL), attributes.get(ATOM_LINK_TITLE), attributes.get(ATOM_LINK_TYPE));

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

		Map<String, String> attributes = new HashMap<String, String>();
		Pattern keyAndValue = Pattern.compile("(\\w+)=\\\"(\\p{Print}*)\"");
		String[] keyAndValues = source.split(";");
		for (int i = 0; i < keyAndValues.length; i++) {

			Matcher matcher = keyAndValue.matcher(keyAndValues[i]);

			if (matcher.find()) {
				attributes.put(matcher.group(1), matcher.group(2));
			} else {
				throw new RuntimeException(String.format("unexpected token found parsing link attributes [%s]", keyAndValues[i]));
			}
		}

		return attributes;
	}
}
