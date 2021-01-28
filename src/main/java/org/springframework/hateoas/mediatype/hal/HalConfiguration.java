/*
 * Copyright 2017-2021 the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HAL specific configuration.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class HalConfiguration {

	private static final PathMatcher MATCHER = new AntPathMatcher();

	/**
	 * Configures how to render links in case there is exactly one defined for a given link relation in general. By
	 * default, this single link will be rendered as nested document.
	 */
	private final RenderSingleLinks renderSingleLinks;
	private final Map<String, RenderSingleLinks> singleLinksPerPattern;
	private final Consumer<ObjectMapper> objectMapperCustomizer;

	/**
	 * Configures whether the Jackson property naming strategy is applied to link relations and within {@code _embedded}
	 * clauses.
	 */
	private final boolean applyPropertyNamingStrategy;

	/**
	 * Configures whether to always use collections for embeddeds, even if there's only one entry for a link relation.
	 * Defaults to {@literal true}.
	 */
	private final boolean enforceEmbeddedCollections;

	/**
	 * Creates a new default {@link HalConfiguration} rendering single links as immediate sub-document.
	 */
	public HalConfiguration() {
		this(RenderSingleLinks.AS_SINGLE, new LinkedHashMap<>(), true, true, __ -> {});
	}

	private HalConfiguration(RenderSingleLinks renderSingleLinks, Map<String, RenderSingleLinks> singleLinksPerPattern,
			boolean applyPropertyNamingStrategy, boolean enforceEmbeddedCollections,
			Consumer<ObjectMapper> objectMapperCustomizer) {

		Assert.notNull(renderSingleLinks, "RenderSingleLinks must not be null!");
		Assert.notNull(singleLinksPerPattern, "Single links per pattern map must not be null!");
		Assert.notNull(objectMapperCustomizer, "ObjectMapper customizer must not be null!");

		this.renderSingleLinks = renderSingleLinks;
		this.singleLinksPerPattern = singleLinksPerPattern;
		this.applyPropertyNamingStrategy = applyPropertyNamingStrategy;
		this.enforceEmbeddedCollections = enforceEmbeddedCollections;
		this.objectMapperCustomizer = objectMapperCustomizer;
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
	 * @param pattern must not be {@literal null} or empty.
	 * @param renderSingleLinks must not be {@literal null}.
	 * @return @see PathMatcher
	 */
	public HalConfiguration withRenderSingleLinksFor(String pattern, RenderSingleLinks renderSingleLinks) {

		Assert.hasText(pattern, "Pattern must not be null or empty!");
		Assert.notNull(renderSingleLinks, "RenderSingleLinks must not be null!");

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
	 * Create a new {@link HalConfiguration} by copying the attributes and replacing the {@literal renderSingleLinks}.
	 *
	 * @param renderSingleLinks must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public HalConfiguration withRenderSingleLinks(RenderSingleLinks renderSingleLinks) {

		Assert.notNull(renderSingleLinks, "RenderSingleLinks must not be null!");

		return this.renderSingleLinks == renderSingleLinks //
				? this //
				: new HalConfiguration(renderSingleLinks, singleLinksPerPattern, applyPropertyNamingStrategy,
						enforceEmbeddedCollections, objectMapperCustomizer);
	}

	/**
	 * Create a new {@link HalConfiguration} by copying the attributes and replacing the {@literal singleLinksPattern}.
	 *
	 * @param singleLinksPerPattern must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	private HalConfiguration withSingleLinksPerPattern(Map<String, RenderSingleLinks> singleLinksPerPattern) {

		Assert.notNull(singleLinksPerPattern, "Single links per pattern map must not be null!");

		return this.singleLinksPerPattern == singleLinksPerPattern //
				? this //
				: new HalConfiguration(renderSingleLinks, singleLinksPerPattern, applyPropertyNamingStrategy,
						enforceEmbeddedCollections, objectMapperCustomizer);
	}

	/**
	 * Create a new {@link HalConfiguration} by copying the attributes and replacing the
	 * {@literal applyProperNamingStrategy}.
	 *
	 * @param applyPropertyNamingStrategy
	 * @return
	 */
	public HalConfiguration withApplyPropertyNamingStrategy(boolean applyPropertyNamingStrategy) {

		return this.applyPropertyNamingStrategy == applyPropertyNamingStrategy //
				? this //
				: new HalConfiguration(renderSingleLinks, singleLinksPerPattern, applyPropertyNamingStrategy,
						enforceEmbeddedCollections, objectMapperCustomizer);
	}

	/**
	 * Create a new {@link HalConfiguration} by copying the attributes and replacing the
	 * {@literal enforceEmbeddedCollections}.
	 *
	 * @param enforceEmbeddedCollections
	 * @return
	 */
	public HalConfiguration withEnforceEmbeddedCollections(boolean enforceEmbeddedCollections) {

		return this.enforceEmbeddedCollections == enforceEmbeddedCollections //
				? this //
				: new HalConfiguration(renderSingleLinks, singleLinksPerPattern, applyPropertyNamingStrategy,
						enforceEmbeddedCollections, objectMapperCustomizer);
	}

	public HalConfiguration withObjectMapperCustomizer(Consumer<ObjectMapper> objectMapperCustomizer) {

		return this.objectMapperCustomizer == objectMapperCustomizer //
				? this //
				: new HalConfiguration(renderSingleLinks, singleLinksPerPattern, applyPropertyNamingStrategy,
						enforceEmbeddedCollections, objectMapperCustomizer);
	}

	public RenderSingleLinks getRenderSingleLinks() {
		return renderSingleLinks;
	}

	public boolean isApplyPropertyNamingStrategy() {
		return applyPropertyNamingStrategy;
	}

	public boolean isEnforceEmbeddedCollections() {
		return enforceEmbeddedCollections;
	}

	public HalConfiguration customize(ObjectMapper mapper) {

		this.objectMapperCustomizer.accept(mapper);

		return this;
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
