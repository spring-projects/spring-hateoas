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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NullValueSerializerTest {

	private NullValueSerializer nullValueSerializer;
	
	@Mock
	private JsonGenerator jgen;
	@Mock
	private SerializerProvider serializerProvider;

	@Before
	public void setUp() throws Exception {
		nullValueSerializer = new NullValueSerializer();
	}

	@Test
	public void serializesNullValue() throws Exception {
		nullValueSerializer.serialize(UberNode.NULL_VALUE, jgen, serializerProvider);
		Mockito.verify(jgen).writeNull();
	}

}
