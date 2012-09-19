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

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Value object for links.
 *
 * @author Oliver Gierke
 * @author Daniel Sawano
 */
@XmlType(name = "link", namespace = Link.ATOM_NAMESPACE)
public class Link implements Serializable {

	private static final long serialVersionUID = -9037755944661782121L;

	public static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";

	public static final String REL_SELF = "self";
	public static final String REL_FIRST = "first";
	public static final String REL_PREVIOUS = "previous";
	public static final String REL_NEXT = "next";
	public static final String REL_LAST = "last";

	@XmlAttribute
	private String rel;
	@XmlAttribute
	private String href;
    @XmlAttribute
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    private HttpMethod method;

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
        this(href, rel, null);
    }

    /**
     * Creates a new {@link Link} to the given URI with the given rel and the given HTTP method.
     *
     * @param href must not be {@literal null} or empty.
     * @param rel must not be {@literal null} or empty.
     * @param  method can be null
     */
	public Link(String href, String rel, HttpMethod method) {

        Assert.hasText(href, "Href must not be null or empty!");
        Assert.hasText(rel, "Rel must not be null or empty!");

        this.href = href;
        this.rel = rel;
        this.method = method;
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
     * Returns the method of the link.
     *
     * @return
     */
    public HttpMethod getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link)) return false;

        final Link link = (Link) o;

        if (href != null ? !href.equals(link.href) : link.href != null) return false;
        if (method != link.method) return false;
        if (rel != null ? !rel.equals(link.rel) : link.rel != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rel != null ? rel.hashCode() : 0;
        result = 31 * result + (href != null ? href.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Link{" +
                "rel='" + rel + '\'' +
                ", href='" + href + '\'' +
                ", method=" + method +
                '}';
    }
}
