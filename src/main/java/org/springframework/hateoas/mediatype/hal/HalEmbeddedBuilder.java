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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Builder class that allows collecting objects under the relation types defined for the objects but moving from the
 * single resource relation to the collection one, once more than one object of the same type is added.
 *
 * @author Oliver Gierke
 * @author Dietrich Schulten
 */
class HalEmbeddedBuilder {

	private static final String INVALID_EMBEDDED_WRAPPER = "Embedded wrapper %s returned null for both the static rel and the rel target type! Make sure one of the two returns a non-null value!";

	private final Map<HalLinkRelation, Object> embeddeds = new HashMap<>();
	private final LinkRelationProvider provider;
	private final CurieProvider curieProvider;
	private final EmbeddedWrappers wrappers;

	/**
	 * Creates a new {@link HalEmbeddedBuilder} using the given {@link LinkRelationProvider} and prefer collection rels
	 * flag.
	 *
	 * @param provider must not be {@literal null}.
	 * @param curieProvider must not be {@literal null}.
	 * @param preferCollectionRels whether to prefer to ask the provider for collection rels.
	 */
	public HalEmbeddedBuilder(LinkRelationProvider provider, CurieProvider curieProvider, boolean preferCollectionRels) {

		Assert.notNull(provider, "LinkRelationProvider must not be null!");

		this.provider = provider;
		this.curieProvider = curieProvider;
		this.wrappers = new EmbeddedWrappers(preferCollectionRels);
	}

	/**
	 * Adds the given value to the embeddeds. Will skip doing so if the value is {@literal null} or the content of a
	 * {@link EntityModel} is {@literal null}.
	 *
	 * @param source can be {@literal null}.
	 */
	public void add(@Nullable Object source) {

		EmbeddedWrapper wrapper = wrappers.wrap(source);

		if (wrapper == null) {
			return;
		}

		HalLinkRelation collectionRel = getDefaultedRelFor(wrapper, true);
		HalLinkRelation collectionOrItemRel = collectionRel;

		if (!embeddeds.containsKey(collectionRel)) {
			collectionOrItemRel = getDefaultedRelFor(wrapper, wrapper.isCollectionValue());
		}

		Object currentValue = embeddeds.get(collectionOrItemRel);
		Object value = wrapper.getValue();

		if (currentValue == null && !wrapper.isCollectionValue()) {
			embeddeds.put(collectionOrItemRel, value);
			return;
		}

		List<Object> list = new ArrayList<>();
		list.addAll(asCollection(currentValue));
		list.addAll(asCollection(wrapper.getValue()));

		embeddeds.remove(collectionOrItemRel);
		embeddeds.put(collectionRel, list);
	}

	@SuppressWarnings("unchecked")
	private Collection<Object> asCollection(@Nullable Object source) {

		return source instanceof Collection //
				? (Collection<Object>) source //
				: source == null ? Collections.emptySet() : Collections.singleton(source);
	}

	private HalLinkRelation getDefaultedRelFor(EmbeddedWrapper wrapper, boolean forCollection) {

		return wrapper.getRel() //
				.map(HalLinkRelation::of) //
				.orElseGet(() -> {

					Class<?> type = wrapper.getRelTargetType();

					if (type == null) {
						throw new IllegalStateException(String.format(INVALID_EMBEDDED_WRAPPER, wrapper));
					}

					LinkRelation rel = forCollection //
							? provider.getCollectionResourceRelFor(type) //
							: provider.getItemResourceRelFor(type);

					return curieProvider != CurieProvider.NONE //
							? curieProvider.getNamespacedRelFor(rel) //
							: HalLinkRelation.of(rel);
				});
	}

	/**
	 * Returns the added objects keyed up by their relation types.
	 *
	 * @return
	 */
	public Map<HalLinkRelation, Object> asMap() {
		return Collections.unmodifiableMap(embeddeds);
	}
}
