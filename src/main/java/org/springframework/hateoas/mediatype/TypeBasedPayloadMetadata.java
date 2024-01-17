/*
 * Copyright 2019-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.Named;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * {@link InputPayloadMetadata} implementation based on a Java type.
 *
 * @author Oliver Drotbohm
 */
class TypeBasedPayloadMetadata implements InputPayloadMetadata {

	private final Class<?> type;
	private final SortedMap<String, PropertyMetadata> properties;
	private final List<MediaType> mediaTypes;

	TypeBasedPayloadMetadata(Class<?> type, Stream<PropertyMetadata> properties) {
		this(type, new TreeMap<>(
				properties.collect(Collectors.toMap(PropertyMetadata::getName, Function.identity()))), Collections.emptyList());
	}

	TypeBasedPayloadMetadata(Class<?> type, SortedMap<String, PropertyMetadata> properties,
			List<MediaType> mediaTypes) {

		Assert.notNull(type, "Type must not be null!");
		Assert.notNull(properties, "Properties must not be null!");
		Assert.notNull(mediaTypes, "Media types must not be null!");

		this.type = type;
		this.properties = properties;
		this.mediaTypes = mediaTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel.PayloadMetadata#customize(org.springframework.hateoas.AffordanceModel.Named, java.util.function.Function)
	 */
	@Override
	public <T extends Named> T customize(T target, Function<PropertyMetadata, T> customizer) {

		PropertyMetadata metadata = this.properties.get(target.getName());

		return metadata == null ? target : customizer.apply(metadata);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.mediatype.PayloadMetadata#stream()
	 */
	@Override
	public Stream<PropertyMetadata> stream() {
		return properties.values().stream();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.MessageSourceResolvable#getCodes()
	 */
	@Override
	public List<String> getI18nCodes() {
		return Arrays.asList(type.getName(), type.getSimpleName());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel.PayloadMetadata#getType()
	 */
	@NonNull
	public Class<?> getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel.InputPayloadMetadata#withMediaTypes(java.util.List)
	 */
	@Override
	public InputPayloadMetadata withMediaTypes(List<MediaType> mediaTypes) {
		return new TypeBasedPayloadMetadata(type, properties, mediaTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModel.InputPayloadMetadata#getMediaTypes()
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return mediaTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof TypeBasedPayloadMetadata)) {
			return false;
		}

		TypeBasedPayloadMetadata that = (TypeBasedPayloadMetadata) obj;

		return this.type.equals(that.type)
				&& this.properties.equals(that.properties)
				&& this.mediaTypes.equals(that.mediaTypes);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = 31;

		result += 17 * type.hashCode();
		result += 17 * properties.hashCode();
		result += 17 * mediaTypes.hashCode();

		return result;
	}
}
