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
package org.springframework.hateoas.mediatype.hal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.UriTemplate;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Default implementation of {@link CurieProvider} rendering a single configurable {@link UriTemplate} based curie.
 *
 * @author Oliver Gierke
 * @author Jeff Stano
 * @author Greg Turnquist
 * @since 0.9
 */
public class DefaultCurieProvider implements CurieProvider {

	private final Map<String, UriTemplate> curies;
	private final String defaultCurie;

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
		this(curies, null);
	}

	/**
	 * Creates a new {@link DefaultCurieProvider} for the given curies using the one with the given name as default, which
	 * means to expand unprefixed, non-IANA link relations.
	 *
	 * @param curies must not be {@literal null}.
	 * @param defaultCurieName can be {@literal null}.
	 * @since 0.19
	 */
	public DefaultCurieProvider(Map<String, UriTemplate> curies, @Nullable String defaultCurieName) {

		Assert.notNull(curies, "Curies must not be null!");

		curies.forEach((name, template) -> {

			Assert.hasText(name, "Curie name must not be null or empty!");
			Assert.notNull(template, "UriTemplate must not be null!");
			Assert.isTrue(template.getVariableNames().size() == 1,
					String.format("Expected a single template variable in the UriTemplate %s!", template.toString()));
		});

		this.defaultCurie = StringUtils.hasText(defaultCurieName) ? defaultCurieName
				: curies.size() == 1 ? curies.keySet().iterator().next() : null;
		this.curies = Collections.unmodifiableMap(curies);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.hal.CurieProvider#getCurieInformation()
	 */
	@Override
	public Collection<?> getCurieInformation(Links links) {

		return curies.entrySet().stream() //
				.map(it -> new Curie(it.getKey(), getCurieHref(it.getKey(), it.getValue()))) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableCollection));
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.hal.CurieProvider#getNamespacedRelFrom(org.springframework.hateoas.Link)
	 */
	@Override
	public HalLinkRelation getNamespacedRelFrom(Link link) {
		return getNamespacedRelFor(link.getRel());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.hal.CurieProvider#getNamespacedRelFrom(java.lang.String)
	 */
	@Override
	public HalLinkRelation getNamespacedRelFor(LinkRelation relation) {

		HalLinkRelation result = HalLinkRelation.of(relation);

		return defaultCurie == null ? result : result.curieIfUncuried(defaultCurie);
	}

	/**
	 * Returns the href for the {@link Curie} instance to be created. Will prepend the current application URI (servlet
	 * mapping) in case the template is not an absolute one in the first place.
	 *
	 * @param name will never be {@literal null} or empty.
	 * @param template will never be {@literal null}.
	 * @return the {@link String} to be used as href in the {@link Curie} to be created, must not be {@literal null}.
	 */
	protected String getCurieHref(String name, UriTemplate template) {

		if (template.toString().startsWith("http")) {
			return template.toString();
		}

		String applicationUri = ServletUriComponentsBuilder.fromCurrentServletMapping().build().expand().toString();
		return applicationUri.concat(template.toString());
	}

	/**
	 * Value object to get the curie {@link Link} rendered in JSON.
	 *
	 * @author Oliver Gierke
	 */
	protected static class Curie extends Link {

		private static final long serialVersionUID = 1L;

		private final String name;

		@SuppressWarnings("deprecation")
		public Curie(String name, String href) {

			super(href, "curies");
			this.name = name;
		}

		public String getName() {
			return this.name;
		}
	}
}
