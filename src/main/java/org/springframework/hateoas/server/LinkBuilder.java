/*
 * Copyright 2012-2020 the original author or authors.
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
package org.springframework.hateoas.server;

import java.net.URI;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.lang.Nullable;

/**
 * Builder to ease building {@link Link} instances.
 *
 * @author Ricardo Gladwell
 * @author Oliver Drotbohm
 */
public interface LinkBuilder {

	/**
	 * Adds the given object's {@link String} representation as sub-resource to the current URI.
	 *
	 * @param object can be {@literal null}.
	 * @return
	 */
	LinkBuilder slash(@Nullable Object object);

	/**
	 * Creates a URI of the link built by the current builder instance.
	 *
	 * @return
	 */
	URI toUri();

	/**
	 * Creates the {@link Link} built by the current builder instance with the given link relation.
	 *
	 * @param rel must not be {@literal null}.
	 * @return
	 */
	default Link withRel(String rel) {
		return withRel(LinkRelation.of(rel));
	}

	/**
	 * Creates the {@link Link} built by the current builder instance with the given {@link LinkRelation}.
	 *
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	Link withRel(LinkRelation rel);

	/**
	 * Creates the {@link Link} built by the current builder instance with the default self link relation.
	 *
	 * @see IanaLinkRelations#SELF
	 * @return
	 */
	Link withSelfRel();
}
