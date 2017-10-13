/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.alps;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.AbstractLinkDiscovererUnitTest;
import org.springframework.util.StreamUtils;

/**
 * Unit tests for {@link AlpsLinkDiscoverer}.
 * 
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public class AlpsLinkDiscoverUnitTest extends AbstractLinkDiscovererUnitTest {

	LinkDiscoverer discoverer = new AlpsLinkDiscoverer();

	@Test
	public void discoversFullyQualifiedRel() {

		Link link = getDiscoverer().findLinkWithRel("http://foo.com/bar", getInputString());

		assertThat(link).isNotNull();
		assertThat(link.getHref()).isEqualTo("fullRelHref");
	}

	/**
	 * Return the {@link LinkDiscoverer} to be tested.
	 *
	 * @return
	 */
	@Override
	protected LinkDiscoverer getDiscoverer() {
		return discoverer;
	}

	/**
	 * Return the JSON structure we expect to find the links in.
	 *
	 * @return
	 */
	@Override
	protected String getInputString() {

		try {
			return StreamUtils.copyToString(new ClassPathResource("link-discoverer.json", getClass()).getInputStream(),
					Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String getInputStringWithoutLinkContainer() {
		return "{}";
	}
}
