/*
 * Copyright 2013-2019 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.collectionjson.CollectionJsonConfigurer;
import org.springframework.hateoas.hal.HalConfigurer;
import org.springframework.hateoas.hal.forms.HalFormsConfigurer;
import org.springframework.hateoas.uber.UberConfigurer;
import org.springframework.http.MediaType;

/**
 * Activates hypermedia support in the {@link ApplicationContext}. Will register infrastructure beans to support all
 * appropriate web stacks based on selected {@link Hypermedia}-type as well as the classpath.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableEntityLinks
@Import({ HypermediaSupportBeanDefinitionRegistrar.class, HateoasConfiguration.class, WebStackImportSelector.class })
public @interface EnableHypermediaSupport {

	/**
	 * The hypermedia type to be supported.
	 *
	 * @return
	 */
	HypermediaType[] type();

	/**
	 * Hypermedia representation types supported.
	 *
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 */
	enum HypermediaType implements Hypermedia {

		/**
		 * HAL - Hypermedia Application Language.
		 *
		 * @see http://stateless.co/hal_specification.html
		 * @see http://tools.ietf.org/html/draft-kelly-json-hal-05
		 */
		HAL(HalConfigurer.class, MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8),

		/**
		 * HAL-FORMS - Independent, backward-compatible extension of the HAL designed to add runtime FORM support
		 *
		 * @see https://rwcbook.github.io/hal-forms/
		 */
		HAL_FORMS(HalFormsConfigurer.class, MediaTypes.HAL_FORMS_JSON),

		/**
		 * Collection+JSON
		 *
		 * @see http://amundsen.com/media-types/collection/format/
		 */
		COLLECTION_JSON(CollectionJsonConfigurer.class, MediaTypes.COLLECTION_JSON),

		/**
		 * UBER Hypermedia
		 *
		 * @see http://uberhypermedia.org/
		 */
		UBER(UberConfigurer.class, MediaTypes.UBER_JSON);

		private final Class<?> configurer;
		private final List<MediaType> mediaTypes;

		HypermediaType(Class<?> configurer, MediaType... mediaTypes) {

			this.configurer = configurer;
			this.mediaTypes = Arrays.asList(mediaTypes);
		}

		@Override
		public List<MediaType> getMediaTypes() {
			return this.mediaTypes;
		}

		@Override
		public Optional<Class<?>> configurer() {
			return Optional.ofNullable(this.configurer);
		}

	}
}
