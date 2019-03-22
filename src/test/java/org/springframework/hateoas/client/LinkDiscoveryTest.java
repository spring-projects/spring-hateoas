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
package org.springframework.hateoas.client;

import org.junit.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.LinkDiscovery.Discoverer;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;

/**
 * @author Oliver Gierke
 */
public class LinkDiscoveryTest {

	@Test
	public void someTest() {

		PluginRegistry<LinkDiscoverer, MediaType> registry = OrderAwarePluginRegistry.of(new HalLinkDiscoverer());

		LinkDiscovery discovery = new LinkDiscovery(registry);

		Discoverer forMediaType = discovery.forMediaType(MediaTypes.HAL_JSON);
		forMediaType.findRelation(IanaLinkRelations.SELF).in("{ some JSON }").all();
	}
}
