/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Links.MergeMode;
import org.springframework.hateoas.mediatype.PropertyUtils;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JavaType;

/**
 * Representation of an "item" in a Collection+JSON document.
 *
 * @author Greg Turnquist
 */
@Value
@Getter(onMethod = @__(@JsonProperty))
@Wither(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class CollectionJsonItem<T> {

	private @Nullable String href;
	private List<CollectionJsonData> data;
	private @JsonInclude(Include.NON_EMPTY) Links links;
	private @Nullable @Getter(onMethod = @__({ @JsonIgnore }), value = AccessLevel.PRIVATE) T rawData;

	@JsonCreator
	CollectionJsonItem(@JsonProperty("href") @Nullable String href, //
			@JsonProperty("data") @Nullable List<CollectionJsonData> data, //
			@JsonProperty("links") @Nullable Links links) {

		this.href = href;
		this.data = data == null ? Collections.emptyList() : data;
		this.links = links == null ? Links.NONE : links;
		this.rawData = null;
	}

	CollectionJsonItem() {
		this(null, null, null);
	}

	/**
	 * Simple scalar types that can be encoded by value, not type.
	 */
	private final static Set<Class<?>> PRIMITIVE_TYPES = Collections.singleton(String.class);

	/**
	 * Transform a domain object into a collection of {@link CollectionJsonData} objects to serialize properly.
	 *
	 * @return
	 */
	@JsonProperty
	public List<CollectionJsonData> getData() {

		if (!this.data.isEmpty()) {
			return this.data;
		}

		if (this.rawData != null && PRIMITIVE_TYPES.contains(this.rawData.getClass())) {
			return Collections.singletonList(new CollectionJsonData().withValue(this.rawData));
		}

		return PropertyUtils.extractPropertyValues(this.rawData).entrySet().stream() //
				.map(entry -> new CollectionJsonData() //
						.withName(entry.getKey()) //
						.withValue(entry.getValue())) //
				.collect(Collectors.toList());
	}

	/**
	 * Generate an object used the deserialized properties and the provided type from the deserializer.
	 *
	 * @param javaType - type of the object to create
	 * @return
	 */
	@Nullable
	public Object toRawData(JavaType javaType) {

		if (this.data.isEmpty()) {
			return null;
		}

		if (PRIMITIVE_TYPES.contains(javaType.getRawClass())) {
			return this.data.get(0).getValue();
		}

		return PropertyUtils.createObjectFromProperties(javaType.getRawClass(), //
				this.data.stream().collect(Collectors.toMap(CollectionJsonData::getName, CollectionJsonData::getValue)));
	}

	public CollectionJsonItem<T> withLinks(Link... links) {
		return new CollectionJsonItem<>(href, data, Links.of(links), rawData);
	}

	public CollectionJsonItem<T> withLinks(Links links) {
		return new CollectionJsonItem<>(href, data, links, rawData);
	}

	public CollectionJsonItem<T> withOwnSelfLink() {

		String href = this.href;

		if (href == null) {
			return this;
		}

		return withLinks(Links.of(new Link(href)).merge(MergeMode.SKIP_BY_REL, links));
	}
}
