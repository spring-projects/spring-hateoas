/*
 * Copyright 2013-2024 the original author or authors.
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
package org.springframework.hateoas.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.support.WebStack;
import org.springframework.http.MediaType;

/**
 * Activates hypermedia support in the {@link org.springframework.context.ApplicationContext}. Will register
 * infrastructure beans to support all appropriate web stacks based on selected
 * {@link HypermediaMappingInformation}-type as well as the classpath.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ HypermediaConfigurationImportSelector.class, HateoasConfiguration.class, WebStackImportSelector.class })
public @interface EnableHypermediaSupport {

	/**
	 * The hypermedia type to be supported.
	 *
	 * @return
	 */
	HypermediaType[] type();

	/**
	 * Configures which {@link WebStack}s we're supposed to enable support for. By default we're activating it for all
	 * available ones if they happen to be in use. Configure this explicitly in case you're using WebFlux components like
	 * {@link org.springframework.web.reactive.function.client.WebClient} but don't want to use hypermedia operations with
	 * it.
	 *
	 * @return
	 */
	WebStack[] stacks() default { WebStack.WEBMVC, WebStack.WEBFLUX };

	/**
	 * Hypermedia representation types supported.
	 *
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 */
	enum HypermediaType {

		/**
		 * HAL - Hypermedia Application Language.
		 *
		 * @see http://stateless.co/hal_specification.html
		 * @see https://tools.ietf.org/html/draft-kelly-json-hal
		 */
		HAL(List.of(MediaTypes.HAL_JSON, MediaTypes.VND_HAL_JSON), "hal"),

		/**
		 * HAL-FORMS - Independent, backward-compatible extension of the HAL designed to add runtime FORM support
		 *
		 * @see https://rwcbook.github.io/hal-forms/
		 */
		HAL_FORMS(MediaTypes.HAL_FORMS_JSON, "hal.forms"),

		HTTP_PROBLEM_DETAILS(MediaTypes.HTTP_PROBLEM_DETAILS_JSON, "problem"),

		/**
		 * Collection+JSON
		 *
		 * @see http://amundsen.com/media-types/collection/format/
		 */
		COLLECTION_JSON(MediaTypes.COLLECTION_JSON, "collectionjson"),

		/**
		 * UBER Hypermedia
		 *
		 * @see https://rawgit.com/uber-hypermedia/specification/master/uber-hypermedia.html
		 */
		UBER(MediaTypes.UBER_JSON, "uber");

		private final List<MediaType> mediaTypes;
		private final String localPackageName;

		HypermediaType(MediaType mediaType, String localPackageName) {
			this.mediaTypes = List.of(mediaType);
			this.localPackageName = localPackageName;
		}

		HypermediaType(List<MediaType> mediaTypes, String localPackageName) {
			this.mediaTypes = mediaTypes;
			this.localPackageName = localPackageName;
		}

		public List<MediaType> getMediaTypes() {
			return this.mediaTypes;
		}

		/**
		 * @deprecated since 2.5, in favor of {@link #getMediaTypes()} as a logical media type might have been associated
		 *             with multiple media type identifiers (see {@link #HAL}).
		 * @return will never be {@literal null}.
		 */
		@Deprecated(since = "2.5", forRemoval = true)
		public MediaType getMediaType() {
			return this.mediaTypes.get(0);
		}

		public String getLocalPackageName() {
			return localPackageName;
		}
	}
}
