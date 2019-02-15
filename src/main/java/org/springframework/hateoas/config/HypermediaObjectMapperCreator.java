/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.config;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.collectionjson.Jackson2CollectionJsonModule;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.hateoas.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.hal.forms.Jackson2HalFormsModule;
import org.springframework.hateoas.hal.forms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import org.springframework.hateoas.uber.Jackson2UberModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for create {@link ObjectMapper} instances of all hypermedia types.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public final class HypermediaObjectMapperCreator {

	/**
	 * Create a {@link org.springframework.hateoas.MediaTypes#HAL_JSON}-based {@link ObjectMapper}.
	 *
	 * @param objectMapper
	 * @param curieProvider
	 * @param relProvider
	 * @param linkRelationMessageSource
	 * @param halConfiguration
	 * @return
	 */
	public static ObjectMapper createHalObjectMapper(ObjectMapper objectMapper, CurieProvider curieProvider,
			RelProvider relProvider, MessageSourceAccessor linkRelationMessageSource, HalConfiguration halConfiguration) {

		ObjectMapper mapper = objectMapper.copy();

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(
				new HalHandlerInstantiator(relProvider, curieProvider, linkRelationMessageSource, halConfiguration));

		return mapper;
	}

	/**
	 * Create a {@link org.springframework.hateoas.MediaTypes#HAL_FORMS_JSON}-based {@link ObjectMapper}.
	 *
	 * @param objectMapper
	 * @param curieProvider
	 * @param relProvider
	 * @param linkRelationMessageSource
	 * @param halFormsConfiguration
	 * @return properly configured objectMapper
	 */
	public static ObjectMapper createHalFormsObjectMapper(ObjectMapper objectMapper, CurieProvider curieProvider,
			RelProvider relProvider, MessageSourceAccessor linkRelationMessageSource,
			HalFormsConfiguration halFormsConfiguration) {

		ObjectMapper mapper = objectMapper.copy();

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(relProvider, curieProvider, linkRelationMessageSource,
				true, halFormsConfiguration));

		return mapper;
	}

	/**
	 * Create a {@link org.springframework.hateoas.MediaTypes#COLLECTION_JSON}-based {@link ObjectMapper}.
	 *
	 * @param objectMapper
	 * @param linkRelationMessageSource
	 * @return properly configured objectMapper
	 */
	public static ObjectMapper createCollectionJsonObjectMapper(ObjectMapper objectMapper) {

		ObjectMapper mapper = objectMapper.copy();

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.registerModule(new Jackson2CollectionJsonModule());

		return mapper;
	}

	/**
	 * Create a {@link org.springframework.hateoas.MediaTypes#UBER_JSON}-based {@link ObjectMapper}.
	 *
	 * @param objectMapper
	 * @return properly configured objectMapper
	 */
	public static ObjectMapper createUberObjectMapper(ObjectMapper objectMapper) {

		ObjectMapper mapper = objectMapper.copy();

		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.registerModule(new Jackson2UberModule());

		return mapper;
	}
}
