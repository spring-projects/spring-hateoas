/*
 * Copyright 2014-2024 the original author or authors.
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

import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.util.Assert;

/**
 * Extension of {@link MappingJackson2HttpMessageConverter} to constrain the ability to read and write HTTP message
 * based on the target type. Useful in case the {@link JsonMapper} about to be configured has customizations that shall
 * only be applied to object trees of a certain base type.
 *
 * @author Oliver Gierke
 */
public class TypeConstrainedJacksonJsonHttpMessageConverter extends JacksonJsonHttpMessageConverter {

	private final Class<?> type;

	/**
	 * Creates a new {@link TypeConstrainedMappingJackson2HttpMessageConverter} for the given type.
	 *
	 * @param type must not be {@literal null}.
	 */
	public TypeConstrainedJacksonJsonHttpMessageConverter(Class<?> type) {

		Assert.notNull(type, "Type must not be null!");
		this.type = type;
	}

	/**
	 * Convenience constructor to supply all parameters at once.
	 *
	 * @param type
	 * @param supportedMediaTypes
	 * @param mapper
	 */
	public TypeConstrainedJacksonJsonHttpMessageConverter(Class<?> type, List<MediaType> supportedMediaTypes,
			JsonMapper mapper) {

		super(mapper);

		setSupportedMediaTypes(supportedMediaTypes);

		this.type = type;
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
	 * @see org.springframework.http.converter.AbstractJacksonHttpMessageConverter#canRead(org.springframework.core.ResolvableType, org.springframework.http.MediaType)
	 */
	@Override
	public boolean canRead(ResolvableType type, @Nullable MediaType mediaType) {

		return this.type.isAssignableFrom(getJavaType(type.getType(), null).getRawClass())
				&& super.canRead(type, mediaType);
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
