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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.core.EmbeddedWrapper;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Builder class that allows collecting objects under the relation types defined for the objects but moving from the
 * single resource relation to the collection one, once more than one object of the same type is added.
 * 
 * @author Oliver Gierke
 * @author Dietrich Schulten
 */
class HalEmbeddedBuilder {

	private static final String DEFAULT_REL = "content";

	private final Map<String, Object> embeddeds = new HashMap<String, Object>();
	private final RelProvider provider;
	private final EmbeddedWrappers wrappers;

	/**
	 * Creates a new {@link HalEmbeddedBuilder} using the given {@link RelProvider} and prefer collection rels flag.
	 * 
	 * @param provider can be {@literal null}.
	 * @param preferCollectionRels whether to prefer to ask the provider for collection rels.
	 */
	public HalEmbeddedBuilder(RelProvider provider, boolean preferCollectionRels) {

		Assert.notNull(provider, "Relprovider must not be null!");

		this.provider = provider;
		this.wrappers = new EmbeddedWrappers(preferCollectionRels);
	}

	/**
	 * Adds the given value to the embeddeds. Will skip doing so if the value is {@literal null} or the content of a
	 * {@link Resource} is {@literal null}.
	 * 
	 * @param value can be {@literal null}.
	 */
	public void add(Object source) {

		EmbeddedWrapper wrapper = wrappers.wrap(source);

		if (wrapper == null) {
			return;
		}

		String collectionRel = getDefaultedRelFor(wrapper, true);
		String collectionOrItemRel = collectionRel;

		if (!embeddeds.containsKey(collectionRel)) {
			collectionOrItemRel = getDefaultedRelFor(wrapper, wrapper.isCollectionValue());
		}

		Object currentValue = embeddeds.get(collectionOrItemRel);
		Object value = wrapper.getValue();

		if (currentValue == null && !wrapper.isCollectionValue()) {
			embeddeds.put(collectionOrItemRel, value);
			return;
		}

		List<Object> list = new ArrayList<Object>();
		list.addAll(asCollection(currentValue));
		list.addAll(asCollection(wrapper.getValue()));

		embeddeds.remove(collectionOrItemRel);
		embeddeds.put(collectionRel, list);
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> asCollection(Object source) {
		return source instanceof Collection ? (Collection<Object>) source : source == null ? Collections.emptySet()
				: Collections.singleton(source);
	}

	private String getDefaultedRelFor(EmbeddedWrapper wrapper, boolean forCollection) {

		String valueRel = wrapper.getRel();

		if (StringUtils.hasText(valueRel)) {
			return valueRel;
		}

		if (provider == null) {
			return DEFAULT_REL;
		}

		Class<?> type = wrapper.getRelTargetType();

		String rel = forCollection ? provider.getCollectionResourceRelFor(type) : provider.getItemResourceRelFor(type);
		return rel == null ? DEFAULT_REL : rel;
	}

	/**
	 * Returns the added objects keyed up by their relation types.
	 * 
	 * @return
	 */
	public Map<String, Object> asMap() {
		return Collections.unmodifiableMap(embeddeds);
	}
}
