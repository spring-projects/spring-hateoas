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
package org.springframework.hateoas.mvc;

import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Base class to implement {@link LinkBuilder}s based on a Spring MVC {@link UriComponentsBuilder}.
 *
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Daniel Sawano
 */
public abstract class UriComponentsLinkBuilder<T extends LinkBuilder> implements LinkBuilder {

	private final LinkComponents linkComponents;

    /**
	 * Creates a new {@link UriComponentsLinkBuilder} using the given {@link LinkComponents}.
	 *
	 * @param linkComponents must not be {@literal null}.
	 */
	public UriComponentsLinkBuilder(LinkComponents linkComponents) {
        Assert.notNull(linkComponents);
        this.linkComponents = linkComponents;
	}

	@Override
    public T slash(Object object) {

		if (object == null) {
			return getThis();
		}

        String[] segments = StringUtils.tokenizeToStringArray(object.toString(), "/");
        UriComponents uriComponents = createUriComponentsBuilder().pathSegment(segments).build();
        return createNewInstance(createLinkComponents(uriComponents, getMethod()));
	}

    private UriComponentsBuilder createUriComponentsBuilder() {
        return UriComponentsBuilder.fromUri(getUriComponents().toUri());
    }

    private UriComponents getUriComponents() {
        return linkComponents.getUriComponents();
    }

    private LinkComponents createLinkComponents(UriComponents uriComponents, HttpMethod method) {
        return new LinkComponents(uriComponents, method);
    }

	@Override
    public LinkBuilder slash(Identifiable<?> identifyable) {

		if (identifyable == null) {
			return this;
		}

		return slash(identifyable.getId());
	}

    @Override
    public LinkBuilder method(HttpMethod method) {
        return createNewInstance(createLinkComponents(createUriComponentsBuilder().build(), method));
    }

	@Override
    public URI toUri() {
		return getUriComponents().encode().toUri();
	}

	@Override
    public Link withRel(String rel) {
		return new Link(this.toString(), rel, getMethod());
	}

    private HttpMethod getMethod() {
        return linkComponents.getMethod();
    }

	@Override
    public Link withSelfRel() {
		return new Link(this.toString(),Link.REL_SELF, getMethod());
	}

	@Override
	public String toString() {
		return toUri().normalize().toASCIIString();
	}

	/**
	 * Returns the current concrete instance.
	 *
	 * @return
	 */
	protected abstract T getThis();

	/**
	 * Creates a new instance of the sub-class.
	 *
	 * @param linkComponents will never be {@literal null}.
	 * @return
	 */
	protected abstract T createNewInstance(LinkComponents linkComponents);
}
