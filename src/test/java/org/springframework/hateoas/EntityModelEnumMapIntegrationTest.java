/*
 * Copyright 2026 the original author or authors.
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

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.mediatype.hal.HalTestUtils;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Regression tests for enum-keyed map serialization (GH-1990).
 */
class EntityModelEnumMapIntegrationTest {

	@Test
	void serializesEnumKeysInPlainMap() {

		var mapper = MappingTestUtils.defaultJsonMapper();
		var json = mapper.readTree(mapper.writeValueAsString(enumMap()));

		assertThat(json.get("K1").asText()).isEqualTo("1");
		assertThat(json.get("K2").asText()).isEqualTo("2");
	}

	@Test // GH-1990
	void serializesEnumKeysInEntityModelMap() {

		var mapper = MappingTestUtils.defaultJsonMapper();
		var model = EntityModel.of(enumMap()).add(Link.of("http://localhost/test"));
		var json = mapper.readTree(mapper.writeValueAsString(model));

		assertThat(json.get("K1").asText()).isEqualTo("1");
		assertThat(json.get("K2").asText()).isEqualTo("2");
		assertThat(json.get("links").get(0).get("href").asText()).isEqualTo("http://localhost/test");
	}

	@Test // GH-1990
	void serializesEnumMap() {

		var mapper = MappingTestUtils.defaultJsonMapper();
		var map = new EnumMap<>(enumMap());

		assertThat(mapper.readTree(mapper.writeValueAsString(EntityModel.of(map))))
				.isEqualTo(mapper.readTree("{\"K1\":\"1\",\"K2\":\"2\",\"links\":[]}"));
	}

	@Test // GH-1990
	void honorsCustomEnumKeySerializer() {

		var module = new SimpleModule();
		module.addKeySerializer(EnumKey.class, new ValueSerializer<EnumKey>() {
			@Override
			public void serialize(EnumKey value, JsonGenerator generator, SerializationContext context) {
				generator.writeName("custom-" + value.name());
			}
		});
		var mapper = MappingTestUtils.defaultMapper(builder -> builder.addModule(module));
		var json = mapper.readTree(mapper.writeValueAsString(EntityModel.of(enumMap())));

		assertThat(json).isEqualTo(mapper.readTree("{\"custom-K1\":\"1\",\"custom-K2\":\"2\",\"links\":[]}"));
	}

	@Test // GH-1990
	void honorsJsonValueOnEnumKeys() {

		var mapper = MappingTestUtils.defaultJsonMapper();
		var map = Map.of(JsonValueKey.KEY, "value");

		assertThat(mapper.readTree(mapper.writeValueAsString(EntityModel.of(map))))
				.isEqualTo(mapper.readTree("{\"external-key\":\"value\",\"links\":[]}"));
	}

	@Test // GH-1990
	void serializesEnumMapWithHalLinks() {

		var mapper = HalTestUtils.halMapper();
		var model = EntityModel.of(enumMap()).add(Link.of("http://localhost/test"));
		var json = mapper.readTree(mapper.writeValueAsString(model));

		assertThat(json.get("K1").asText()).isEqualTo("1");
		assertThat(json.get("K2").asText()).isEqualTo("2");
		assertThat(json.get("_links").get("self").get("href").asText()).isEqualTo("http://localhost/test");
	}

	@Test // GH-1990
	void preservesStringKeysAndNestedAndNullValues() {

		var mapper = MappingTestUtils.defaultJsonMapper();
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("nested", Map.of("count", 2));
		map.put("missing", null);

		assertThat(mapper.readTree(mapper.writeValueAsString(EntityModel.of(map))))
				.isEqualTo(mapper.readTree("{\"nested\":{\"count\":2},\"missing\":null,\"links\":[]}"));
	}

	@Test // GH-1990
	void preservesLinksForEmptyMap() {

		var mapper = MappingTestUtils.defaultJsonMapper();
		var model = EntityModel.of(Map.of()).add(Link.of("http://localhost/test"));
		var json = mapper.readTree(mapper.writeValueAsString(model));

		assertThat(json.size()).isEqualTo(1);
		assertThat(json.get("links").get(0).get("href").asText()).isEqualTo("http://localhost/test");
	}

	@Test // GH-1990
	void deserializesStringKeyedMapContent() {

		var mapper = MappingTestUtils.defaultJsonMapper();
		var type = mapper.getTypeFactory().constructParametricType(EntityModel.class, Map.class);
		EntityModel<Map<String, Object>> model = mapper.readValue("{\"name\":\"value\",\"count\":2}", type);

		assertThat(model.getContent()).containsExactlyInAnyOrderEntriesOf(Map.of("name", "value", "count", 2));
	}

	private static Map<EnumKey, String> enumMap() {

		Map<EnumKey, String> map = new LinkedHashMap<>();
		map.put(EnumKey.K1, "1");
		map.put(EnumKey.K2, "2");
		return map;
	}

	enum JsonValueKey {
		KEY;

		@JsonValue
		public String externalName() {
			return "external-key";
		}
	}

	enum EnumKey {
		K1, K2
	}
}
