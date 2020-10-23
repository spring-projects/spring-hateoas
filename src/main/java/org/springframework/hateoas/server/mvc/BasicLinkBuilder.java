/*
 * Copyright 2012-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import java.util.List;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.core.LinkBuilderSupport;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Simples {@link LinkBuilder} implementation possible. Exposes a link to the current servlet mapping only.
 *
 * @author Oliver Gierke
 */
public class BasicLinkBuilder extends LinkBuilderSupport<BasicLinkBuilder> {

	/**
	 * Creates a new {@link BasicLinkBuilder} using the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 */
	private BasicLinkBuilder(UriComponents components) {
		super(components);
	}

	private BasicLinkBuilder(UriComponents components, List<Affordance> affordances) {
		super(components, affordances);
	}

	/**
	 * Creates a new {@link BasicLinkBuilder} to link to the current servlet mapping.
	 *
	 * @return
	 */
	public static BasicLinkBuilder linkToCurrentMapping() {
		return new BasicLinkBuilder(ServletUriComponentsBuilder.fromCurrentServletMapping().build());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.LinkBuilderSupport#createNewInstance(org.springframework.web.util.UriComponents, java.util.List)
	 */
	@Override
	protected BasicLinkBuilder createNewInstance(UriComponents components, List<Affordance> affordances) {
		return new BasicLinkBuilder(components, affordances);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.mvc.LinkBuilderSupport#getThis()
	 */
	@Override
	protected BasicLinkBuilder getThis() {
		return this;
	}
}
