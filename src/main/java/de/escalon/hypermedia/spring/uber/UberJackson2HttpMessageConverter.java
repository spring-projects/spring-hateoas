/*
 * Copyright (c) 2015. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring.uber;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.escalon.hypermedia.spring.HypermediaTypes;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;

public class UberJackson2HttpMessageConverter extends AbstractHttpMessageConverter<Object> {

	private ObjectMapper objectMapper = new ObjectMapper();
	private Boolean prettyPrint;

	public UberJackson2HttpMessageConverter() {
		super(HypermediaTypes.UBER_JSON);
		objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		final boolean ret;
		if (ResourceSupport.class.isAssignableFrom(clazz)
				|| Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz)) {
			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}

	@Override
	protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		// TODO read uber data
		return null;
	}

	@Override
	protected void writeInternal(Object t, HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException {

		UberMessageModel uberModel = new UberMessageModel(t);
		JsonEncoding encoding = getJsonEncoding(outputMessage.getHeaders().getContentType());
		JsonGenerator jsonGenerator = this.objectMapper.getFactory().createGenerator(outputMessage.getBody(), encoding);

		// A workaround for JsonGenerators not applying serialization features
		// https://github.com/FasterXML/jackson-databind/issues/12
		if (this.objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
			jsonGenerator.useDefaultPrettyPrinter();
		}

		try {
			this.objectMapper.writeValue(jsonGenerator, uberModel);
		} catch (JsonProcessingException ex) {
			throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
		}

	}

	/**
	 * Determine the JSON encoding to use for the given content type.
	 *
	 * @param contentType the media type as requested by the caller
	 * @return the JSON encoding to use (never {@code null})
	 */
	protected JsonEncoding getJsonEncoding(MediaType contentType) {
		if (contentType != null && contentType.getCharSet() != null) {
			Charset charset = contentType.getCharSet();
			for (JsonEncoding encoding : JsonEncoding.values()) {
				if (charset.name().equals(encoding.getJavaName())) {
					return encoding;
				}
			}
		}
		return JsonEncoding.UTF8;
	}

	/**
	 * Set the {@code ObjectMapper} for this view. If not set, a default {@link com.fasterxml.jackson.databind.ObjectMapper#ObjectMapper() ObjectMapper}
	 * is used.
	 * <p>
	 * Setting a custom-configured {@code ObjectMapper} is one way to take further control of the JSON serialization
	 * process. For example, an extended {@link com.fasterxml.jackson.databind.ser.SerializerFactory} can be configured that
	 * provides custom serializers for specific types. The other option for refining the serialization process is to use
	 * Jackson's provided annotations on the types to be serialized, in which case a custom-configured ObjectMapper is
	 * unnecessary.
     * @param objectMapper used for json mapping
	 */
	public void setObjectMapper(ObjectMapper objectMapper) {
		Assert.notNull(objectMapper, "ObjectMapper must not be null");
		this.objectMapper = objectMapper;
		configurePrettyPrint();
	}

	private void configurePrettyPrint() {
		if (this.prettyPrint != null) {
			this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, this.prettyPrint);
		}
	}

}
