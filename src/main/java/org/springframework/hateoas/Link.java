/*
 * Copyright 2012-2014 the original author or authors.
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Value object for links.
 * 
 * @author Oliver Gierke
 */
@XmlType(name = "link", namespace = Link.ATOM_NAMESPACE)
@JsonIgnoreProperties("templated")
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

	public static final String REL_SELF = "self";
	public static final String REL_FIRST = "first";
	public static final String REL_PREVIOUS = "prev";
	public static final String REL_NEXT = "next";
	public static final String REL_LAST = "last";

	@XmlAttribute private String rel;
	 @XmlAttribute private String href;
    @XmlAttribute private String title;
    @XmlAttribute private String name;
    @XmlAttribute private String type;
    @XmlAttribute private String deprecation;
    @XmlAttribute private String profile;
    @XmlAttribute private String hreflang;
	@XmlTransient @JsonIgnore private UriTemplate template;

	/**
	 * Creates a new link to the given URI with the self rel.
	 * 
	 * @see #REL_SELF
	 * @param href must not be {@literal null} or empty.
	 */
	public Link(String href) {
		this(href, REL_SELF);
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

		Assert.notNull(template, "UriTempalte must not be null!");
		Assert.hasText(rel, "Rel must not be null or empty!");

		this.template = template;
		this.href = template.toString();
		this.rel = rel;
	}

	/**
	 * Empty constructor required by the marshalling framework.
	 */
	protected Link() {

	}

    /**
     * Copy constructor for use by the additional with methods.
     * @param copyLink The link to copy.
     */
    private Link(Link copyLink) {
        rel = copyLink.rel;
        href = copyLink.href;
        template = copyLink.template;
        name = copyLink.name;
        title = copyLink.title;
        type = copyLink.type;
        deprecation = copyLink.deprecation;
        profile = copyLink.profile;
        hreflang = copyLink.hreflang;
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
	 * Returns a {@link Link} pointing to the same URI but with the given relation.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	public Link withRel(String rel) {
		return new Link(href, rel);
	}

	/**
	 * Returns a {@link Link} pointing to the same URI but with the {@code self} relation.
	 * 
	 * @return
	 */
	public Link withSelfRel() {
		return withRel(Link.REL_SELF);
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
	 * Returns whether the link is templated.
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

	private UriTemplate getUriTemplate() {

		if (template == null) {
			this.template = new UriTemplate(href);
		}

		return template;
	}

    /**
     * Returns the title of the link.
     *
     * @return
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getTitle() {
        return title;
    }

    /**
     * Returns the name of the link.
     *
     * @return
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the link.
     *
     * @return
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getType() {

        return type;
    }

    /**
     * Returns the deprecation of the link.
     *
     * @return
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getDeprecation() {

        return deprecation;
    }

    /**
     * Returns the profile of the link.
     *
     * @return
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getProfile() {

        return profile;
    }

    /**
     * Returns the hreflang of the link.
     *
     * @return
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getHreflang() {

        return hreflang;
    }

    /**
     * Returns a new Link with the title changed.
     *
     * @param title The title of the link.
     * @return
     */
    public Link withTitle(String title) {
        Link link = new Link(this);
        link.title = title;
        return link;
    }

    /**
     * Returns a new Link with the name changed.
     *
     * @param name The name of the link.
     * @return
     */
    public Link withName(String name) {
        Link link = new Link(this);
        link.name = name;
        return link;
    }

    /**
     * Returns a new Link with the type changed.
     *
     * @param type The type of the link.
     * @return
     */
    public Link withType(String type) {
        Link link = new Link(this);
        link.type = type;
        return link;
    }

    /**
     * Returns a new Link with the deprecation changed.
     *
     * @param deprecation The deprecation of the link.
     * @return
     */
    public Link withDeprecation(String deprecation) {
        Link link = new Link(this);
        link.deprecation = deprecation;
        return link;
    }

    /**
     * Returns a new Link with the profile changed.
     *
     * @param profile The profile of the link.
     * @return
     */
    public Link withProfile(String profile) {
        Link link = new Link(this);
        link.profile = profile;
        return link;
    }

    /**
     * Returns a new Link with the hreflang changed.
     *
     * @param hreflang The hreflang of the link.
     * @return
     */
    public Link withHreflang(String hreflang) {
        Link link = new Link(this);
        link.hreflang = hreflang;
        return link;
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
        if (!(obj instanceof Link)) {
            return false;
        }

        Link that = (Link)obj;

        if (!href.equals(that.href)) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (!rel.equals(that.rel)) {
            return false;
        }
        if (title != null ? !title.equals(that.title) : that.title != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (deprecation != null ? !deprecation.equals(that.deprecation) : that.deprecation != null) {
            return false;
        }
        if (profile != null ? !profile.equals(that.profile) : that.profile != null) {
            return false;
        }
        if (hreflang != null ? !hreflang.equals(that.hreflang) : that.hreflang != null) {
            return false;
        }

        return true;
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = 17;
		result += 31 * href.hashCode();
		result += 31 * rel.hashCode();
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (deprecation != null ? deprecation.hashCode() : 0);
        result = 31 * result + (profile != null ? profile.hashCode() : 0);
        result = 31 * result + (hreflang != null ? hreflang.hashCode() : 0);
		return result;
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
        StringBuilder str = new StringBuilder(String.format("<%s>;rel=\"%s\"", href, rel));

        if (title != null) {
            str.append(";title=\"");
            str.append(title);
            str.append("\"");
        }

        if (name != null) {
            str.append(";name=\"");
            str.append(name);
            str.append("\"");
        }

        if (type != null) {
            str.append(";type=\"");
            str.append(type);
            str.append("\"");
        }

        if (deprecation != null) {
            str.append(";deprecation=\"");
            str.append(deprecation);
            str.append("\"");
        }

        if (profile != null) {
            str.append(";profile=\"");
            str.append(profile);
            str.append("\"");
        }

        if (hreflang != null) {
            str.append(";hreflang=\"");
            str.append(hreflang);
            str.append("\"");
        }

        return str.toString();
	}

	/**
	 * Factory method to easily create {@link Link} instances from RFC-5988 compatible {@link String} representations of a
	 * link. Will return {@literal null} if an empty or {@literal null} {@link String} is given.
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

		Pattern uriAndAttributes = Pattern.compile("<(.*)>;(.*)");
		Matcher matcher = uriAndAttributes.matcher(element);

		if (matcher.find()) {

			Map<String, String> attributes = getAttributeMap(matcher.group(2));

			if (!attributes.containsKey("rel")) {
				throw new IllegalArgumentException("Link does not provide a rel attribute!");
			}

			return new Link(matcher.group(1), attributes.get("rel"));

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
		Pattern keyAndValue = Pattern.compile("(\\w+)=\\\"(\\p{Alnum}*)\"");
		Matcher matcher = keyAndValue.matcher(source);

		while (matcher.find()) {
			attributes.put(matcher.group(1), matcher.group(2));
		}

		return attributes;
	}
}
