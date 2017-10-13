/*
 * Copyright 2012-2013 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.TestUtils;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link DelegatingEntityLinks}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class DelegatingEntityLinksUnitTest extends TestUtils {

	@Mock EntityLinks target;

	@Before
	@Override
	public void setUp() {
		when(target.supports(String.class)).thenReturn(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullPluginRegistry() {
		new DelegatingEntityLinks(null);
	}

	@Test
	public void throwsExceptionForUnsupportedClass() {

		EntityLinks links = new DelegatingEntityLinks(SimplePluginRegistry.<Class<?>, EntityLinks> create());

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> links.linkFor(String.class)) //
				.withMessageContaining(String.class.getName());
	}

	@Test
	public void supportsDomainTypeBackedByPlugin() {

		EntityLinks links = createDelegatingEntityLinks();

		assertThat(links.supports(String.class)).isTrue();
	}

	@Test
	public void delegatesLinkForCall() {

		createDelegatingEntityLinks().linkFor(String.class);

		verify(target, times(1)).linkFor(String.class);
	}

	private EntityLinks createDelegatingEntityLinks() {
		return new DelegatingEntityLinks(SimplePluginRegistry.create(Arrays.asList(target)));
	}

	@ExposesResourceFor(String.class)
	@RequestMapping("/string")
	static class Controller {}
}
