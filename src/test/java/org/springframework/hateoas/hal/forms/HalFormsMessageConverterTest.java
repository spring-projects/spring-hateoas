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
package org.springframework.hateoas.hal.forms;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.*;
import static org.springframework.hateoas.hal.forms.HalFormsDocument.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Greg Turnquist
 */
public class HalFormsMessageConverterTest {

	ObjectMapper mapper;
	HttpMessageConverter<Object> messageConverter;

	@Before
	public void setUp() {

		this.mapper = new ObjectMapper();
		this.messageConverter = new HalFormsMessageConverter(this.mapper);
	}

	@Test
	public void verifyBasicAttributes() {

		assertThat(this.messageConverter.getSupportedMediaTypes(), hasItems(MediaTypes.HAL_FORMS_JSON));
		assertThat(this.messageConverter.canRead(HalFormsDocument.class, MediaTypes.HAL_FORMS_JSON), is(true));
		assertThat(this.messageConverter.canWrite(HalFormsDocument.class, MediaTypes.HAL_FORMS_JSON), is(true));
	}

	@Test
	public void canReadAHalFormsDocumentMessage() throws IOException {

		HttpInputMessage message = new HttpInputMessage() {
			@Override
			public InputStream getBody() throws IOException {
				return new ClassPathResource("reference.json", getClass()).getInputStream();
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}
		};

		Object convertedMessage = this.messageConverter.read(HalFormsDocument.class, message);

		assertThat(convertedMessage, instanceOf(HalFormsDocument.class));

		HalFormsDocument<?> halFormsDocument = (HalFormsDocument<?>) convertedMessage;

		assertThat(halFormsDocument.getLinks().size(), is(2));
		assertThat(halFormsDocument.getLinks().get(0).getHref(), is("/employees"));
		assertThat(halFormsDocument.getLinks().get(1).getHref(), is("/employees/1"));

		assertThat(halFormsDocument.getTemplates().size(), is(1));
		assertThat(halFormsDocument.getTemplates().keySet(), hasItems("default"));
		assertThat(halFormsDocument.getTemplates().get("default").getContentType(), is("application/hal+json"));
		assertThat(halFormsDocument.getTemplates().get("default").getKey(), is(HalFormsTemplate.DEFAULT_KEY));
		assertThat(halFormsDocument.getTemplates().get("default").getHttpMethod(), is(HttpMethod.GET));
		assertThat(halFormsDocument.getTemplates().get("default").getMethod(), is(HttpMethod.GET.toString().toLowerCase()));
	}

	@Test
	public void canWriteAHalFormsDocumentMessage() throws IOException {

		HalFormsProperty property = new HalFormsProperty("my-name", true, "my-value", "my-prompt",
			"my-regex", false, true, false);
		HalFormsTemplate template = new HalFormsTemplate();
		template.setHttpMethod(HttpMethod.GET);
		template.setContentType(Collections.singletonList(MediaTypes.HAL_JSON));
		template.setTitle("HAL-FORMS unit test");
		template.getProperties().add(property);

		HalFormsDocument expected = halFormsDocument()
			.link(new Link("/employees").withRel("collection"))
			.link(new Link("/employees/1").withSelfRel())
			.template("foo", template)
			.build();


		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		HttpOutputMessage convertedMessage = new HttpOutputMessage() {
			@Override
			public OutputStream getBody() throws IOException {
				return stream;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}
		};

		this.messageConverter.write(expected, MediaTypes.HAL_FORMS_JSON, convertedMessage);

		String json = stream.toString();

		System.out.println(json);
		
		HalFormsDocument actual = this.mapper.readValue(json, HalFormsDocument.class);

		assertThat(actual, is(expected));
	}

}
