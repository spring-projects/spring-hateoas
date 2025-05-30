/*
 * Copyright 2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A {@link TypeConstrainedMappingJackson2HttpMessageConverter} that will inspect the returned
 * {@link RepresentationModel} for affordances and flip the {@link org.springframework.http.MediaType} rendered back to
 * {@link MediaTypes#HAL_JSON} if no templates have been registered.
 *
 * @author Oliver Drotbohm
 * @since 2.4
 */
public class HalFormsHttpMessageConverter extends TypeConstrainedMappingJackson2HttpMessageConverter {

	private final HalFormsTemplateBuilder builder;

	/**
	 * Creates a new {@link HalFormsHttpMessageConverter} for the given {@link BeanFactory} and {@link ObjectMapper}
	 *
	 * @param factory must not be {@literal null}.
	 * @param mapper must not be {@literal null}.
	 */
	public HalFormsHttpMessageConverter(BeanFactory factory, ObjectMapper mapper) {

		super(RepresentationModel.class, List.of(MediaTypes.HAL_FORMS_JSON), mapper);

		Assert.notNull(factory, "BeanFactory must not be null!");
		Assert.notNull(mapper, "ObjectMapper must not be null!");

		this.builder = factory.getBean(HalFormsTemplateBuilder.class);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter#writeInternal(java.lang.Object, java.lang.reflect.Type, org.springframework.http.HttpOutputMessage)
	 */
	@Override
	protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

		if (!(object instanceof RepresentationModel model)) {
			super.writeInternal(object, type, outputMessage);
			return;
		}

		var result = builder.findTemplates(model);

		// Revert back to HAL if no templates found
		if (result.isEmpty()) {
			var headers = outputMessage.getHeaders();
			headers.setContentType(MediaTypes.HAL_JSON);
		}

		super.writeInternal(object, type, outputMessage);
	}
}
