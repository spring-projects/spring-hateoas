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
package org.springframework.hateoas.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DefaultLinkDiscoverer;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.Jackson1HalModule;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for {@link EnableHypermediaSupport}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class EnableHypermediaSupportIntegrationTest {

	@Test
	public void bootstrapHalConfiguration() {

		ApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);
		assertEntityLinksSetUp(context);
		assertThat(context.getBean(LinkDiscoverer.class), is(instanceOf(HalLinkDiscoverer.class)));

		ObjectMapper mapper = context.getBean(ObjectMapper.class);
		verify(mapper, times(1)).registerModule(Mockito.any(Jackson2HalModule.class));

		org.codehaus.jackson.map.ObjectMapper jackson1Mapper = context.getBean(org.codehaus.jackson.map.ObjectMapper.class);
		verify(jackson1Mapper, times(1)).registerModule(Mockito.any(Jackson1HalModule.class));
	}

	@Test
	public void bootstrapsDefaultConfiguration() {

		ApplicationContext context = new AnnotationConfigApplicationContext(DefaultConfig.class);
		assertEntityLinksSetUp(context);
		assertRelProvidersSetUp(context);
		assertThat(context.getBean(LinkDiscoverer.class), is(instanceOf(DefaultLinkDiscoverer.class)));
	}

	private static void assertEntityLinksSetUp(ApplicationContext context) {

		Map<String, EntityLinks> discoverers = context.getBeansOfType(EntityLinks.class);
		assertThat(discoverers.values(), hasItem(Matchers.<EntityLinks> instanceOf(DelegatingEntityLinks.class)));
	}

	private static void assertRelProvidersSetUp(ApplicationContext context) {

		Map<String, RelProvider> discoverers = context.getBeansOfType(RelProvider.class);
		assertThat(discoverers.values(), hasItem(Matchers.<RelProvider> instanceOf(DelegatingRelProvider.class)));
	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig {

		@Bean
		public ObjectMapper jackson2ObjectMapper() {
			return mock(ObjectMapper.class);
		}

		@Bean
		public org.codehaus.jackson.map.ObjectMapper jackson1ObjectMapper() {
			return mock(org.codehaus.jackson.map.ObjectMapper.class);
		}
	}

	@Configuration
	@EnableHypermediaSupport
	static class DefaultConfig {

	}
}
