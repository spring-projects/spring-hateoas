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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.aop.support.AopUtils;
import org.springframework.hateoas.RelAware;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.core.ObjectUtils;
import org.springframework.util.Assert;

/**
 * Builder class that allows collecting objects under the relation types defined for the objects but moving from the
 * single resource relation to the collection one, once more than one object of the same type is added.
 * 
 * @author Oliver Gierke
 * @author Dietrich Schulten
 */
class HalEmbeddedBuilder {

	private static final String DEFAULT_REL = "content";

	private final Map<String, List<Object>> embeddeds = new HashMap<String, List<Object>>();
	private final RelProvider provider;
	private final boolean preferCollectionRels;

	private boolean relAwareFound;

	/**
	 * Creates a new {@link HalEmbeddedBuilder} using the given {@link RelProvider} and prefer collection rels flag.
	 * 
	 * @param provider can be {@literal null}.
	 * @param preferCollectionRels whether to prefer to ask the provider for collection rels.
	 */
	public HalEmbeddedBuilder(RelProvider provider, boolean preferCollectionRels) {

		Assert.notNull(provider, "Relprovider must not be null!");

		this.provider = provider;
		this.preferCollectionRels = preferCollectionRels;
	}

	/**
	 * Adds the given value to the embeddeds. Will skip doing so if the value is {@literal null} or the content of a
	 * {@link Resource} is {@literal null}.
	 * 
	 * @param value
	 */
	public void add(Object value) {

		if (ObjectUtils.getResourceType(value) == null) {
			return;
		}

		String rel = getDefaultedRelFor(value, true);

		if (!embeddeds.containsKey(rel)) {
			rel = getDefaultedRelFor(value, preferCollectionRels);
		}

		List<Object> currentValue = embeddeds.get(rel);

		if (currentValue == null) {
			ArrayList<Object> arrayList = new ArrayList<Object>();
			arrayList.add(value);
			embeddeds.put(rel, arrayList);
		} else if (currentValue.size() == 1) {
			currentValue.add(value);
			embeddeds.remove(rel);
			embeddeds.put(getDefaultedRelFor(value, true), currentValue);
		} else {
			currentValue.add(value);
		}
	}

	private String getDefaultedRelFor(Object value, boolean forCollection) {

		Object unwrapped = ObjectUtils.getResourceType(value);

		if (value instanceof RelAware) {
			this.relAwareFound = true;
			return ((RelAware) value).getRel();
		}

		if (provider == null) {
			return DEFAULT_REL;
		}

		Class<?> type = AopUtils.getTargetClass(unwrapped);

		String rel = forCollection ? provider.getCollectionResourceRelFor(type) : provider.getItemResourceRelFor(type);
		return rel == null ? DEFAULT_REL : rel;
	}

	/**
	 * Returns whether the builder only created collection rels.
	 * 
	 * @return
	 */
	public boolean hasOnlyCollections() {
		return preferCollectionRels && !relAwareFound;
	}

	/**
	 * Returns the added objects keyed up by their relation types.
	 * 
	 * @return
	 */
	public Map<String, List<Object>> asMap() {
		return Collections.unmodifiableMap(embeddeds);
	}
}
