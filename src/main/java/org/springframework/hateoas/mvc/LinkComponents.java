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

import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;

/**
 * @author Daniel Sawano
 */
public class LinkComponents {
    private final UriComponents uriComponents;
    private final HttpMethod method;

    public LinkComponents(UriComponents uriComponents, HttpMethod method) {
        Assert.notNull(uriComponents);

        this.uriComponents = uriComponents;
        this.method = method;
    }

    public UriComponents getUriComponents() {
        return uriComponents;
    }

    public HttpMethod getMethod() {
        return method;
    }
}
