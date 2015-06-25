/*
 * Copyright 2014-2015 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.http.MediaType.*;

import org.junit.Test;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.converter.GenericHttpMessageConverter;

/**
 * Unit tests for {@link TypeConstrainedMappingJackson2HttpMessageConverter}.
 * 
 * @author Oliver Gierke
 */
public class TypeConstrainedMappingJackson2HttpMessageConverterUnitTest {

	/**
	 * @see #219
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullType() {
		new TypeConstrainedMappingJackson2HttpMessageConverter(null);
	}

	/**
	 * @see #219, #360
	 */
	@Test
	public void canReadTypeIfAssignableToConfiguredType() {

		GenericHttpMessageConverter<Object> converter = new TypeConstrainedMappingJackson2HttpMessageConverter(
				ResourceSupport.class);

		assertCanRead(converter, Object.class, false);
		assertCanRead(converter, ResourceSupport.class, true);
		assertCanRead(converter, Resource.class, true);
	}

	/**
	 * @see #219, #360
	 */
	@Test
	public void canWriteTypeIfAssignableToConfiguredType() {

		GenericHttpMessageConverter<Object> converter = new TypeConstrainedMappingJackson2HttpMessageConverter(
				ResourceSupport.class);

		assertCanWrite(converter, Object.class, false);
		assertCanWrite(converter, ResourceSupport.class, true);
		assertCanWrite(converter, Resource.class, true);
	}

	private static void assertCanRead(GenericHttpMessageConverter<Object> converter, Class<?> type, boolean expected) {

		assertThat(converter.canRead(type, APPLICATION_JSON), is(expected));
		assertThat(converter.canRead(type, type, APPLICATION_JSON), is(expected));
	}

	private static void assertCanWrite(GenericHttpMessageConverter<Object> converter, Class<?> type, boolean expected) {

		assertThat(converter.canWrite(type, APPLICATION_JSON), is(expected));
		assertThat(converter.canWrite(type, type, APPLICATION_JSON), is(expected));
	}
}
