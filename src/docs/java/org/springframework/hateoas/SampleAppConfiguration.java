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
package org.springframework.hateoas;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.HalConfiguration.RenderSingleLinks;

/**
 * @author Greg Turnquist
 */

@Configuration
public class SampleAppConfiguration {

	// tag::1[]
	@Bean
	public HalConfiguration globalPolicy() {
		return new HalConfiguration() //
				.withRenderSingleLinks(RenderSingleLinks.AS_ARRAY); // <1>
	}
	// end::1[]

	// tag::2[]
	@Bean
	public HalConfiguration linkRelationBasedPolicy() {
		return new HalConfiguration() //
				.withRenderSingleLinksFor( //
						IanaLinkRelations.ITEM, RenderSingleLinks.AS_ARRAY) // <1>
				.withRenderSingleLinksFor( //
						LinkRelation.of("prev"), RenderSingleLinks.AS_SINGLE); // <2>
	}
	// end::2[]

	// tag::3[]
	@Bean
	public HalConfiguration patternBasedPolicy() {
		return new HalConfiguration() //
				.withRenderSingleLinksFor( //
						"http*", RenderSingleLinks.AS_ARRAY); // <1>
	}
	// end::3[]

}
