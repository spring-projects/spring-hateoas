/*
 * Copyright 2013-2021 the original author or authors.
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
package org.springframework.hateoas.server.core;

import org.atteo.evo.inflector.English;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.LinkRelationProvider;

/**
 * {@link LinkRelationProvider} implementation using the Evo Inflector implementation of an algorithmic approach to
 * English plurals.
 *
 * @see http://users.monash.edu/~damian/papers/HTML/Plurals.html
 * @author Oliver Gierke
 */
public class EvoInflectorLinkRelationProvider extends DefaultLinkRelationProvider {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.server.core.DefaultLinkRelationProvider#getCollectionResourceRelFor(java.lang.Class)
	 */
	@Override
	public LinkRelation getCollectionResourceRelFor(Class<?> type) {
		return LinkRelation.of(English.plural(getItemResourceRelFor(type).value()));
	}
}
