/*
 * Copyright 2019-2020 the original author or authors.
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

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.function.Consumer;

import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Oliver Drotbohm
 */
public class MappingTestUtils {

	public static ObjectMapper defaultObjectMapper() {

		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		// Disable auto-detection to make sure our model classes work in that scenario
		mapper.disable(MapperFeature.AUTO_DETECT_CREATORS) //
				.disable(MapperFeature.AUTO_DETECT_FIELDS) //
				.disable(MapperFeature.AUTO_DETECT_GETTERS) //
				.disable(MapperFeature.AUTO_DETECT_IS_GETTERS) //
				.disable(MapperFeature.AUTO_DETECT_SETTERS);

		return mapper;
	}

	public static ContextualMapper createMapper(Class<?> context, Consumer<ObjectMapper> configurer) {

		ObjectMapper mapper = defaultObjectMapper();
		configurer.accept(mapper);

		return ContextualMapper.of(context, mapper);
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

			try {
				return mapper.writeValueAsString(source);
			} catch (JsonProcessingException o_O) {
				throw new RuntimeException(o_O);
			}
		}

		public RepresentationModel<?> readObject(String filename) {
			return readObject(filename, RepresentationModel.class);
		}

		public <T> T readObject(String filename, Class<T> type) {

			TypeFactory factory = mapper.getTypeFactory();
			JavaType javaType = factory.constructType(type);

			return readObject(filename, javaType);
		}

		public <S> S readObject(String filename, Class<?> type, Class<?> elementType) {

			TypeFactory factory = mapper.getTypeFactory();
			JavaType javaType = factory.constructParametricType(type, elementType);

			return readObject(filename, javaType);
		}

		public <S> S readObject(String filename, JavaType type) {

			ClassPathResource resource = new ClassPathResource(filename, context);

			try (InputStream stream = resource.getInputStream()) {

				return mapper.readValue(stream, type);

			} catch (IOException o_O) {
				throw new RuntimeException(o_O);
			}
		}

		public String readFile(String filename) {

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

	}
}
