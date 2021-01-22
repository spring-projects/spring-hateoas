/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.html;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;

/**
 * @author Oliver Drotbohm
 */
@Configuration
public class HtmlConfiguration implements HypermediaMappingInformation {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getMediaTypes()
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return Collections.singletonList(MediaType.TEXT_HTML);
	}

	@Bean
	HttpMessageConverter<RepresentationModel<?>> htmlHttpMessageConverter(ResourceLoader loader, MessageResolver messages)
			throws IOException {

		HtmlFormRenderer renderer = new HtmlFormRenderer(loader, messages);
		return new JMustacheRenderingHttpMessageConverter(renderer);
	}

	private static class JMustacheRenderingHttpMessageConverter
			implements HttpMessageConverter<RepresentationModel<?>> {

		private final HtmlFormRenderer renderer;

		JMustacheRenderingHttpMessageConverter(HtmlFormRenderer renderer) throws IOException {
			this.renderer = renderer;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.http.converter.HttpMessageConverter#getSupportedMediaTypes()
		 */
		@Override
		public List<MediaType> getSupportedMediaTypes() {
			return Collections.singletonList(MediaType.TEXT_HTML);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.http.converter.HttpMessageConverter#canRead(java.lang.Class, org.springframework.http.MediaType)
		 */
		@Override
		public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.http.converter.HttpMessageConverter#canWrite(java.lang.Class, org.springframework.http.MediaType)
		 */
		@Override
		public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
			return RepresentationModel.class.isAssignableFrom(clazz) && MediaType.TEXT_HTML.equals(mediaType);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.http.converter.HttpMessageConverter#write(java.lang.Object, org.springframework.http.MediaType, org.springframework.http.HttpOutputMessage)
		 */
		@Override
		public void write(RepresentationModel<?> t, @Nullable MediaType contentType, HttpOutputMessage outputMessage)
				throws IOException, HttpMessageNotWritableException {

			OutputStream body = outputMessage.getBody();

			String forms = t.getLinks().stream()
					.flatMap(it -> it.getAffordances().stream())
					.map(renderer::renderForm)
					.collect(Collectors.joining());

			body.write(forms.getBytes(StandardCharsets.UTF_8));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.http.converter.HttpMessageConverter#read(java.lang.Class, org.springframework.http.HttpInputMessage)
		 */
		@Override
		public RepresentationModel<?> read(Class<? extends RepresentationModel<?>> clazz, HttpInputMessage inputMessage)
				throws IOException, HttpMessageNotReadableException {
			throw new UnsupportedOperationException();
		}
	}
}
