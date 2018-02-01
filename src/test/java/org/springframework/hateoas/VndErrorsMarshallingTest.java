/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.custommonkey.xmlunit.Diff;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.VndErrors.VndError;
import org.springframework.hateoas.core.EvoInflectorRelProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Integration tests for marshalling of {@link VndErrors}.
 * 
 * @author Oliver Gierke
 */
public class VndErrorsMarshallingTest {

	ObjectMapper jackson2Mapper;

	RelProvider relProvider = new EvoInflectorRelProvider();

	VndErrors errors;
	String jsonReference;
	String json2Reference;
	String xmlReference;

	public VndErrorsMarshallingTest() throws IOException {

		jsonReference = readFile(new ClassPathResource("vnderror.json"));
		json2Reference = readFile(new ClassPathResource("vnderror2.json"));
		xmlReference = readFile(new ClassPathResource("vnderror.xml"));
	}

	@Before
	public void setUp() throws Exception {

		jackson2Mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		jackson2Mapper.registerModule(new Jackson2HalModule());
		jackson2Mapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(relProvider, null, null));
		jackson2Mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		VndError error = new VndError("42", "Validation failed!", //
				new Link("http://...", "describes"), new Link("http://...", "help"));
		errors = new VndErrors(error, error, error);
	}

	/**
	 * @see #62
	 */
	@Test
	public void jackson2Marshalling() throws Exception {
		assertThat(jackson2Mapper.writeValueAsString(errors)).isEqualToIgnoringWhitespace(json2Reference);
	}

	/**
	 * @see #93, #94
	 */
	@Test
	public void jackson2UnMarshalling() throws Exception {
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
