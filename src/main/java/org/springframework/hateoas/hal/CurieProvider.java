/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.Collection;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

/**
 * API to provide HAL curie information for links.
 * 
 * @see http://tools.ietf.org/html/draft-kelly-json-hal#section-8.2
 * @author Oliver Gierke
 * @since 0.9
 */
public interface CurieProvider {

	/**
	 * Returns the rel to be rendered for the given {@link Link}. Will potentially prefix the rel but also might decide
	 * not to, depending on the actual rel.
	 * 
	 * @param link
	 * @return
	 */
	String getNamespacedRelFrom(Link link);

	/**
	 * Returns an object to render as the base curie information. Implementations have to make sure, the retunred
	 * instances renders as defined in the spec.
	 * 
	 * @param links the {@link Links} that have been added to the response so far.
	 * @return must not be {@literal null}.
	 */
	Collection<? extends Object> getCurieInformation(Links links);
}
