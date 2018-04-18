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
package org.springframework.hateoas.mvc;

import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.Order;
import org.springframework.hateoas.GenericAffordanceModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.core.AffordanceModelFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;

/**
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public class SpringMvcAffordanceBuilderUnitTest {

	@Test
	public void favorsCustomLinkDiscovererOverDefault() {

		AffordanceModelFactory low = new LowPriorityModelFactory();
		AffordanceModelFactory high = new HighPriorityModelFactory();

		PluginRegistry<AffordanceModelFactory, MediaType> registry = OrderAwarePluginRegistry
				.create(Arrays.asList(low, high));

		assertThat(registry.getPluginFor(MediaType.APPLICATION_JSON).get()).isEqualTo(high);
	}

	@Order(20)
	static class LowPriorityModelFactory implements AffordanceModelFactory {

		@Override
		public GenericAffordanceModel getAffordanceModel(String name, Link link, HttpMethod httpMethod, ResolvableType inputType, List<QueryParameter> queryMethodParameters, ResolvableType outputType) {
			return null;
		}

		@Override
		public boolean supports(MediaType delimiter) {
			return true;
		}
	}

	@Order(10)
	static class HighPriorityModelFactory implements AffordanceModelFactory {

		@Override
		public GenericAffordanceModel getAffordanceModel(String name, Link link, HttpMethod httpMethod, ResolvableType inputType, List<QueryParameter> queryMethodParameters, ResolvableType outputType) {
			return null;
		}

		@Override
		public boolean supports(MediaType delimiter) {
			return true;
		}
	}
}
