/*
 * Copyright 2015-2021 the original author or authors.
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
final class CollectionJsonItem<T> {

	private @Nullable final String href;
	private final List<CollectionJsonData> data;
	private @JsonInclude(Include.NON_EMPTY) final Links links;
	private @Nullable final T rawData;

	/**
	 * Simple scalar types that can be encoded by value, not type.
	 */
	private final static Set<Class<?>> PRIMITIVE_TYPES = Collections.singleton(String.class);

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

	CollectionJsonItem(String href, List<CollectionJsonData> data, Links links, T rawData) {

		this.href = href;
		this.data = data;
		this.links = links;
		this.rawData = rawData;
	}

	/**
	 * Create new {@link CollectionJsonItem} by copying attributes and replacing the {@link Link}s.
	 *
	 * @param links
	 * @return
	 */
	CollectionJsonItem<T> withLinks(Link... links) {
		return new CollectionJsonItem<>(this.href, this.data, Links.of(links), this.rawData);
	}

	/**
	 * Create new {@link CollectionJsonItem} by copying attributes and replacing the {@link Links}.
	 * 
	 * @param links
	 * @return
	 */
	CollectionJsonItem<T> withLinks(Links links) {
		return this.links == links ? this : new CollectionJsonItem<T>(this.href, this.data, links, this.rawData);
	}

	/**
	 * Create new {@link CollectionJsonItem} by copying attributes and replacing the {@literal links} with a
	 * {@literal self} link.
	 * 
	 * @return
	 */
	CollectionJsonItem<T> withOwnSelfLink() {

		String href = this.href;

		if (href == null) {
			return this;
		}

		return withLinks(Links.of(Link.of(href)).merge(MergeMode.SKIP_BY_REL, links));
	}

	/**
	 * Create new {@link CollectionJsonItem} by copying attributes and replacing the {@literal href}.
	 *
	 * @param href
	 * @return
	 */
	CollectionJsonItem<T> withHref(@Nullable String href) {
		return this.href == href ? this : new CollectionJsonItem<T>(href, this.data, this.links, this.rawData);
	}

	/**
	 * Create new {@link CollectionJsonItem} by copying attributes and replacing the {@literal data}.
	 *
	 * @param data
	 * @return
	 */
	CollectionJsonItem<T> withData(List<CollectionJsonData> data) {
		return this.data == data ? this : new CollectionJsonItem<T>(this.href, data, this.links, this.rawData);
	}

	/**
	 * Create new {@link CollectionJsonItem} by copying attributes and replacing the {@literal rawData}.
	 *
	 * @param rawData
	 * @return
	 */
	CollectionJsonItem<T> withRawData(@Nullable T rawData) {
		return this.rawData == rawData ? this : new CollectionJsonItem<T>(this.href, this.data, this.links, rawData);
	}

	@JsonProperty
	@Nullable
	String getHref() {
		return this.href;
	}

	/**
	 * Transform a domain object into a collection of {@link CollectionJsonData} objects to serialize properly.
	 *
	 * @return
	 */
	@JsonProperty
	List<CollectionJsonData> getData() {

		if (!this.data.isEmpty()) {
			return this.data;
		}

		if (this.rawData != null && PRIMITIVE_TYPES.contains(this.rawData.getClass())) {
			return Collections.singletonList(new CollectionJsonData().withValue(this.rawData));
		}

		if (this.rawData == null) {
			return Collections.emptyList();
		}

		return PropertyUtils.extractPropertyValues(this.rawData).entrySet().stream() //
				.map(entry -> new CollectionJsonData() //
						.withName(entry.getKey()) //
						.withValue(entry.getValue())) //
				.collect(Collectors.toList());
	}

	@JsonProperty
	Links getLinks() {
		return this.links;
	}

	@Nullable
	@JsonIgnore
	T getRawData() {
		return this.rawData;
	}

	/**
	 * Generate an object using the deserialized properties and the provided type from the deserializer.
	 *
	 * @param javaType - type of the object to create
	 * @return
	 */
	@Nullable
	Object toRawData(JavaType javaType) {

		if (this.data.isEmpty()) {
			return null;
		}

		if (PRIMITIVE_TYPES.contains(javaType.getRawClass())) {
			return this.data.get(0).getValue();
		}

		return PropertyUtils.createObjectFromProperties(javaType.getRawClass(), //
				this.data.stream() //
						.collect(Collectors.toMap(CollectionJsonData::getName, CollectionJsonData::getValue)));
	}

	@Override
	public boolean equals(Object o) {

		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CollectionJsonItem<?> that = (CollectionJsonItem<?>) o;
		return Objects.equals(this.href, that.href) && Objects.equals(this.data, that.data)
				&& Objects.equals(this.links, that.links) && Objects.equals(this.rawData, that.rawData);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.href, this.data, this.links, this.rawData);
	}

	public String toString() {

		return "CollectionJsonItem(href=" + this.href + ", data=" + this.data + ", links=" + this.links + ", rawData="
				+ this.rawData + ")";
	}

}
