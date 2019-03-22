/*
 * Copyright 2013-2014 the original author or authors.
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

import org.springframework.core.Ordered;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.RelProvider;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link RelProvider} to simply use the uncapitalized version of the given type's name as
 * single resource rel as well as an appended {@code List} for the collection resource rel.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class DefaultRelProvider implements RelProvider, Ordered {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForCollectionResource(java.lang.Class)
	 */
	@Override
	public LinkRelation getCollectionResourceRelFor(Class<?> type) {
		return LinkRelation.of(StringUtils.uncapitalize(type.getSimpleName()) + "List");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RelProvider#getRelForSingleResource(java.lang.Class)
	 */
	@Override
	public LinkRelation getItemResourceRelFor(Class<?> type) {
		return LinkRelation.of(StringUtils.uncapitalize(type.getSimpleName()));
	}
}
