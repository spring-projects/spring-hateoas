/*
 * Copyright 2014-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.http.MediaType.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.converter.GenericHttpMessageConverter;

/**
 * Unit tests for {@link TypeConstrainedMappingJackson2HttpMessageConverter}.
 * 
 * @author Oliver Gierke
 */
class TypeConstrainedMappingJackson2HttpMessageConverterUnitTest {

	/**
	 * @see #219
	 */
	@Test
	void rejectsNullType() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new TypeConstrainedMappingJackson2HttpMessageConverter(null);
		});
	}

	/**
	 * @see #219, #360
	 */
	@Test
	void canReadTypeIfAssignableToConfiguredType() {

		GenericHttpMessageConverter<Object> converter = new TypeConstrainedMappingJackson2HttpMessageConverter(
				RepresentationModel.class);

		assertCanRead(converter, Object.class, false);
		assertCanRead(converter, RepresentationModel.class, true);
		assertCanRead(converter, EntityModel.class, true);
	}

	/**
	 * @see #219, #360
	 */
	@Test
	void canWriteTypeIfAssignableToConfiguredType() {

		GenericHttpMessageConverter<Object> converter = new TypeConstrainedMappingJackson2HttpMessageConverter(
				RepresentationModel.class);

		assertCanWrite(converter, Object.class, false);
		assertCanWrite(converter, RepresentationModel.class, true);
		assertCanWrite(converter, EntityModel.class, true);
	}

	private static void assertCanRead(GenericHttpMessageConverter<Object> converter, Class<?> type, boolean expected) {

		assertThat(converter.canRead(type, APPLICATION_JSON)).isEqualTo(expected);
		assertThat(converter.canRead(type, type, APPLICATION_JSON)).isEqualTo(expected);
	}

	private static void assertCanWrite(GenericHttpMessageConverter<Object> converter, Class<?> type, boolean expected) {

		assertThat(converter.canWrite(type, APPLICATION_JSON)).isEqualTo(expected);
	}
}
