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
package org.springframework.hateoas.mediatype;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.MediaType;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Test utilities to verify configuration of media type support.
 *
 * @author Oliver Drotbohm
 */
public class MediaTypeTestUtils {

	/**
	 * Looks up the the media types supported for {@link RepresentationModel} in the {@link RequestMappingHandlerAdapter}
	 * within the given {@link ApplicationContext}.
	 *
	 * @param context must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static List<MediaType> getSupportedHypermediaTypes(ApplicationContext context) {
		return getSupportedHypermediaTypes(context, RepresentationModel.class);
	}

	/**
	 * Looks up the the media types supported for the given type in the {@link RequestMappingHandlerAdapter} within the
	 * given {@link ApplicationContext}.
	 *
	 * @param context must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static List<MediaType> getSupportedHypermediaTypes(ApplicationContext context, Class<?> type) {

		return getSupportedHypermediaTypes(context,
				it -> it.getBean(RequestMappingHandlerAdapter.class).getMessageConverters(), type);
	}

	public static List<MediaType> getSupportedHypermediaTypes(ApplicationContext context,
			Function<ApplicationContext, List<HttpMessageConverter<?>>> extractor) {
		return getSupportedHypermediaTypes(context, extractor, RepresentationModel.class);
	}

	public static List<MediaType> getSupportedHypermediaTypes(ApplicationContext context,
			Function<ApplicationContext, List<HttpMessageConverter<?>>> extractor, Class<?> type) {
		return getSupportedHypermediaTypes(extractor.apply(context), type);
	}

	public static List<MediaType> getSupportedHypermediaTypes(List<HttpMessageConverter<?>> converters) {
		return getSupportedHypermediaTypes(converters, RepresentationModel.class); //
	}

	public static List<MediaType> getSupportedHypermediaTypes(List<HttpMessageConverter<?>> converters, Class<?> type) {

		return converters.stream() //
				.filter(MappingJackson2HttpMessageConverter.class::isInstance) //
				.map(MappingJackson2HttpMessageConverter.class::cast) //
				.findFirst() //
				.map(it -> it.getSupportedMediaTypes(type)) //
				.orElseGet(() -> Collections.emptyList()); //
	}

	public static List<MediaType> getSupportedHypermediaTypes(WebClient client) {
		return getSupportedHypermediaTypes(client, RepresentationModel.class); //
	}

	@SuppressWarnings("unchecked")
	public static List<MediaType> getSupportedHypermediaTypes(WebClient client, Class<?> type) {

		return exchangeStrategies(client).messageReaders().stream() //
				.filter(DecoderHttpMessageReader.class::isInstance) //
				.map(DecoderHttpMessageReader.class::cast) //
				.filter(it -> Jackson2JsonDecoder.class.isInstance(it.getDecoder()))
				.findFirst() //
				.map(it -> it.getReadableMediaTypes(ResolvableType.forClass(type))) //
				.orElseGet(() -> Collections.emptyList());
	}

	/**
	 * Extract the {@link ExchangeStrategies} from a {@link WebTestClient} to assert it has the proper message readers and
	 * writers.
	 *
	 * @param webClient
	 * @return
	 */
	@SuppressWarnings("null")
	private static ExchangeStrategies exchangeStrategies(WebClient webClient) {

		return (ExchangeStrategies) ReflectionTestUtils
				.getField(ReflectionTestUtils.getField(webClient, "exchangeFunction"), "strategies");
	}
}
