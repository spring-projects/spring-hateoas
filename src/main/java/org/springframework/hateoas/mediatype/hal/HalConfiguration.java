/*
 * Copyright 2017-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

/**
 * HAL specific configuration.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HalConfiguration {

	private static final PathMatcher MATCHER = new AntPathMatcher();

	/**
	 * Configures how to render links in case there is exactly one defined for a given link relation in general. By
	 * default, this single link will be rendered as nested document.
	 */
	private final @Wither @Getter RenderSingleLinks renderSingleLinks;
	private final @Wither(AccessLevel.PRIVATE) Map<String, RenderSingleLinks> singleLinksPerPattern;

	/**
	 * Creates a new default {@link HalConfiguration} rendering single links as immediate sub-document.
	 */
	public HalConfiguration() {

		this.renderSingleLinks = RenderSingleLinks.AS_SINGLE;
		this.singleLinksPerPattern = new LinkedHashMap<>();
	}

	/**
	 * Configures how to render a single link for a given particular {@link LinkRelation}. This will override what has
	 * been configured via {@link #withRenderSingleLinks(RenderSingleLinks)} for that particular link relation.
	 *
	 * @param relation must not be {@literal null}.
	 * @param renderSingleLinks must not be {@literal null}.
	 * @return
	 */
	public HalConfiguration withRenderSingleLinksFor(LinkRelation relation, RenderSingleLinks renderSingleLinks) {

		Assert.notNull(relation, "Link relation must not be null!");
		Assert.notNull(renderSingleLinks, "RenderSingleLinks must not be null!");

		return withRenderSingleLinksFor(relation.value(), renderSingleLinks);
	}

	/**
	 * Configures how to render a single link for the given link relation pattern, i.e. this can be either a fixed link
	 * relation (like {@code search}), take wildcards to e.g. match links of a given curie (like {@code acme:*}) or even
	 * complete URIs (like {@code https://api.acme.com/foo/**}).
	 *
	 * @param pattern must not be {@literal null}.
	 * @param renderSingleLinks must not be {@literal null}.
	 * @return @see PathMatcher
	 */
	public HalConfiguration withRenderSingleLinksFor(String pattern, RenderSingleLinks renderSingleLinks) {

		Map<String, RenderSingleLinks> map = new LinkedHashMap<>(singleLinksPerPattern);
		map.put(pattern, renderSingleLinks);

		return withSingleLinksPerPattern(map);
	}

	/**
	 * Returns which render mode to use to render a single link for the given {@link LinkRelation}.
	 *
	 * @param relation must not be {@literal null}.
	 * @return
	 */
	RenderSingleLinks getSingleLinkRenderModeFor(LinkRelation relation) {

		return singleLinksPerPattern.entrySet().stream() //
				.filter(entry -> MATCHER.match(entry.getKey(), relation.value())) //
				.map(Entry::getValue) //
				.findFirst() //
				.orElse(renderSingleLinks);
	}

	/**
	 * Configuration option how to render single links of a given {@link LinkRelation}.
	 *
	 * @author Oliver Drotbohm
	 */
	public enum RenderSingleLinks {

		/**
		 * A single {@link Link} is rendered as a JSON object.
		 */
		AS_SINGLE,

		/**
		 * A single {@link Link} is rendered as a JSON Array.
		 */
		AS_ARRAY
	}
}
