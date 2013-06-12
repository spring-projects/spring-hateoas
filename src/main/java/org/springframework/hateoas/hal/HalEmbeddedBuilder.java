/*
 * Copyright 2013 the original author or authors.
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

import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.core.ObjectUtils;

/**
 * Builder class that allows collecting objects under the relation types defined for the objects but moving from the
 * single resource relation to the collection one, once more than one object of the same type is added.
 * 
 * @author Oliver Gierke
 */
class HalEmbeddedBuilder {

	private static final String DEFAULT_REL = "content";

	private final Map<String, List<Object>> embeddeds = new HashMap<String, List<Object>>();
	private final RelProvider provider;

	/**
	 * Creates a new {@link HalEmbeddedBuilder} using the given {@link RelProvider}.
	 * 
	 * @param provider can be {@literal null}.
	 */
	public HalEmbeddedBuilder(RelProvider provider) {
		this.provider = provider;
	}

	/**
	 * Adds the given value to the embeddeds.
	 * 
	 * @param value
	 */
	public void add(Object value) {

		Class<?> type = ObjectUtils.getResourceType(value);
		if (type != null) {
			String singleRel = getDefaultedRelFor(type, false);
			List<Object> currentValue = embeddeds.get(singleRel);

			if (currentValue == null) {
				ArrayList<Object> arrayList = new ArrayList<Object>();
				arrayList.add(value);
				embeddeds.put(singleRel, arrayList);
			} else if (currentValue.size() == 1) {
				currentValue.add(value);
				embeddeds.remove(singleRel);
				embeddeds.put(getDefaultedRelFor(type, true), currentValue);
			} else {
				currentValue.add(value);
			}
		}
	}

	private String getDefaultedRelFor(Class<?> type, boolean forCollection) {

		if (provider == null) {
			return DEFAULT_REL;
		}

		String rel = forCollection ? provider.getCollectionResourceRelFor(type) : provider.getSingleResourceRelFor(type);
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
