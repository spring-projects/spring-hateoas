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
package org.springframework.hateoas.mediatype.uber;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscovererUnitTest;

/**
 * Unit tests for {@link HalLinkDiscoverer}.
 *
 * @author Oliver Gierke
 */
class UberLinkDiscovererUnitTest extends LinkDiscovererUnitTest {

	LinkDiscoverer discoverer = new UberLinkDiscoverer();
	ContextualMapper $ = MappingTestUtils.createMapper();
	String sample;

	@BeforeEach
	void setUp() throws IOException {

		this.discoverer = new UberLinkDiscoverer();
		this.sample = $.readFileContent("link-discovery.json");
	}

	/**
	 * @see #314
	 * @see #784
	 */
	@Test
	void discoversFullyQualifiedRel() {
		assertThat(getDiscoverer().findLinkWithRel("http://www.foo.com/bar", this.sample)).isNotNull();
	}

	@Override
	protected LinkDiscoverer getDiscoverer() {
		return discoverer;
	}

	@Override
	protected String getInputString() {
		return this.sample;
	}

	@Override
	protected String getInputStringWithoutLinkContainer() {
		return "{ \"uber\" : { \"data\" : []}}";
	}
}
