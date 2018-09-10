/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.support.PropertyUtils;

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
@Wither(AccessLevel.PACKAGE)
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class CollectionJsonItem<T> {

	private String href;
	private List<CollectionJsonData> data;

	@JsonInclude(Include.NON_EMPTY)
	private List<Link> links;

	@Getter(onMethod = @__({@JsonIgnore}), value = AccessLevel.PRIVATE)
	private T rawData;

	@JsonCreator
	CollectionJsonItem(@JsonProperty("href") String href, @JsonProperty("data") List<CollectionJsonData> data,
					   @JsonProperty("links") List<Link> links) {

		this.href = href;
		this.data = data;
		this.links = links;
		this.rawData = null;
	}

	CollectionJsonItem() {
		this(null, null, null);
	}

	/**
	 * Simple scalar types that can be encoded by value, not type.
	 */
	private final static HashSet<Class<?>> PRIMITIVE_TYPES = new HashSet<Class<?>>() {{
		add(String.class);
	}};

	/**
	 * Transform a domain object into a collection of {@link CollectionJsonData} objects to serialize properly.
	 *
	 * @return
	 */
	public List<CollectionJsonData> getData() {

		if (this.data != null) {
			return this.data;
		}

		if (PRIMITIVE_TYPES.contains(this.rawData.getClass())) {
			return Collections.singletonList(new CollectionJsonData().withValue(this.rawData));
		}

		return PropertyUtils.findProperties(this.rawData).entrySet().stream()
			.map(entry -> new CollectionJsonData()
				.withName(entry.getKey())
				.withValue(entry.getValue()))
			.collect(Collectors.toList());
	}

	/**
	 * Generate an object used the deserialized properties and the provided type from the deserializer.
	 * 
	 * @param javaType - type of the object to create
	 * @return
	 */
	public Object toRawData(JavaType javaType) {

		if (PRIMITIVE_TYPES.contains(javaType.getRawClass())) {
			return this.data.get(0).getValue();
		}

		return PropertyUtils.createObjectFromProperties(javaType.getRawClass(), //
			this.data.stream()
				.collect(Collectors.toMap(
					CollectionJsonData::getName,
					CollectionJsonData::getValue)));
	}
}
