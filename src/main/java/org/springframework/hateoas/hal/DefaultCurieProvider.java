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

import org.springframework.hateoas.IanaRels;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.UriTemplate;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link CurieProvider} rendering a single configurable {@link UriTemplate} based curie.
 * 
 * @author Oliver Gierke
 * @since 0.9
 */
public class DefaultCurieProvider implements CurieProvider {

	private final Curie curie;

	/**
	 * Creates a new {@link DefaultCurieProvider} for the given name and {@link UriTemplate}.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param uriTemplate must not be {@literal null} and contain exactly one template variable.
	 */
	public DefaultCurieProvider(String name, UriTemplate uriTemplate) {

		Assert.hasText(name, "Name must not be null or empty!");
		Assert.notNull(uriTemplate, "UriTemplate must not be null!");
		Assert.isTrue(uriTemplate.getVariableNames().size() == 1,
				String.format("Expected a single template variable in the UriTemplate %s!", uriTemplate.toString()));

		this.curie = new Curie(name, uriTemplate.toString());
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.hal.CurieProvider#getCurieInformation()
	 */
	@Override
	public Collection<? extends Object> getCurieInformation(Links links) {
		return Collections.singleton(curie);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.hal.CurieProvider#getNamespacedRelFrom(org.springframework.hateoas.Link)
	 */
	@Override
	public String getNamespacedRelFrom(Link link) {

		String rel = link.getRel();

		boolean prefixingNeeded = !IanaRels.isIanaRel(rel) && !rel.contains(":");
		return prefixingNeeded ? String.format("%s:%s", curie.name, rel) : rel;
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
