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

import static org.assertj.core.api.Assertions.*;

import lombok.RequiredArgsConstructor;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonWriteFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.springframework.core.io.ClassPathResource;

/**
 * @author Oliver Drotbohm
 */
public class MappingTestUtils {

	public static ObjectMapper defaultObjectMapper() {
		return defaultObjectMapper(UnaryOperator.identity());
	}

	public static ObjectMapper defaultObjectMapper(
			Function<MapperBuilder<ObjectMapper, ?>, MapperBuilder<ObjectMapper, ?>> consumer) {

		ObjectMapper mapper = JsonMapper.builder()
				.enable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
				.disable(JsonWriteFeature.ESCAPE_FORWARD_SLASHES)
				.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
				.build();

		return consumer.apply(mapper.rebuild()).build();
	}

	public static ContextualMapper createMapper(Class<?> context) {
		return createMapper(context, Function.identity());
	}

	public static ContextualMapper createMapper(Class<?> context,
			Function<MapperBuilder<ObjectMapper, ?>, MapperBuilder<ObjectMapper, ?>> configurer) {

		return ContextualMapper.of(context, defaultObjectMapper(configurer));
	}

	@RequiredArgsConstructor(staticName = "of")
	public static class ContextualMapper {

		private final Class<?> context;
		private final ObjectMapper mapper;

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

		public <S> EntityModel<S> readEntityModel(String filename, Class<S> type) {
			return readFile(filename, EntityModel.class, type);
		}

		public <S> CollectionModel<EntityModel<S>> readEntityCollectionModel(String filename, Class<S> type) {
			return readFile(filename, CollectionModel.class, EntityModel.class, type);
		}

		public <S> PagedModel<EntityModel<S>> readEntityPagedModel(String filename, Class<S> type) {
			return readFile(filename, PagedModel.class, EntityModel.class, type);
		}

		public <S> S readFile(String filename, Class<?> type, Class<?> elementType, Class<?> nested) {

			TypeFactory factory = mapper.getTypeFactory();

			JavaType genericElement = factory.constructParametricType(elementType, nested);
			JavaType javaType = factory.constructParametricType(type, genericElement);

			return readFile(filename, javaType);
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

		public void assertSerializesTo(Object source, String filename) {
			assertThat(writeObject(source)).isEqualTo(readFileContent(filename));
		}
	}
}
