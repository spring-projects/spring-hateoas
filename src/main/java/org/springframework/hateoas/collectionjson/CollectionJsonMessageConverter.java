/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.collectionjson;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *  A message converter that converts any object into a Collection+JSON document before bundling up as an
 *  {@link HttpOutputMessage}, or that converts any incoming {@link HttpInputMessage} into an object.
 *
 * @author Greg Turnquist
 */
public class CollectionJsonMessageConverter extends AbstractHttpMessageConverter<Object> {

	private final ObjectMapper objectMapper;

	public CollectionJsonMessageConverter(ObjectMapper objectMapper) {

		this.objectMapper = objectMapper;
		this.objectMapper.registerModule(new Jackson2CollectionJsonModule());

		setSupportedMediaTypes(Arrays.asList(MediaTypes.COLLECTION_JSON));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
		throws IOException, HttpMessageNotReadableException {
		
		return this.objectMapper.readValue(inputMessage.getBody(), clazz);
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage)
		throws IOException, HttpMessageNotWritableException {

		JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputMessage.getBody(), JsonEncoding.UTF8);

		// A workaround for JsonGenerators not applying serialization features
		// https://github.com/FasterXML/jackson-databind/issues/12
		if (objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
			jsonGenerator.useDefaultPrettyPrinter();
		}

		try {
			objectMapper.writeValue(jsonGenerator, t);
		} catch (JsonProcessingException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		}

	}
}
