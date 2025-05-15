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

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.impl.UnknownSerializer;
import tools.jackson.databind.ser.jackson.JsonValueSerializer;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.databind.util.NameTransformer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * A simple {@link EntityModel} wrapping a domain object and adding links to it.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class EntityModel<T> extends RepresentationModel<EntityModel<T>> {

	private @Nullable T content;

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
	@JsonUnwrapped
	@JsonSerialize(using = MapSuppressingUnwrappingSerializer.class)
	public T getContent() {
		return Objects.requireNonNull(content);
	}

	// Hacks to allow deserialization into an EntityModel<Map<String, Object>>

	@JsonAnyGetter
	@SuppressWarnings("unchecked")
	private @Nullable Map<String, Object> getMapContent() {
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

		return super.equals(obj) && Objects.equals(this.content, that.content);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.RepresentationModel#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode() + Objects.hash(content);
	}

	private static class MapSuppressingUnwrappingSerializer extends StdSerializer<Object> {

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
		 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
		 */
		@Override
		@SuppressWarnings({ "unchecked" })
		public void serialize(@Nullable Object value, @Nullable JsonGenerator gen,
				@Nullable SerializationContext provider) {

			if (value == null || Map.class.isInstance(value) || provider == null) {
				return;
			}

			var serializer = provider.findValueSerializer(value.getClass());

			if (UnknownSerializer.class.isInstance(serializer)
					&& !provider.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)) {
				return;
			}

			if (JsonValueSerializer.class.isInstance(serializer)) {
				throw new IllegalStateException(
						"@JsonValue rendered classes can not be directly nested in EntityModel as they do not produce a document key!");
			}

			serializer = (ValueSerializer<Object>) serializer.createContextual(provider, property);

			serializer //
					.unwrappingSerializer(NameTransformer.NOP) //
					.serialize(value, gen, provider);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#createContextual(tools.jackson.databind.SerializationContext, tools.jackson.databind.BeanProperty)
		 */
		@Override
		public ValueSerializer<?> createContextual(@Nullable SerializationContext prov, @Nullable BeanProperty property) {
			return new MapSuppressingUnwrappingSerializer(property);
		}

		/*
		 * (non-Javadoc)
		 * @see tools.jackson.databind.ValueSerializer#isUnwrappingSerializer()
		 */
		@Override
		public boolean isUnwrappingSerializer() {
			return true;
		}
	}
}
