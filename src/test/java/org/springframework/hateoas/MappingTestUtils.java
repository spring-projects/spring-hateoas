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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
}
