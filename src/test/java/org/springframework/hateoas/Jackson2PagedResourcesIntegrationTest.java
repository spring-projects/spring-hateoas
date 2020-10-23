/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;
import static org.mockito.Mockito.*;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ReflectionUtils;

/**
 * Integration tests for serialization of {@link PagedModel}.
 * 
 * @author Oliver Gierke
 */
class Jackson2PagedResourcesIntegrationTest {

	private static String REFERENCE = "{\"links\":[],\"content\":[{\"firstname\":\"Dave\",\"lastname\":\"Matthews\"}],\"page\":{\"size\":1,\"totalElements\":2,\"totalPages\":2,\"number\":0}}";

	private static Method SPRING_4_2_WRITE_METHOD;

	static {

		try {
			SPRING_4_2_WRITE_METHOD = MappingJackson2HttpMessageConverter.class.getMethod("write", Object.class, Type.class,
					MediaType.class, HttpOutputMessage.class);
		} catch (Exception e) {}
	}

	/**
	 * @see SPR-13318
	 */
	@Test
	void serializesPagedResourcesCorrectly() throws Exception {

		assumeThat(SPRING_4_2_WRITE_METHOD).isNotNull();

		User user = new User();
		user.firstname = "Dave";
		user.lastname = "Matthews";

		PageMetadata metadata = new PagedModel.PageMetadata(1, 0, 2);
		PagedModel<User> resources = PagedModel.of(Collections.singleton(user), metadata);

		Method method = Sample.class.getMethod("someMethod");
		StringWriter writer = new StringWriter();

		HttpOutputMessage outputMessage = mock(HttpOutputMessage.class);
		when(outputMessage.getBody()).thenReturn(new WriterOutputStream(writer));
		when(outputMessage.getHeaders()).thenReturn(new HttpHeaders());

		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

		ReflectionUtils.invokeMethod(SPRING_4_2_WRITE_METHOD, converter, resources, method.getGenericReturnType(),
				MediaType.APPLICATION_JSON, outputMessage);

		assertThat(writer.toString()).isEqualTo(REFERENCE);
	}

	interface Sample {
		CollectionModel<?> someMethod();
	}

	static class User {
		public String firstname, lastname;
	}
}
