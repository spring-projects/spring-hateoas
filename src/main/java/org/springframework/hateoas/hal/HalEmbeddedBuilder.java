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
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.core.ObjectUtils;

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
	private final boolean enforceCollections;

	/**
	 * Creates a new {@link HalEmbeddedBuilder} using the given {@link RelProvider}.
	 * 
	 * @param provider can be {@literal null}.
	 */
	public HalEmbeddedBuilder(RelProvider provider, boolean enforceCollections) {
		this.provider = provider;
		this.enforceCollections = enforceCollections;
	}

	/**
	 * Adds the given value to the embeddeds. Will skip doing so if the value is {@literal null} or the content of a
	 * {@link Resource} is {@literal null}.
	 * 
	 * @param value
	 */
	public void add(Object value) {

		Object unwrapped = ObjectUtils.getResourceType(value);

		if (unwrapped == null) {
			return;
		}

		String rel = getDefaultedRelFor(unwrapped, true);

		if (!embeddeds.containsKey(rel)) {
			rel = getDefaultedRelFor(unwrapped, enforceCollections);
		}

		List<Object> currentValue = embeddeds.get(rel);

		if (currentValue == null) {
			ArrayList<Object> arrayList = new ArrayList<Object>();
			arrayList.add(value);
			embeddeds.put(rel, arrayList);
		} else if (currentValue.size() == 1) {
			currentValue.add(value);
			embeddeds.remove(rel);
			embeddeds.put(getDefaultedRelFor(unwrapped, true), currentValue);
		} else {
			currentValue.add(value);
		}
	}

	private String getDefaultedRelFor(Object value, boolean forCollection) {

		if (provider == null) {
			return DEFAULT_REL;
		}

		Class<?> type = AopUtils.getTargetClass(value);

		String rel = forCollection ? provider.getCollectionResourceRelFor(type) : provider.getItemResourceRelFor(type);
		return rel == null ? DEFAULT_REL : rel;
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
