/*
 * Copyright 2012-2024 the original author or authors.
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
package org.springframework.hateoas;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.std.JsonValueSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;

/**
 * A simple {@link EntityModel} wrapping a domain object and adding links to it.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class EntityModel<T> extends RepresentationModel<EntityModel<T>> {

	private T content;

	/**
	 * Creates an empty {@link EntityModel}.
	 */
	protected EntityModel() {
		this.content = null;
	}

	protected EntityModel(T content) {
		this(content, Links.NONE);
	}

	/**
	 * Creates a new {@link EntityModel} with the given content and {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link EntityModel}.
	 */
	protected EntityModel(T content, Iterable<Link> links) {

		Assert.notNull(content, "Content must not be null!");
		Assert.isTrue(!(content instanceof Collection), "Content must not be a collection! Use CollectionModel instead!");

		this.content = content;
		this.add(links);
	}

	/**
	 * Creates a new {@link EntityModel} with the given content.
	 *
	 * @param content must not be {@literal null}.
	 * @return
	 * @since 1.1
	 */
	public static <T> EntityModel<T> of(T content) {
		return of(content, Collections.emptyList());
	}

	/**
	 * Creates a new {@link EntityModel} with the given content and {@link Link}s (optional).
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link EntityModel}.
	 * @return
	 * @since 1.1
	 */
	public static <T> EntityModel<T> of(T content, Link... links) {
		return of(content, Arrays.asList(links));
	}

	/**
	 * Creates a new {@link EntityModel} with the given content and {@link Link}s.
	 *
	 * @param content must not be {@literal null}.
	 * @param links the links to add to the {@link EntityModel}.
	 * @return
	 * @since 1.1
	 */
	public static <T> EntityModel<T> of(T content, Iterable<Link> links) {
		return new EntityModel<>(content, links);
	}

	/**
	 * Returns the underlying entity.
	 *
	 * @return the content
	 */
	@Nullable
	@JsonUnwrapped
	@JsonSerialize(using = MapSuppressingUnwrappingSerializer.class)
	public T getContent() {
		return content;
	}

	// Hacks to allow deserialization into an EntityModel<Map<String, Object>>

	@Nullable
	@JsonAnyGetter
	@SuppressWarnings("unchecked")
	private Map<String, Object> getMapContent() {
		return Map.class.isInstance(content) ? (Map<String, Object>) content : null;
	}

	@JsonAnySetter
	private void setPropertiesAsMap(String key, Object value) {
		getOrInitAsMap().put(key, value);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getOrInitAsMap() {

		if (this.content == null) {
			this.content = (T) new LinkedHashMap<>();
		} else {
			Assert.state(Map.class.isInstance(this.content), "Content is not a Map!");
		}

		return (Map<String, Object>) this.content;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#toString()
	 */
	@Override
	public String toString() {
		return String.format("EntityModel { content: %s, %s }", getContent(), super.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object obj) {

		if (this == obj) {
			return true;
		}

		if (obj == null || !obj.getClass().equals(getClass())) {
			return false;
		}

		EntityModel<?> that = (EntityModel<?>) obj;

		boolean contentEqual = this.content == null ? that.content == null : this.content.equals(that.content);
		return contentEqual && super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#hashCode()
	 */
	@Override
	public int hashCode() {

		int result = super.hashCode();
		result += content == null ? 0 : 17 * content.hashCode();
		return result;
	}

	private static class MapSuppressingUnwrappingSerializer extends StdSerializer<Object>
			implements ContextualSerializer {

		private static final long serialVersionUID = -8367255762553946324L;

		private final @Nullable BeanProperty property;

		@SuppressWarnings("unused")
		public MapSuppressingUnwrappingSerializer() {
			this(null);
		}

		private MapSuppressingUnwrappingSerializer(@Nullable BeanProperty property) {

			super(Object.class);

			this.property = property;
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		@SuppressWarnings({ "null", "unchecked" })
		public void serialize(@Nullable Object value, @Nullable JsonGenerator gen, @NonNull SerializerProvider provider)
				throws IOException {

			if (value == null || Map.class.isInstance(value)) {
				return;
			}

			JsonSerializer<Object> serializer = provider.findValueSerializer(value.getClass());

			if (UnknownSerializer.class.isInstance(serializer)
					&& !provider.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)) {
				return;
			}

			if (JsonValueSerializer.class.isInstance(serializer)) {
				throw new IllegalStateException(
						"@JsonValue rendered classes can not be directly nested in EntityModel as they do not produce a document key!");
			}

			if (ContextualSerializer.class.isInstance(serializer)) {
				serializer = (JsonSerializer<Object>) ((ContextualSerializer) serializer).createContextual(provider, property);
			}

			serializer //
					.unwrappingSerializer(NameTransformer.NOP) //
					.serialize(value, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.ser.ContextualSerializer#createContextual(com.fasterxml.jackson.databind.SerializerProvider, com.fasterxml.jackson.databind.BeanProperty)
		 */
		@Override
		public JsonSerializer<?> createContextual(@Nullable SerializerProvider prov, @Nullable BeanProperty property)
				throws JsonMappingException {
			return new MapSuppressingUnwrappingSerializer(property);
		}

		/*
		 * (non-Javadoc)
		 * @see com.fasterxml.jackson.databind.JsonSerializer#isUnwrappingSerializer()
		 */
		@Override
		public boolean isUnwrappingSerializer() {
			return true;
		}
	}
}
