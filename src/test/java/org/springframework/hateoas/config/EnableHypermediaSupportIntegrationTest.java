/*
 * Copyright 2013-2015 the original author or authors.
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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.LinkDiscoverers;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.HypermediaSupportBeanDefinitionRegistrar.Jackson2ModuleRegisteringBeanPostProcessor;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for {@link EnableHypermediaSupport}.
 * 
 * @author Oliver Gierke
 */
@SuppressWarnings("deprecation")
@RunWith(MockitoJUnitRunner.class)
public class EnableHypermediaSupportIntegrationTest {

	@Test
	public void bootstrapHalConfiguration() {
		assertHalSetupForConfigClass(HalConfig.class);
	}

	@Test
	public void registersLinkDiscoverers() {

		ApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);
		LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

		assertThat(discoverers, is(notNullValue()));
		assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON), is(instanceOf(HalLinkDiscoverer.class)));
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

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);

		Jackson2ModuleRegisteringBeanPostProcessor postProcessor = new HypermediaSupportBeanDefinitionRegistrar.Jackson2ModuleRegisteringBeanPostProcessor();
		postProcessor.setBeanFactory(context);

		RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

		assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes(), hasItem(MediaTypes.HAL_JSON));

		boolean found = false;

		for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

			if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

				found = true;

				AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
				List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils.getField(
						processor, "messageConverters");

				assertThat(converters.get(0), is(instanceOf(TypeConstrainedMappingJackson2HttpMessageConverter.class)));
				assertThat(converters.get(0).getSupportedMediaTypes(), hasItem(MediaTypes.HAL_JSON));
			}
		}

		assertThat(found, is(true));
	}

	/**
	 * @see #293
	 */
	@Test
	public void registersHttpMessageConvertersForRestTemplate() {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);
		RestTemplate template = context.getBean(RestTemplate.class);

		assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes(), hasItem(MediaTypes.HAL_JSON));
		context.close();
	}

	/**
	 * @see #341
	 */
	@Test
	public void configuresDefaultObjectMapperForHalToIgnoreUnknownProperties() {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);
		ObjectMapper mapper = context.getBean("_halObjectMapper", ObjectMapper.class);

		assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES), is(false));
		context.close();
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

		ApplicationContext context = new AnnotationConfigApplicationContext(configClass);
		assertEntityLinksSetUp(context);
		assertThat(context.getBean(LinkDiscoverer.class), is(instanceOf(HalLinkDiscoverer.class)));
		assertThat(context.getBean(ObjectMapper.class), is(notNullValue()));

		RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
		assertThat(rmha.getMessageConverters(),
				Matchers.<HttpMessageConverter<?>> hasItems(instanceOf(MappingJackson2HttpMessageConverter.class)));

		AnnotationMethodHandlerAdapter amha = context.getBean(AnnotationMethodHandlerAdapter.class);
		assertThat(Arrays.asList(amha.getMessageConverters()),
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

	@Configuration
	@Import(DelegateConfig.class)
	static class HalConfig {

		static int numberOfMessageConverters = 0;
		static int numberOfMessageConvertersLegacy = 0;

		@Bean
		public RequestMappingHandlerAdapter rmh() {
			RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
			numberOfMessageConverters = adapter.getMessageConverters().size();
			return adapter;
		}

		@Bean
		public AnnotationMethodHandlerAdapter amha() {
			AnnotationMethodHandlerAdapter adapter = new AnnotationMethodHandlerAdapter();
			numberOfMessageConvertersLegacy = adapter.getMessageConverters().length;
			return adapter;
		}

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
}
