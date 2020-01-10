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

import java.lang.reflect.Type;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Extension of {@link MappingJackson2HttpMessageConverter} to constrain the ability to read and write HTTP message
 * based on the target type. Useful in case the {@link ObjectMapper} about to be configured has customizations that
 * shall only be applied to object trees of a certain base type.
 *
 * @author Oliver Gierke
 */
public class TypeConstrainedMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

	private final Class<?> type;

	/**
	 * Creates a new {@link TypeConstrainedMappingJackson2HttpMessageConverter} for the given type.
	 *
	 * @param type must not be {@literal null}.
	 */
	public TypeConstrainedMappingJackson2HttpMessageConverter(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");
		this.type = type;
	}

	/**
	 * Convenience constructor to supply all parameters at once.
	 *
	 * @param type
	 * @param supportedMediaTypes
	 * @param objectMapper
	 */
	public TypeConstrainedMappingJackson2HttpMessageConverter(Class<?> type, List<MediaType> supportedMediaTypes,
			ObjectMapper objectMapper) {

		this(type);
		setSupportedMediaTypes(supportedMediaTypes);
		setObjectMapper(objectMapper);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.MappingJackson2HttpMessageConverter#canRead(java.lang.Class, org.springframework.http.MediaType)
	 */
	@Override
	public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
		return type.isAssignableFrom(clazz) && super.canRead(clazz, mediaType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.MappingJackson2HttpMessageConverter#canRead(java.lang.reflect.Type, java.lang.Class, org.springframework.http.MediaType)
	 */
	@Override
	public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
		return this.type.isAssignableFrom(getJavaType(type, contextClass).getRawClass())
				&& super.canRead(type, contextClass, mediaType);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.MappingJackson2HttpMessageConverter#canWrite(java.lang.Class, org.springframework.http.MediaType)
	 */
	@Override
	public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
		return type.isAssignableFrom(clazz) && super.canWrite(clazz, mediaType);
	}
}
