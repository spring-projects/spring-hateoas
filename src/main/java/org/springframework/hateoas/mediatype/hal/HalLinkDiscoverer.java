/*
 * Copyright 2013-2020 the original author or authors.
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

import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.JsonPathLinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.http.MediaType;

/**
 * {@link LinkDiscoverer} implementation based on HAL link structure.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class HalLinkDiscoverer extends JsonPathLinkDiscoverer {

	/**
	 * Constructor for {@link MediaTypes#HAL_JSON}.
	 */
	public HalLinkDiscoverer() {
		this(MediaTypes.HAL_JSON);
	}

	protected HalLinkDiscoverer(MediaType... mediaTypes) {
		super("$._links..['%s']", mediaTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.JsonPathLinkDiscoverer#extractLink(java.lang.Object, org.springframework.hateoas.LinkRelation)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected Link extractLink(Object element, LinkRelation rel) {

		if (!Map.class.isInstance(element)) {
			return super.extractLink(element, rel);
		}

		Map<String, String> json = (Map<String, String>) element;

		return Link.of(json.get("href"), rel) //
				.withHreflang(json.get("hreflang")) //
				.withMedia(json.get("media")) //
				.withTitle(json.get("title")) //
				.withType(json.get("type")) //
				.withDeprecation(json.get("deprecation")) //
				.withProfile(json.get("profile")) //
				.withName(json.get("name"));
	}
}
