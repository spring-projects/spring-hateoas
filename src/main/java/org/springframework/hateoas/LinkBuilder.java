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

import java.net.URI;

/**
 * Builder to ease building {@link Link} instances.
 * 
 * @author Ricardo Gladwell
 */
public interface LinkBuilder {

	/**
	 * Adds the given object's {@link String} representation as sub-resource to the current URI. Will unwrap
	 * {@link Identifiable}s to their id value (see {@link Identifiable#getId()}).
	 * 
	 * @param object
	 * @return
	 */
	LinkBuilder slash(Object object);

	/**
	 * Adds the given {@link Identifiable}'s id as sub-resource. Will simply return the {@link LinkBuilder} as is if the
	 * given entity is {@literal null}.
	 * 
	 * @param identifiable
	 * @return
	 */
	LinkBuilder slash(Identifiable<?> identifiable);

	/**
	 * Creates a URI of the link built by the current builder instance.
	 * 
	 * @return
	 */
	URI toUri();

	/**
	 * Creates the {@link Link} built by the current builder instance with the given rel.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	Link withRel(String rel);

	/**
	 * Creates the {@link Link} built by the current builder instance with the default self rel.
	 * 
	 * @see Link#REL_SELF
	 * @return
	 */
	Link withSelfRel();
}
