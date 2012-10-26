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
package org.springframework.hateoas.hal;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonUnwrapped;
import org.springframework.hateoas.Link;
import org.springframework.util.Assert;

/**
 * A simple Resource wrapping a domain object and adding links and embedded resources to it.
 * 
 * @author Alexander Bätz
 */
@XmlRootElement
public class HalResource<T> extends HalResourceSupport {

    @JsonUnwrapped
    private final T content;

    /**
     * Creates an empty {@link Resource}.
     */
    protected HalResource() {
        this.content = null;
    }

    /**
     * Creates a new {@link HalResource} with the given content and {@link Link}s (optional).
     * 
     * @param content must not be {@literal null}.
     * @param links the links to add to the {@link Resource}.
     */
    public HalResource(T content, Link... links) {
        this(content, Arrays.asList(links));
    }

    /**
     * Creates a new {@link HalResource} with the given content and {@link Link}s.
     * 
     * @param content must not be {@literal null}.
     * @param links the links to add to the {@link Resource}.
     */
    public HalResource(T content, Iterable<Link> links) {

        Assert.notNull(content, "Content must not be null!");
        this.content = content;
        this.add(links);
    }

    /**
     * Returns the underlying entity.
     * 
     * @return the content
     */
    public T getContent() {
        return content;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.hateoas.hal.HalResourceSupport#toString()
     */
    @Override
    public String toString() {
        return String.format("Resource { content: %s, %s }", getContent(), super.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.hateoas.hal.HalResourceSupport#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !obj.getClass().equals(getClass())) {
            return false;
        }

        HalResource<?> that = (HalResource<?>) obj;

        boolean contentEqual = this.content == null ? that.content == null : this.content.equals(that.content);
        return contentEqual ? super.equals(obj) : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.hateoas.hal.HalResourceSupport#hashCode()
     */
    @Override
    public int hashCode() {

        int result = super.hashCode();
        result += content == null ? 0 : 17 * content.hashCode();
        return result;
    }
}
