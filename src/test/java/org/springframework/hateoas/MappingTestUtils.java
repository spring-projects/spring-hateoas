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
package org.springframework.hateoas;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.*;
import static org.assertj.core.api.Assertions.*;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.json.JsonMapper.Builder;
import tools.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.assertj.core.api.AbstractStringAssert;
import org.jspecify.annotations.Nullable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper.DeserializationAssertions.WithDeserialization;
import org.springframework.lang.CheckReturnValue;
import org.springframework.util.function.ThrowingFunction;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

/**
 * @author Oliver Drotbohm
 */
public class MappingTestUtils {

	public static JsonMapper defaultJsonMapper() {
		return defaultMapper(UnaryOperator.identity());
	}

	public static JsonMapper defaultMapper(Function<Builder, Builder> consumer) {

		var mapper = JsonMapper.builder()
				.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
				.build();

		return consumer.apply(mapper.rebuild()).build();
	}

	public static ContextualMapper createMapper() {
		return createMapper(detectCaller());
	}

	private static final Collection<String> SUFFIXES = Set.of("Test", "Tests");

	private static Class<?> detectCaller() {

		return StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE)
				.walk(it -> it.map(StackFrame::getDeclaringClass)
						.filter(type -> SUFFIXES.stream().anyMatch(suffix -> type.getName().endsWith(suffix)))
						.findFirst())
				.orElseThrow();
	}

	public static ContextualMapper createMapper(Function<Builder, Builder> configurer) {
		return createMapper(detectCaller(), defaultMapper(configurer));
	}

	public static ContextualMapper createMapper(JsonMapper mapper) {
		return ContextualMapper.of(detectCaller(), mapper);
	}

	public static ContextualMapper createMapper(Class<?> context) {
		return createMapper(context, Function.identity());
	}

	private static ContextualMapper createMapper(Class<?> context, Function<Builder, Builder> configurer) {
		return createMapper(context, defaultMapper(configurer));
	}

	private static ContextualMapper createMapper(Class<?> context, JsonMapper mapper) {
		return ContextualMapper.of(context, mapper);
	}

	@RequiredArgsConstructor(staticName = "of")
	public static class ContextualMapper {

		private final Class<?> context;
		private final JsonMapper mapper;

		public JavaType getGenericType(Class<?> type, Class<?>... elements) {
			return mapper.getTypeFactory().constructParametricType(type, elements);
		}

		public JavaType getGenericType(Class<?> type, JavaType element) {
			return mapper.getTypeFactory().constructParametricType(type, element);
		}

		public String writeObject(Object source) {
			return mapper.writeValueAsString(source);
		}

		public RepresentationModel<?> readObject(String serialized) {
			return mapper.readValue(serialized, RepresentationModel.class);
		}

		public <T> T readObject(String serialized, Class<T> type) {
			return mapper.readValue(serialized, type);
		}

		public RepresentationModel<?> readFile(String filename) {
			return readFile(filename, RepresentationModel.class);
		}

		public <T> T readFile(String filename, Class<T> type) {

			var factory = mapper.getTypeFactory();
			var javaType = factory.constructType(type);

			return readFile(filename, javaType);
		}

		public <S> S readFile(String filename, Class<?> type, Class<?> elementType) {

			TypeFactory factory = mapper.getTypeFactory();
			JavaType javaType = factory.constructParametricType(type, elementType);

			return readFile(filename, javaType);
		}

		public <S> EntityModel<S> readFileIntoEntityModel(String filename, Class<S> type) {
			return readFile(filename, EntityModel.class, type);
		}

		public <S> CollectionModel<EntityModel<S>> readFileIntoEntityCollectionModel(String filename, Class<S> type) {
			return readFile(filename, CollectionModel.class, EntityModel.class, type);
		}

		public <S> PagedModel<EntityModel<S>> readFileIntoEntityPagedModel(String filename, Class<S> type) {
			return readFile(filename, PagedModel.class, EntityModel.class, type);
		}

		public <S> EntityModel<S> readEntityModel(String json, Class<S> type) {
			return readObject(json, EntityModel.class, type);
		}

		public <S> CollectionModel<EntityModel<S>> readEntityCollectionModel(String json, Class<S> type) {
			return readObject(json, CollectionModel.class, EntityModel.class, type);
		}

		public <S> PagedModel<EntityModel<S>> readEntityPagedModel(String json, Class<S> type) {
			return readObject(json, PagedModel.class, EntityModel.class, type);
		}

		public <S> S readObject(String source, Class<?> type, Class<?> elementType) {
			return mapper.readValue(source, createType(type, elementType));
		}

		public <S> S readObject(String source, Class<?> type, Class<?> elementType, Class<?> nested) {
			return mapper.readValue(source, createType(type, elementType, nested));
		}

		public <S> S readFile(String filename, Class<?> type, Class<?> elementType, Class<?> nested) {
			return readFile(filename, createType(type, elementType, nested));
		}

		private JavaType createType(Class<?> type, Class<?> elementType) {

			var factory = mapper.getTypeFactory();

			return factory.constructParametricType(type, elementType);
		}

		private JavaType createType(Class<?> type, Class<?> elementType, Class<?> nested) {

			var factory = mapper.getTypeFactory();
			var genericElement = factory.constructParametricType(elementType, nested);

			return factory.constructParametricType(type, genericElement);
		}

		public <S> S readFile(String filename, JavaType type) {

			ClassPathResource resource = new ClassPathResource(filename, context);

			try (InputStream stream = resource.getInputStream()) {

				return mapper.readValue(stream, type);

			} catch (IOException o_O) {
				throw new RuntimeException(o_O);
			}
		}

		public String readFileContent(String filename) {

			ClassPathResource resource = new ClassPathResource(filename, context);

			try (Scanner scanner = new Scanner(resource.getInputStream())) {

				StringBuilder builder = new StringBuilder();

				while (scanner.hasNextLine()) {

					builder.append(scanner.nextLine());

					if (scanner.hasNextLine()) {
						builder.append(System.lineSeparator());
					}
				}

				return builder.toString();
			} catch (IOException o_O) {
				throw new RuntimeException(o_O);
			}
		}

		@CheckReturnValue
		public <T> SerializationAssertions<T> assertSerializes(T source) {
			return new SerializationAssertions<>(source, mapper.writeValueAsString(source));
		}

		public void assertSerializesTo(Object source, String json) {
			assertThatJson(writeObject(source)).isEqualTo(json);
		}

		public AbstractStringAssert<?> assertFileContent(String filename) {
			return assertThat(readFileContent(filename));
		}

		public DeserializationAssertions assertDeserializes(String source) {
			return new DeserializationAssertions(source, null);
		}

		public DeserializationAssertions assertDeserializes(String source, Class<?> type, Class<?> elementType) {
			return new DeserializationAssertions(source, getGenericType(type, elementType));
		}

		public DeserializationAssertions assertDeserializesFile(String filename) {
			return new DeserializationAssertions(readFileContent(filename), null);
		}

		public DeserializationAssertions assertDeserializesFile(String filename, Class<?> type, Class<?> elementType) {
			return new DeserializationAssertions(readFileContent(filename), getGenericType(type, elementType));
		}

		public DeserializationAssertions assertDeserializesFile(String filename, Class<?> type, Class<?> elementType,
				Class<?> nested) {
			return new DeserializationAssertions(readFileContent(filename), getGenericType(type, elementType, nested));
		}

		@RequiredArgsConstructor
		public class ContentAssertions {

			private final String content;

			public void matches(Consumer<? super String> assertions) {
				assertThat(content).satisfies(assertions);
			}
		}

		@RequiredArgsConstructor
		public class SerializationAssertions<T> {

			private final T source;
			private final String result;

			public SerializationAssertions<T> into(String json) {

				assertThatJson(result).isEqualTo(json);

				return this;
			}

			public SerializationAssertions<T> into(Consumer<DocumentContext> assertions) {

				assertions.accept(JsonPath.parse(result));

				return this;
			}

			public SerializationAssertions<T> intoContentOf(String filename) {

				assertThatJson(result).isEqualTo(readFileContent(filename));

				return this;
			}

			public DeserializationAssertions map(ThrowingFunction<String, String> assertions) {
				return assertDeserializes(assertions.apply(result));
			}

			public WithDeserialization<T> andBack() {

				assertDeserializes(result).into(source);

				return new WithDeserialization<T>(source);
			}

			public <S> WithDeserialization<S> andBack(ParameterizedTypeReference<S> type) {
				return assertDeserializes(result).into(type);
			}

			public void andBack(Consumer<? super T> assertions) {

				Class<T> type = (Class<T>) source.getClass();

				assertDeserializes(result).into(type).matching(assertions);
			}

			public void andBack(Class<?> elementType, Class<?> nested) {

				var genericElement = getGenericType(elementType, nested);
				var type = getGenericType(source.getClass(), genericElement);

				assertDeserializes(result).into(type);
			}

			public void andBack(Class<?> elementType) {

				var type = getGenericType(source.getClass(), elementType);

				assertDeserializes(result).into(type);
			}
		}

		@RequiredArgsConstructor
		public class DeserializationAssertions {

			private final String source;
			private final @Nullable JavaType type;

			public void into(String expected) {
				assertThat(source).isEqualTo(expected);
			}

			public void into(@Nullable Object expected) {
				into(expected, type != null ? type : mapper.getTypeFactory().constructType(expected.getClass()));
			}

			@CheckReturnValue
			public <T> WithDeserialization<T> into(Class<T> type) {

				var target = mapper.getTypeFactory().constructType(type);

				return new WithDeserialization<T>(mapper.readValue(source, target));
			}

			@CheckReturnValue
			public <T> WithDeserialization<T> into(ParameterizedTypeReference<T> type) {

				var target = mapper.getTypeFactory().constructType(type.getType());

				return new WithDeserialization<T>(mapper.readValue(source, target));
			}

			public void into(Object expected, Class<?> elementType) {
				into(expected, getGenericType(expected.getClass(), elementType));
			}

			public void into(Object expected, Class<?> elementType, Class<?> nested) {
				into(expected, getGenericType(expected.getClass(), getGenericType(elementType, nested)));
			}

			private void into(@Nullable Object expected, JavaType type) {
				assertThat(mapper.<Object> readValue(source, type)).isEqualTo(expected);
			}

			private <T> void into(JavaType type) {
				assertThat(mapper.<Object> readValue(source, type)).isNotNull();
			}

			public <T> WithDeserialization<EntityModel<T>> intoEntityModel(Class<T> type) {

				EntityModel<T> result = readObject(source, EntityModel.class, type);

				assertThat(result).isNotNull();

				return new WithDeserialization<>(result);
			}

			public <T> WithDeserialization<CollectionModel<T>> intoCollectionModel(Class<T> type) {

				CollectionModel<T> result = readObject(source, CollectionModel.class, type);

				assertThat(result).isNotNull();

				return new WithDeserialization<>(result);
			}

			public <T> WithDeserialization<CollectionModel<EntityModel<T>>> intoCollectionEntityModel(Class<T> type) {

				CollectionModel<EntityModel<T>> result = readObject(source, CollectionModel.class, EntityModel.class, type);

				assertThat(result).isNotNull();

				return new WithDeserialization<>(result);
			}

			public <T> WithDeserialization<PagedModel<EntityModel<T>>> intoPagedEntityModel(Class<T> type) {

				PagedModel<EntityModel<T>> result = readObject(source, PagedModel.class, EntityModel.class, type);

				assertThat(result).isNotNull();

				return new WithDeserialization<>(result);
			}

			@RequiredArgsConstructor
			public static class WithDeserialization<T> {

				private final T result;

				public void matching(Consumer<? super T> assertions) {
					assertThat(result).satisfies(assertions);
				}
			}
		}
	}
}
