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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.IanaRels;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.UriTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default implementation of {@link CurieProvider} rendering a single configurable {@link UriTemplate} based curie.
 *
 * @author Oliver Gierke
 * @author Jeff Stano
 * @since 0.9
 */
//TODO: update method javadocs
public class DefaultCurieProvider implements CurieProvider {

	private final Map<String, Link> curies;
	private final Link defaultCurie;

	/**
	 * Creates a new {@link DefaultCurieProvider} for the given name and {@link UriTemplate}. The curie will be used to
	 * expand previously unprefixed, non-IANA link relations.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @param uriTemplate must not be {@literal null} and contain exactly one template variable.
	 */
	public DefaultCurieProvider(String name, UriTemplate uriTemplate) {
		this(Collections.singletonMap(name, uriTemplate));
	}

	/**
	 * Creates a new {@link DefaultCurieProvider} for the given curies. If more than one curie is given, no default curie
	 * will be registered. Use {@link #DefaultCurieProvider(Map, String)} to define which of the provided curies shall be
	 * used as the default one.
	 *
	 * @param curies must not be {@literal null}.
	 * @see #DefaultCurieProvider(String, UriTemplate)
	 * @since 0.19
	 */
	public DefaultCurieProvider(Map<String, UriTemplate> curies) {
		this(curies, null, null);
	}

	/**
	 * Creates a new {@link DefaultCurieProvider} for the given curies using the one with the given name, title 
	 * as default, which means to expand unprefixed, non-IANA link relations.
	 *
	 * @param curies must not be {@literal null}.
	 * @param defaultCurieName can be {@literal null}.
	 * @param defaultTitle may be {@literal null} or empty.
	 * @since 0.19
	 */
	public DefaultCurieProvider(Map<String, UriTemplate> curies, String defaultCurieName, String defaultTitle) {

		Assert.notNull(curies, "Curies must not be null!");

		Map<String, Link> map = new HashMap<String, Link>(curies.size());

		for (Map.Entry<String, UriTemplate> entry : curies.entrySet()) {

			String name = entry.getKey();
			UriTemplate template = entry.getValue();
			map.put(name, buildCurie(name, defaultTitle, template));
		}
		this.defaultCurie = StringUtils.hasText(defaultCurieName) ? map.get(defaultCurieName)
				: map.size() == 1 ? map.values().iterator().next() : null;
		this.curies = Collections.unmodifiableMap(map);
	}

	/* 
     * (non-Javadoc)
     * @see org.springframework.hateoas.hal.CurieProvider#getCurieInformation()
     */
	@Override
	public Collection<? extends Object> getCurieInformation(Links links) {
		return Collections.unmodifiableCollection(curies.values());
	}

	/* 
     * (non-Javadoc)
     * @see org.springframework.hateoas.hal.CurieProvider#getNamespacedRelFrom(org.springframework.hateoas.Link)
     */
	@Override
	public String getNamespacedRelFrom(Link link) {
		return getNamespacedRelFor(link.getRel());
	}

	/* 
     * (non-Javadoc)
     * @see org.springframework.hateoas.hal.CurieProvider#getNamespacedRelFrom(java.lang.String)
     */
	@Override
	public String getNamespacedRelFor(String rel) {

		boolean prefixingNeeded = defaultCurie != null && !IanaRels.isIanaRel(rel) && !rel.contains(":");
		return prefixingNeeded ? String.format("%s:%s", defaultCurie.getName(), rel) : rel;
	}

	private Link buildCurie(String name, String title, UriTemplate uriTemplate) {
		Assert.hasText(name, "curie prefix must not be null or empty");
		Assert.isTrue(uriTemplate.getVariableNames().size() == 1,
				String.format("Expected a single template variable in the UriTemplate %s!", uriTemplate.toString()));

		return new Link.Builder()
				.name(name)
				.href(uriTemplate.toString())
				.rel("curies")
				.title(title)
				.preferCollections()
				.build();
	}

	/**
	 * Value object to get the curie {@link Link} rendered in JSON.
	 *
	 * @author Oliver Gierke
	 */
	protected static class Curie extends Link {

		private static final long serialVersionUID = 1L;

		private final String name;

		public Curie(String name, String href) {

			super(href, "curies");
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}		
}
