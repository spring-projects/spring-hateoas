/*
 * Copyright 2019-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test utilities for HAL.
 *
 * @author Oliver Drotbohm
 */
public class HalTestUtils {

	/**
	 * Returns a default HAL {@link ObjectMapper} using a default {@link HalConfiguration}.
	 *
	 * @return
	 */
	public static ObjectMapper halObjectMapper() {
		return halObjectMapper(new HalConfiguration());
	}

	/**
	 * Returns a default HAL {@link ObjectMapper} using the given {@link HalConfiguration}.
	 *
	 * @param configuration must not be {@literal null}.
	 * @return
	 */
	public static ObjectMapper halObjectMapper(HalConfiguration configuration) {

		Assert.notNull(configuration, "HalConfiguration must not be null!");

		ObjectMapper mapper = MappingTestUtils.defaultObjectMapper();

		LinkRelationProvider provider = new DelegatingLinkRelationProvider(new AnnotationLinkRelationProvider(),
				HalTestUtils.DefaultLinkRelationProvider.INSTANCE);

		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(
				new HalHandlerInstantiator(provider, CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY, configuration,
						new DefaultListableBeanFactory()));

		return mapper;
	}

	public enum DefaultLinkRelationProvider implements LinkRelationProvider {

		INSTANCE;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.server.LinkRelationProvider#getItemResourceRelFor(java.lang.Class)
		 */
		@Override
		public LinkRelation getItemResourceRelFor(Class<?> type) {
			throw new UnsupportedOperationException();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.server.LinkRelationProvider#getCollectionResourceRelFor(java.lang.Class)
		 */
		@Override
		public LinkRelation getCollectionResourceRelFor(Class<?> type) {
			return LinkRelation.of("content");
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
		 */
		@Override
		public boolean supports(LookupContext delimiter) {
			return delimiter.isCollectionRelationLookup();
		}
	}
}
