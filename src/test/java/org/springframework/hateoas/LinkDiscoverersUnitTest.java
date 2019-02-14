/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.core.Ordered;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;

/**
 * Unit tests for {@link LinkDiscoverers}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class LinkDiscoverersUnitTest {

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullPluginRegistry() {
		new LinkDiscoverers(null);
	}

	@Test
	public void favorsCustomLinkDiscovererOverDefault() {

		LinkDiscoverer low = new LowPriorityLinkDiscoverer();
		LinkDiscoverer high = new HighPriorityLinkDiscoverer();

		PluginRegistry<LinkDiscoverer, MediaType> registry = OrderAwarePluginRegistry.of(Arrays.asList(low, high));
		assertThat(registry.getRequiredPluginFor(MediaType.APPLICATION_JSON)).isEqualTo(high);
	}

	static class LowPriorityLinkDiscoverer extends JsonPathLinkDiscoverer implements Ordered {

		@Override
		public int getOrder() {
			return 20;
		}

		public LowPriorityLinkDiscoverer() {
			super("$.links.%s", MediaType.APPLICATION_JSON);
		}
	}

	static class HighPriorityLinkDiscoverer extends JsonPathLinkDiscoverer implements Ordered {

		@Override
		public int getOrder() {
			return 10;
		}

		public HighPriorityLinkDiscoverer() {
			super("$.links.%s", MediaType.APPLICATION_JSON);
		}
	}
}
