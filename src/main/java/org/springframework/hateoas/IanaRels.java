/*
 * Copyright 2013-2016 the original author or authors.
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import lombok.experimental.UtilityClass;

/**
 * Static class to find out whether a relation type is defined by the IANA.
 * 
 * @see http://www.iana.org/assignments/link-relations/link-relations.xhtml
 * @author Oliver Gierke
 * @author Roland Kulcsár
 */
@UtilityClass
public class IanaRels {

	private static final Collection<String> RELS;

	static {

		Collection<String> rels = new HashSet<String>();

		rels.addAll(Arrays.asList("about", "alternate", "appendix", "archives", "author", "blocked-by", "bookmark",
				"canonical", "chapter", "collection", "contents", "convertedFrom", "copyright", "create-form", "current",
				"describedby", "describes", "disclosure", "dns-prefetch", "duplicate", "edit", "edit-form", "edit-media",
				"enclosure", "first", "glossary", "help", "hosts", "hub", "icon", "index", "item", "last", "latest-version",
				"license", "lrdd", "memento", "monitor", "monitor-group", "next", "next-archive", "nofollow", "noreferrer",
				"original", "payment", "pingback", "preconnect", "predecessor-version", "prefetch", "preload", "prerender",
				"prev", "preview", "previous", "prev-archive", "privacy-policy", "profile", "related", "restconf", "replies",
				"search", "section", "self", "service", "start", "stylesheet", "subsection", "successor-version", "tag",
				"terms-of-service", "timegate", "timemap", "type", "up", "version-history", "via", "webmention",
				"working-copy", "working-copy-of"));

		RELS = Collections.unmodifiableCollection(rels);
	}

	/**
	 * Returns whether the given relation type is defined by the IANA.
	 * 
	 * @param rel the relation type to check
	 * @return
	 */
	public static boolean isIanaRel(String rel) {
		return rel == null ? false : RELS.contains(rel);
	}
}
