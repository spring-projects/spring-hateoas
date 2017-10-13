/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.hateoas.RelProvider;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;

/**
 * Unit tests for {@link DelegatingRelProvider}.
 * 
 * @author Oliver Gierke
 */
public class DelegatingRelProviderUnitTest {

	@Test
	public void foo() {

		List<RelProvider> providers = Arrays.asList(new AnnotationRelProvider(), new DefaultRelProvider());
		PluginRegistry<RelProvider, Class<?>> registry = OrderAwarePluginRegistry.create(providers);

		RelProvider delegatingProvider = new DelegatingRelProvider(registry);

		assertThat(delegatingProvider.supports(Sample.class)).isTrue();
		assertThat(delegatingProvider.getItemResourceRelFor(Sample.class)).isEqualTo("foo");
		assertThat(delegatingProvider.getCollectionResourceRelFor(Sample.class)).isEqualTo("bar");

		assertThat(delegatingProvider.supports(String.class)).isTrue();
		assertThat(delegatingProvider.getItemResourceRelFor(String.class)).isEqualTo("string");
		assertThat(delegatingProvider.getCollectionResourceRelFor(String.class)).isEqualTo("stringList");
	}

	@Relation(value = "foo", collectionRelation = "bar")
	static class Sample {

	}
}
