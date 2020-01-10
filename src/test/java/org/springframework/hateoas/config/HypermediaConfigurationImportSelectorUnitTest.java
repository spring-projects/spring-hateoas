/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.hateoas.mediatype.uber.UberLinkDiscoverer;

/**
 * Unit tests for {@link HypermediaConfigurationImportSelector}.
 *
 * @author Greg Turnquist
 */
class HypermediaConfigurationImportSelectorUnitTest {

	@Test // #833
	void testHalImportConfiguration() {

		withContext(HalConfig.class, context -> {

			Map<String, LinkDiscoverer> linkDiscoverers = context.getBeansOfType(LinkDiscoverer.class);

			assertThat(linkDiscoverers.values()).extracting("class") //
					.containsExactly(HalLinkDiscoverer.class);
		});
	}

	@Test // #833
	void testHalFormsImportConfiguration() {

		withContext(HalFormsConfig.class, context -> {

			Map<String, LinkDiscoverer> linkDiscoverers = context.getBeansOfType(LinkDiscoverer.class);

			assertThat(linkDiscoverers.values()).extracting("class") //
					.containsExactly(HalFormsLinkDiscoverer.class);
		});
	}

	@Test // #833
	void testHalAndHalFormsImportConfigurations() {

		withContext(HalAndHalFormsConfig.class, context -> {

			Map<String, LinkDiscoverer> linkDiscoverers = context.getBeansOfType(LinkDiscoverer.class);

			assertThat(linkDiscoverers.values()).extracting("class") //
					.containsExactlyInAnyOrder( //
							HalLinkDiscoverer.class, //
							HalFormsLinkDiscoverer.class);
		});
	}

	@Test // #833
	void testAllImportConfigurations() {

		withContext(AllConfig.class, context -> {

			Map<String, LinkDiscoverer> linkDiscoverers = context.getBeansOfType(LinkDiscoverer.class);

			assertThat(linkDiscoverers.values()).extracting("class") //
					.containsExactlyInAnyOrder( //
							HalLinkDiscoverer.class, //
							HalFormsLinkDiscoverer.class, //
							UberLinkDiscoverer.class, //
							CollectionJsonLinkDiscoverer.class);
		});
	}

	@EnableHypermediaSupport(type = HAL)
	static class HalConfig {

	}

	@EnableHypermediaSupport(type = HAL_FORMS)
	static class HalFormsConfig {

	}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS })
	static class HalAndHalFormsConfig {

	}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, UBER, COLLECTION_JSON })
	static class AllConfig {

	}
}
