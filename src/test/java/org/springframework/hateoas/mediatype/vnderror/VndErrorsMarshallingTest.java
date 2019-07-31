/*
 * Copyright 2013-2018 the original author or authors.
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
package org.springframework.hateoas.mediatype.vnderror;

import static org.assertj.core.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors.VndError;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Integration tests for marshalling of {@link VndErrors}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class VndErrorsMarshallingTest {

	ObjectMapper jackson2Mapper;

	LinkRelationProvider relProvider = new EvoInflectorLinkRelationProvider();

	VndErrors errors;
	String jsonReference;
	String json2Reference;

	public VndErrorsMarshallingTest() throws IOException {

		jsonReference = readFile(new ClassPathResource("vnderror.json"));
		json2Reference = readFile(new ClassPathResource("vnderror2.json"));
	}

	@BeforeEach
	void setUp() {

		jackson2Mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		jackson2Mapper.registerModule(new Jackson2HalModule());
		jackson2Mapper.setHandlerInstantiator(
				new Jackson2HalModule.HalHandlerInstantiator(relProvider, CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY));
		jackson2Mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		VndError error = new VndError("42", "Validation failed!", //
				new Link("http://...", "describes"), new Link("http://...", "help"));
		errors = new VndErrors(error, error, error);
	}

	/**
	 * @see #62
	 */
	@Test
	void jackson2Marshalling() throws Exception {

		assertThat(jackson2Mapper.writeValueAsString(errors)) //
				.isEqualToIgnoringWhitespace(json2Reference);
	}

	/**
	 * @see #93, #94
	 */
	@Test
	void jackson2UnMarshalling() throws Exception {
		assertThat(jackson2Mapper.readValue(jsonReference, VndErrors.class)).isEqualTo(errors);
	}

	private static String readFile(org.springframework.core.io.Resource resource) throws IOException {

		try (FileInputStream stream = new FileInputStream(resource.getFile())) {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			return Charset.defaultCharset().decode(bb).toString();
		}
	}
}
