/*
 * Copyright 2017-2018 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public class HalFormsMessageConverterUnitTest {

	ObjectMapper mapper;
	HttpMessageConverter<Object> messageConverter;

	@Before
	public void setUp() {

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new Jackson2HalFormsModule());

		TypeConstrainedMappingJackson2HttpMessageConverter converter = new TypeConstrainedMappingJackson2HttpMessageConverter(
				ResourceSupport.class);
		converter.setObjectMapper(mapper);

		this.messageConverter = converter;
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

		assertThat(halFormsDocument.getLinks()).hasSize(2);
		assertThat(halFormsDocument.getLinks()).extracting(Link::getHref).containsExactly("/employees", "/employees/1");

		assertThat(halFormsDocument.getTemplates().size(), is(1));
		assertThat(halFormsDocument.getTemplates().keySet(), hasItems("default"));
		assertThat(halFormsDocument.getTemplates().get("default").getContentType(), is("application/hal+json"));
		assertThat(halFormsDocument.getTemplates().get("default").getHttpMethod(), is(HttpMethod.GET));
		assertThat(halFormsDocument.getTemplates().get("default").getMethod(), is(HttpMethod.GET.toString().toLowerCase()));
	}

	@Test
	@SuppressWarnings("rawtypes")
	public void canWriteAHalFormsDocumentMessage() throws IOException {

		HalFormsProperty property = HalFormsProperty.named("my-name")//
				.withReadOnly(true) //
				.withValue("my-value") //
				.withPrompt("my-prompt") //
				.withRegex("my-regex") //
				.withRequired(true);

		HalFormsTemplate template = HalFormsTemplate.forMethod(HttpMethod.GET) //
				.withTitle("HAL-FORMS unit test") //
				.andContentType(MediaTypes.HAL_JSON) //
				.andProperty(property); //

		HalFormsDocument expected = HalFormsDocument.empty() //
				.andLink(new Link("/employees").withRel("collection")) //
				.andLink(new Link("/employees/1").withSelfRel())//
				.andTemplate("foo", template);

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		HttpOutputMessage convertedMessage = new HttpOutputMessage() {
			@Override
			public OutputStream getBody() {
				return stream;
			}

			@Override
			public HttpHeaders getHeaders() {
				return new HttpHeaders();
			}
		};

		this.messageConverter.write(expected, MediaTypes.HAL_FORMS_JSON, convertedMessage);

		assertThat(this.mapper.readValue(stream.toString(), HalFormsDocument.class), is(expected));
	}
}
