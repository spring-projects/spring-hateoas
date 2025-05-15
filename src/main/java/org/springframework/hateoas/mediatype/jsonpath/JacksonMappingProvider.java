/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.jsonpath;

import tools.jackson.databind.json.JsonMapper;

import org.jspecify.annotations.Nullable;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.mapper.MappingException;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

public class JacksonMappingProvider implements MappingProvider {

	private static final JsonMapper DEFAULT_MAPPER = new JsonMapper();

	private final JsonMapper mapper;

	public JacksonMappingProvider() {
		this(DEFAULT_MAPPER);
	}

	public JacksonMappingProvider(JsonMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public <T> @Nullable T map(Object source, Class<T> targetType, Configuration configuration) {

		if (source == null) {
			return null;
		}

		try {
			return mapper.convertValue(source, targetType);
		} catch (Exception e) {
			throw new MappingException(e);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> @Nullable T map(Object source, final TypeRef<T> targetType,
			Configuration configuration) {

		if (source == null) {
			return null;
		}

		var type = mapper.getTypeFactory().constructType(targetType.getType());

		try {
			return (T) mapper.convertValue(source, type);
		} catch (Exception e) {
			throw new MappingException(e);
		}
	}
}
