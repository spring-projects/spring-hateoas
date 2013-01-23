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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.TestUtils;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link DelegatingEntityLinks}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class DelegatingEntityLinksUnitTest extends TestUtils {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Mock
	EntityLinks target;

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

		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(String.class.getName());

		EntityLinks links = new DelegatingEntityLinks(SimplePluginRegistry.<Class<?>, EntityLinks> create());
		links.linkFor(String.class);
	}

	@Test
	public void supportsDomainTypeBackedByPlugin() {

		EntityLinks links = createDelegatingEntityLinks();

		assertThat(links.supports(String.class), is(true));
	}

	@Test
	public void delegatesLinkForCall() {

		EntityLinks links = createDelegatingEntityLinks();

		links.linkFor(String.class);
		verify(target, times(1)).linkFor(String.class);
	}

	private EntityLinks createDelegatingEntityLinks() {

		PluginRegistry<EntityLinks, Class<?>> registry = SimplePluginRegistry.create(Arrays.asList(target));
		return new DelegatingEntityLinks(registry);
	}

	@ExposesResourceFor(String.class)
	@RequestMapping("/string")
	static class Controller {

	}
}
