/*
 * Copyright 2011-2026 the original author or authors.
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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import com.jayway.jsonpath.InvalidJsonException;
import com.jayway.jsonpath.spi.json.AbstractJsonProvider;

public class JacksonJsonProvider extends AbstractJsonProvider {

	private static final JsonMapper DEFAULT_MAPPER = new JsonMapper();
	private static final ObjectReader DEFAULT_READER = DEFAULT_MAPPER.reader().forType(Object.class);

	protected JsonMapper mapper;
	protected ObjectReader reader;

	public JsonMapper getMapper() {
		return mapper;
	}

	/**
	 * Initialize the JacksonProvider with the default JsonMapper and ObjectReader
	 */
	public JacksonJsonProvider() {
		this(DEFAULT_MAPPER, DEFAULT_READER);
	}

	/**
	 * Initialize the JacksonProvider with a custom JsonMapper.
	 *
	 * @param JsonMapper the JsonMapper to use
	 */
	public JacksonJsonProvider(JsonMapper JsonMapper) {
		this(JsonMapper, JsonMapper.reader().forType(Object.class));
	}

	/**
	 * Initialize the JacksonProvider with a custom JsonMapper and ObjectReader.
	 *
	 * @param JsonMapper the JsonMapper to use
	 * @param reader the ObjectReader to use
	 */
	public JacksonJsonProvider(JsonMapper JsonMapper, ObjectReader reader) {
		this.mapper = JsonMapper;
		this.reader = reader;
	}

	@Override
	public Object parse(String json) throws InvalidJsonException {
		try {
			return reader.readValue(json);
		} catch (JacksonException e) {
			throw new InvalidJsonException(e, json);
		}
	}

	@Override
	public Object parse(byte[] json) throws InvalidJsonException {
		try {
			return reader.readValue(json);
		} catch (JacksonException e) {
			throw new InvalidJsonException(e, new String(json, StandardCharsets.UTF_8));
		}
	}

	@Override
	public Object parse(InputStream jsonStream, String charset) throws InvalidJsonException {
		try {
			return reader.readValue(new InputStreamReader(jsonStream, charset));
		} catch (JacksonException | IOException e) {
			throw new InvalidJsonException(e);
		}
	}

	@Override
	public String toJson(Object obj) {
		return mapper.writeValueAsString(obj);
	}

	@Override
	public List<Object> createArray() {
		return new LinkedList<Object>();
	}

	@Override
	public Object createMap() {
		return new LinkedHashMap<String, Object>();
	}
}
