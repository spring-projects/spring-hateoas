/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.hal;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.hal.HalConfiguration.RenderSingleLinks;

/**
 * Unit tests for {@link HalConfiguration}.
 *
 * @author Oliver Drotbohm
 * @soundtrack Port Cities - Montreal (Single)
 */
public class HalConfigurationUnitTest {

	@Test // #811
	public void registersSimpleArrayLinksPattern() {

		HalConfiguration configuration = new HalConfiguration().withRenderSingleLinksFor("foo", RenderSingleLinks.AS_ARRAY);

		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("foo"))).isEqualTo(RenderSingleLinks.AS_ARRAY);
		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("bar"))).isEqualTo(RenderSingleLinks.AS_SINGLE);
	}

	@Test // #811
	public void registersWildcardedArrayLinksPattern() {

		HalConfiguration configuration = new HalConfiguration().withRenderSingleLinksFor("foo*",
				RenderSingleLinks.AS_ARRAY);

		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("foo"))).isEqualTo(RenderSingleLinks.AS_ARRAY);
		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("foobar")))
				.isEqualTo(RenderSingleLinks.AS_ARRAY);
		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("bar"))).isEqualTo(RenderSingleLinks.AS_SINGLE);
	}

	@Test // #811
	public void registersWildcardedArrayLinksPatternForUri() {

		HalConfiguration configuration = new HalConfiguration().withRenderSingleLinksFor("http://somehost/foo/**",
				RenderSingleLinks.AS_ARRAY);

		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("http://somehost/foo")))
				.isEqualTo(RenderSingleLinks.AS_ARRAY);
		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("http://somehost/foo/bar")))
				.isEqualTo(RenderSingleLinks.AS_ARRAY);
		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("http://somehost/foo/bar/foobar")))
				.isEqualTo(RenderSingleLinks.AS_ARRAY);
		assertThat(configuration.getSingleLinkRenderModeFor(LinkRelation.of("http://somehost/bar")))
				.isEqualTo(RenderSingleLinks.AS_SINGLE);
	}
}
