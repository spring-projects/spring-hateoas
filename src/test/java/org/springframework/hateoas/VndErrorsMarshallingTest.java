/*
 * Copyright 2013 the original author or authors.
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.VndErrors.VndError;
import org.springframework.hateoas.hal.Jackson1HalModule;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Integration tests for marshalling of {@link VndErrors}.
 * 
 * @author Oliver Gierke
 */
public class VndErrorsMarshallingTest {

	ObjectMapper jackson1Mapper;

	com.fasterxml.jackson.databind.ObjectMapper jackson2Mapper;

	Marshaller marshaller;

	Unmarshaller unmarshaller;

	VndErrors errors;

	String jsonReference;

	String xmlReference;

	public VndErrorsMarshallingTest() throws IOException {

		jsonReference = readFile(new ClassPathResource("vnderror.json"));
		xmlReference = readFile(new ClassPathResource("vnderror.xml"));
	}

	@Before
	public void setUp() throws Exception {

		jackson1Mapper = new ObjectMapper();
		jackson1Mapper.registerModule(new Jackson1HalModule());
		jackson1Mapper.configure(Feature.INDENT_OUTPUT, true);

		jackson2Mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		jackson2Mapper.registerModule(new Jackson2HalModule());
		jackson2Mapper.configure(SerializationFeature.INDENT_OUTPUT, true);

		JAXBContext context = JAXBContext.newInstance(VndErrors.class);
		marshaller = context.createMarshaller();
		unmarshaller = context.createUnmarshaller();

		VndError error = new VndError("42", "Validation failed!", //
				new Link("http://...", "describes"), new Link("http://...", "help"));
		errors = new VndErrors(error, error, error);
	}

	@Test
	public void jackson1Marshalling() throws Exception {
		assertThat(jackson1Mapper.writeValueAsString(errors), is(jsonReference));
	}

	@Test
	public void jackson2Marshalling() throws Exception {
		assertThat(jackson2Mapper.writeValueAsString(errors), is(jsonReference));
	}

	@Test
	public void jaxbMarshalling() throws Exception {

		Writer writer = new StringWriter();
		marshaller.marshal(errors, writer);
		assertThat(writer.toString(), is(xmlReference));
	}

	@Test
	public void jackson1UnMarshalling() throws Exception {
		VndErrors actual = jackson1Mapper.readValue(jsonReference, VndErrors.class);
		assertThat(actual, is(errors));
	}

	@Test
	public void jackson2UnMarshalling() throws Exception {
		assertThat(jackson2Mapper.readValue(jsonReference, VndErrors.class), is(errors));
	}

	@Test
	public void jaxbUnMarshalling() throws Exception {
		VndErrors actual = (VndErrors) unmarshaller.unmarshal(new StringReader(xmlReference));
		assertThat(actual, is(errors));
	}

	private static String readFile(org.springframework.core.io.Resource resource) throws IOException {

		FileInputStream stream = new FileInputStream(resource.getFile());

		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			return Charset.defaultCharset().decode(bb).toString();
		}
		finally {
			stream.close();
		}
	}
}
