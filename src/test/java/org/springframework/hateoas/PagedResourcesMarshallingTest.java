/*
 * Copyright 2013-2017 the original author or authors.
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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Test for marshalling / unmarshalling of {@link PagedResources}.
 * 
 * @author Eric Bottard
 */
public class PagedResourcesMarshallingTest {

	Marshaller marshaller;
	Unmarshaller unmarshaller;

	String xmlReference;
	PagedResources<Inner> pagedResources;

	public PagedResourcesMarshallingTest() throws IOException {
		xmlReference = readFile(new ClassPathResource("pagedresources.xml"));
	}

	@Before
	public void setUp() throws Exception {

		JAXBContext context = JAXBContext.newInstance(PagedResources.class);
		marshaller = context.createMarshaller();
		unmarshaller = context.createUnmarshaller();

		pagedResources = new PagedResources<Inner>(new ArrayList<Inner>(), null);
	}

	/**
	 * @see #98
	 */
	@Test
	public void jaxbUnMarshalling() throws Exception {

		assertThat(unmarshaller.unmarshal(new StringReader(xmlReference))).isEqualTo(pagedResources);
	}

	public static class Inner {}

	private static String readFile(org.springframework.core.io.Resource resource) throws IOException {

		FileInputStream stream = new FileInputStream(resource.getFile());

		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}
}
