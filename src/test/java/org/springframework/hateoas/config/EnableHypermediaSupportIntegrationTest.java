/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.hateoas.hal.HalConfiguration.RenderSingleLinks.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
		assertHalSetupForConfigClass(HalConfig.class);
	}

	@Test
	public void registersLinkDiscoverers() {

		ConfigurableApplicationContext context = createApplicationContext(HalConfig.class);
		LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

		assertThat(discoverers, is(notNullValue()));
		assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON), is(instanceOf(HalLinkDiscoverer.class)));
		assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON_UTF8), is(instanceOf(HalLinkDiscoverer.class)));
		assertRelProvidersSetUp(context);
	}

	@Test
	public void bootstrapsHalConfigurationForSubclass() {
		assertHalSetupForConfigClass(ExtendedHalConfig.class);
	}

	/**
	 * @see #134, #219
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void halSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		ConfigurableApplicationContext context = createApplicationContext(HalConfig.class);

		RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

		assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes(),
				hasItems(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));

		boolean found = false;

		for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

			if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

				found = true;

				AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
				List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
						.getField(processor, "messageConverters");

				assertThat(converters.get(0), is(instanceOf(TypeConstrainedMappingJackson2HttpMessageConverter.class)));
				assertThat(converters.get(0).getSupportedMediaTypes(), hasItems(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));
			}
		}

		assertThat(found, is(true));
	}

	/**
	 * @see #293
	 */
	@Test
	public void registersHttpMessageConvertersForRestTemplate() {

		ConfigurableApplicationContext context = createApplicationContext(HalConfig.class);
		RestTemplate template = context.getBean(RestTemplate.class);

		assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes(),
				hasItems(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));
	}

	/**
	 * @see #341
	 */
	@Test
	public void configuresDefaultObjectMapperForHalToIgnoreUnknownProperties() {

		ObjectMapper mapper = getObjectMapperFor(MediaTypes.HAL_JSON, createApplicationContext(HalConfig.class));

		assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES), is(false));
	}

	@Test
	public void verifyDefaultHalConfigurationRendersSingleItemAsSingleItem() throws JsonProcessingException {

		ObjectMapper mapper = getObjectMapperFor(MediaTypes.HAL_JSON, createApplicationContext(HalConfig.class));

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(mapper.writeValueAsString(resourceSupport), is("{\"_links\":{\"self\":{\"href\":\"localhost\"}}}"));
	}

	@Test
	public void verifyRenderSingleLinkAsArrayViaOverridingBean() throws JsonProcessingException {

		ObjectMapper mapper = getObjectMapperFor(MediaTypes.HAL_JSON,
				createApplicationContext(RenderLinkAsSingleLinksConfig.class));

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(mapper.writeValueAsString(resourceSupport), is("{\"_links\":{\"self\":[{\"href\":\"localhost\"}]}}"));
	}

	private static void assertEntityLinksSetUp(ApplicationContext context) {

		Map<String, EntityLinks> discoverers = context.getBeansOfType(EntityLinks.class);
		assertThat(discoverers.values(), Matchers.<EntityLinks> hasItem(instanceOf(DelegatingEntityLinks.class)));
	}

	private static void assertRelProvidersSetUp(ApplicationContext context) {

		Map<String, RelProvider> discoverers = context.getBeansOfType(RelProvider.class);
		assertThat(discoverers.values(), Matchers.<RelProvider> hasItem(instanceOf(DelegatingRelProvider.class)));
	}

	@SuppressWarnings({ "unchecked" })
	private static void assertHalSetupForConfigClass(Class<?> configClass) {

		ConfigurableApplicationContext context = createApplicationContext(HalConfig.class);

		assertEntityLinksSetUp(context);
		assertThat(context.getBean(LinkDiscoverer.class), is(instanceOf(HalLinkDiscoverer.class)));

		RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
		assertThat(rmha.getMessageConverters(),
				Matchers.<HttpMessageConverter<?>> hasItems(instanceOf(MappingJackson2HttpMessageConverter.class)));
	}

	/**
	 * Method to mitigate API changes between Spring 3.2 and 4.0.
	 * 
	 * @param adapter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static List<HandlerMethodArgumentResolver> getResolvers(RequestMappingHandlerAdapter adapter) {

		Method method = ReflectionUtils.findMethod(RequestMappingHandlerAdapter.class, "getArgumentResolvers");
		Object result = ReflectionUtils.invokeMethod(method, adapter);

		if (result instanceof List) {
			return (List<HandlerMethodArgumentResolver>) result;
		}

		if (result instanceof HandlerMethodArgumentResolverComposite) {
			return ((HandlerMethodArgumentResolverComposite) result).getResolvers();
		}

		throw new IllegalStateException("Unexpected result when looking up argument resolvers!");
	}

	private static ConfigurableApplicationContext createApplicationContext(Class<?>... configurations) {

		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setServletContext(new MockServletContext());
		context.register(configurations);
		context.refresh();

		return context;
	}

	private static ObjectMapper getObjectMapperFor(MediaType mediaType, ApplicationContext context) {

		RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

		for (HttpMessageConverter<?> converter : adapter.getMessageConverters()) {
			if (converter.getSupportedMediaTypes().contains(mediaType)) {
				return ((AbstractJackson2HttpMessageConverter) converter).getObjectMapper();
			}
		}

		throw new IllegalArgumentException("Did not find HttpMessageConverter supporting " + mediaType + "!");
	}

	@EnableWebMvc
	@Configuration
	@Import(DelegateConfig.class)
	static class HalConfig {

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}

	@Configuration
	static class ExtendedHalConfig extends HalConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class DelegateConfig {

	}

	@EnableWebMvc
	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class RenderLinkAsSingleLinksConfig {

		@Bean
		HalConfiguration halConfiguration() {
			return new HalConfiguration().withRenderSingleLinks(AS_ARRAY);
		}
	}
}
