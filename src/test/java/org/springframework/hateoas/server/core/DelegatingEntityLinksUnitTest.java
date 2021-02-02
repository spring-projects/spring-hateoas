/*
 * Copyright 2012-2021 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.plugin.core.SimplePluginRegistry;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link DelegatingEntityLinks}.
 * 
 * @author Oliver Gierke
 */
class DelegatingEntityLinksUnitTest extends TestUtils {

	@Test
	void rejectsNullPluginRegistry() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new DelegatingEntityLinks(null);
		});
	}

	@Test
	void throwsExceptionForUnsupportedClass() {

		EntityLinks links = new DelegatingEntityLinks(SimplePluginRegistry.empty());

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> links.linkFor(String.class)) //
				.withMessageContaining(String.class.getName());
	}

	@Test
	void supportsDomainTypeBackedByPlugin() {

		EntityLinks target = mock(EntityLinks.class);
		when(target.supports(String.class)).thenReturn(true);

		EntityLinks links = createDelegatingEntityLinks(target);

		assertThat(links.supports(String.class)).isTrue();
	}

	@Test
	void delegatesLinkForCall() {

		EntityLinks target = mock(EntityLinks.class);
		when(target.supports(String.class)).thenReturn(true);

		createDelegatingEntityLinks(target).linkFor(String.class);

		verify(target, times(1)).linkFor(String.class);
	}

	private EntityLinks createDelegatingEntityLinks(EntityLinks target) {
		return new DelegatingEntityLinks(SimplePluginRegistry.of(target));
	}

	@ExposesResourceFor(String.class)
	@RequestMapping("/string")
	static class Controller {}
}
