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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.config.HypermediaSupportBeanDefinitionRegistrar.*;
import static org.springframework.hateoas.hal.HalConfiguration.RenderSingleLinks.*;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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
import org.springframework.hateoas.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.HypermediaSupportBeanDefinitionRegistrar.Jackson2ModuleRegisteringBeanPostProcessor;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for {@link EnableHypermediaSupport}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@RunWith(MockitoJUnitRunner.class)
public class EnableHypermediaSupportIntegrationTest {

	@Test
	public void bootstrapHalConfiguration() {
		assertHalSetupForConfigClass(HalConfig.class);
	}

	@Test
	public void bootstrapHalFormsConfiguration() {
		assertHalFormsSetupForConfigClass(HalFormsConfig.class);
	}

	public void bootstrapJsonCollectionConfiguration() {
		assertCollectionJsonSetupForConfigClass(CollectionJsonConfig.class);
	}

	@Test
	public void registersHalLinkDiscoverers() {

		withContext(HalConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON)).isInstanceOf(HalLinkDiscoverer.class);
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON_UTF8)).isInstanceOf(HalLinkDiscoverer.class);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	public void registersHalFormsLinkDiscoverers() {

		withContext(HalFormsConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_FORMS_JSON)).isInstanceOf(HalFormsLinkDiscoverer.class);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	public void registersCollectionJsonLinkDiscoverers() {

		withContext(CollectionJsonConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.COLLECTION_JSON)).isInstanceOf(CollectionJsonLinkDiscoverer.class);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	public void bootstrapsHalConfigurationForSubclass() {
		assertHalSetupForConfigClass(ExtendedHalConfig.class);
	}

	@Test
	public void bootstrapsHalFormsConfigurationForSubclass() {
		assertHalFormsSetupForConfigClass(ExtendedHalFormsConfig.class);
	}

	@Test
	public void bootstrapsCollectionJsonConfigurationForSubclass() {
		assertCollectionJsonSetupForConfigClass(ExtendedCollectionJsonConfig.class);
	}

	/**
	 * @see #134, #219
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void halSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withContext(HalConfig.class, context -> {

			Jackson2ModuleRegisteringBeanPostProcessor postProcessor = new Jackson2ModuleRegisteringBeanPostProcessor();
			postProcessor.setBeanFactory(context.getAutowireCapableBeanFactory());

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes()) //
					.contains(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);

			boolean found = false;

			for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

				if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

					found = true;

					AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
					List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
							.getField(processor, "messageConverters");

					assertThat(converters.get(0)).isInstanceOfSatisfying(TypeConstrainedMappingJackson2HttpMessageConverter.class,
							it -> assertThat(it.getSupportedMediaTypes()).contains(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8));
				}
			}

			assertThat(found).isTrue();
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void halFormsSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withContext(HalFormsConfig.class, context -> {

			Jackson2ModuleRegisteringBeanPostProcessor postProcessor = new Jackson2ModuleRegisteringBeanPostProcessor();
			postProcessor.setBeanFactory(context.getAutowireCapableBeanFactory());

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes())
				.hasSize(1)
				.contains(MediaTypes.HAL_FORMS_JSON);

			boolean found = false;

			for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

				if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

					found = true;

					AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
					List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
						.getField(processor, "messageConverters");

					assertThat(converters.get(0)).isInstanceOf(TypeConstrainedMappingJackson2HttpMessageConverter.class);
					assertThat(converters.get(0).getSupportedMediaTypes())
						.hasSize(1)
						.contains(MediaTypes.HAL_FORMS_JSON);
				}
			}

			assertThat(found).isTrue();
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectionJsonSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withContext(CollectionJsonConfig.class, context -> {

			Jackson2ModuleRegisteringBeanPostProcessor postProcessor = new Jackson2ModuleRegisteringBeanPostProcessor();
			postProcessor.setBeanFactory(context.getAutowireCapableBeanFactory());

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes())
				.hasSize(1)
				.contains(MediaTypes.COLLECTION_JSON);

			boolean found = false;

			for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

				if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

					found = true;

					AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
					List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
						.getField(processor, "messageConverters");

					assertThat(converters.get(0)).isInstanceOf(TypeConstrainedMappingJackson2HttpMessageConverter.class);
					assertThat(converters.get(0).getSupportedMediaTypes())
						.hasSize(1)
						.contains(MediaTypes.COLLECTION_JSON);
				}
			}

			assertThat(found).isTrue();
		});
	}

	/**
	 * @see #293
	 */
	@Test
	public void registersHttpMessageConvertersForRestTemplate() {

		withContext(HalConfig.class, context -> {

			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes()) //
					.contains(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		});
	}

	@Test
	public void registersHalFormsHttpMessageConvertersForRestTemplate() {

		withContext(HalFormsConfig.class, context -> {
			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes())
				.hasSize(1)
				.contains(MediaTypes.HAL_FORMS_JSON);
		});
	}

	@Test
	public void registersCollectionJsonHttpMessageConvertersForRestTemplate() {

		withContext(CollectionJsonConfig.class, context -> {
			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes())
				.hasSize(1)
				.contains(MediaTypes.COLLECTION_JSON);
		});
	}

	/**
	 * @see #341
	 */
	@Test
	public void configuresDefaultObjectMapperForHalToIgnoreUnknownProperties() {

		withContext(HalConfig.class, context -> {

			ObjectMapper mapper = context.getBean("_halObjectMapper", ObjectMapper.class);

			assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
		});
	}

	/**
	 * @see #341
	 */
	@Test
	public void configuresDefaultObjectMapperForHalFormsToIgnoreUnknownProperties() {

		withContext(HalFormsConfig.class, context -> {

			ObjectMapper mapper = context.getBean("_halFormsObjectMapper", ObjectMapper.class);

			assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
		});
	}

	@Test
	public void configuresDefaultObjectMapperForCollectionJsonToIgnoreUnknownProperties() {

		withContext(CollectionJsonConfig.class, context -> {

			ObjectMapper mapper = context.getBean("_collectionJsonObjectMapper", ObjectMapper.class);

			assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
		});
	}

	@Test
	public void verifyDefaultHalConfigurationRendersSingleItemAsSingleItem() throws JsonProcessingException {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(HalConfig.class);

		ObjectMapper mapper = context.getBean("_halObjectMapper", ObjectMapper.class);

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(mapper.writeValueAsString(resourceSupport))
				.isEqualTo("{\"_links\":{\"self\":{\"href\":\"localhost\"}}}");

		context.close();
	}

	@Test
	public void verifyRenderSingleLinkAsArrayViaOverridingBean() throws JsonProcessingException {

		withContext(RenderLinkAsSingleLinksConfig.class, context -> {

			ObjectMapper mapper = context.getBean("_halObjectMapper", ObjectMapper.class);

			ResourceSupport resourceSupport = new ResourceSupport();
			resourceSupport.add(new Link("localhost").withSelfRel());

			assertThat(mapper.writeValueAsString(resourceSupport))
					.isEqualTo("{\"_links\":{\"self\":[{\"href\":\"localhost\"}]}}");
		});
	}

	private static <E extends Exception> void withContext(Class<?> configuration,
			ConsumerWithException<AnnotationConfigApplicationContext, E> consumer) throws E {

		try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(configuration)) {
			consumer.accept(context);
		}
	}

	private static void assertEntityLinksSetUp(ApplicationContext context) {

		assertThat(context.getBeansOfType(EntityLinks.class).values()) //
				.anySatisfy(it -> assertThat(it).isInstanceOf(DelegatingEntityLinks.class));
	}

	private static void assertRelProvidersSetUp(ApplicationContext context) {

		assertThat(context.getBeansOfType(RelProvider.class).values()) //
				.anySatisfy(it -> assertThat(it).isInstanceOf(DelegatingRelProvider.class));
	}

	private static void assertHalSetupForConfigClass(Class<?> configClass) {

		withContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(HalLinkDiscoverer.class);
			assertThat(context.getBean(ObjectMapper.class)).isNotNull();

			RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
			assertThat(rmha.getMessageConverters())
					.anySatisfy(it -> assertThat(it).isInstanceOf(MappingJackson2HttpMessageConverter.class));
		});
	}

	private static void assertHalFormsSetupForConfigClass(Class<?> configClass) {

		withContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(HalFormsLinkDiscoverer.class);
			assertThat(context.getBean(ObjectMapper.class)).isNotNull();

			RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
			assertThat(rmha.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);

		});
	}

	@SuppressWarnings({ "unchecked" })
	private static void assertCollectionJsonSetupForConfigClass(Class<?> configClass) {

		withContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(CollectionJsonLinkDiscoverer.class);
			assertThat(context.getBean(ObjectMapper.class)).isNotNull();

			RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
			assertThat(rmha.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);

		});
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
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}

		@Bean
		public HalConfiguration halConfiguration() {
			return new HalConfiguration();
		}
	}

	@Configuration
	static class ExtendedHalConfig extends HalConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class DelegateConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class RenderLinkAsSingleLinksConfig {

		@Bean
		HalConfiguration halConfiguration() {
			return new HalConfiguration().withRenderSingleLinks(AS_ARRAY);
		}

		@Bean
		public RequestMappingHandlerAdapter rmh() {
			return new RequestMappingHandlerAdapter();
		}
	}

	@Import(DelegateHalFormsHypermediaConfig.class)
	static class HalFormsConfig {

		static int numberOfMessageConverters = 0;

		@Bean
		public RequestMappingHandlerAdapter rmh() {
			RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
			numberOfMessageConverters = adapter.getMessageConverters().size();
			return adapter;
		}

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}

		@Bean
		public HalFormsConfiguration halFormsConfiguration() {
			return new HalFormsConfiguration();
		}

	}

	@Configuration
	static class ExtendedHalFormsConfig extends HalFormsConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class DelegateHalFormsHypermediaConfig {

	}

	interface ConsumerWithException<T, E extends Exception> {

		void accept(T element) throws E;
	}

	@Configuration
	@Import(AlternateDelegateConfig.class)
	static class CollectionJsonConfig {

		static int numberOfMessageConverters = 0;
		static int numberOfMessageConvertersLegacy = 0;

		@Bean
		public RequestMappingHandlerAdapter rmh() {
			RequestMappingHandlerAdapter adapter = new RequestMappingHandlerAdapter();
			numberOfMessageConverters = adapter.getMessageConverters().size();
			return adapter;
		}

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}

	@Configuration
	static class ExtendedCollectionJsonConfig extends CollectionJsonConfig {

	}

	@EnableHypermediaSupport(type = HypermediaType.COLLECTION_JSON)
	static class AlternateDelegateConfig {

	}
}
