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

import java.io.InputStream;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.plugin.core.Plugin;

/**
 * Interface to allow discovering links by relation type from some source.
 * 
 * @author Oliver Gierke
 */
public interface LinkDiscoverer extends Plugin<MediaType> {

	/**
	 * Finds a single link with the given relation type in the given {@link String} representation.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return the first link with the given relation type found, or {@literal null} if none was found.
	 */
	Link findLinkWithRel(String rel, String representation);

	/**
	 * Finds a single link with the given relation type in the given {@link InputStream} representation.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return the first link with the given relation type found, or {@literal null} if none was found.
	 */
	Link findLinkWithRel(String rel, InputStream representation);

	/**
	 * Returns all links with the given relation type found in the given {@link String} representation.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return
	 */
	List<Link> findLinksWithRel(String rel, String representation);

	/**
	 * Returns all links with the given relation type found in the given {@link InputStream} representation.
	 * 
	 * @param rel must not be {@literal null} or empty.
	 * @param representation must not be {@literal null} or empty.
	 * @return
	 */
	List<Link> findLinksWithRel(String rel, InputStream representation);
}
