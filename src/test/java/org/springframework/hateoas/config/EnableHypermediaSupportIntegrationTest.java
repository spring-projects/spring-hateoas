/*
 * Copyright 2013-2019 the original author or authors.
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
import static org.springframework.hateoas.hal.HalConfiguration.RenderSingleLinks.*;
import static org.springframework.hateoas.support.ContextTester.withServletContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
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
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.hateoas.core.DelegatingRelProvider;
import org.springframework.hateoas.hal.HalConfiguration;
import org.springframework.hateoas.hal.HalLinkDiscoverer;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;
import org.springframework.hateoas.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.hateoas.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.hateoas.uber.UberLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
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

	@Test
	public void bootstrapJsonCollectionConfiguration() {
		assertCollectionJsonSetupForConfigClass(CollectionJsonConfig.class);
	}

	@Test
	public void bootstrapUberConfiguration() {
		assertUberSetupForConfigClass(UberConfig.class);
	}

	@Test
	public void registersHalLinkDiscoverers() {

		withServletContext(HalConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON))
					.hasValueSatisfying(HalLinkDiscoverer.class::isInstance);
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON_UTF8))
					.hasValueSatisfying(HalLinkDiscoverer.class::isInstance);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	public void registersHalFormsLinkDiscoverers() {

		withServletContext(HalFormsConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_FORMS_JSON))
					.hasValueSatisfying(HalFormsLinkDiscoverer.class::isInstance);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	public void registersCollectionJsonLinkDiscoverers() {

		withServletContext(CollectionJsonConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.COLLECTION_JSON))
					.hasValueSatisfying(CollectionJsonLinkDiscoverer.class::isInstance);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	public void registersUberLinkDiscoverers() {

		withServletContext(UberConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.UBER_JSON))
					.hasValueSatisfying(UberLinkDiscoverer.class::isInstance);
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

	@Test
	public void bootstrapsUberConfigurationForSubclass() {
		assertUberSetupForConfigClass(ExtendedUberConfig.class);
	}

	/**
	 * @see #134, #219
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void halSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withServletContext(HalConfig.class, context -> {

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

		withServletContext(HalFormsConfig.class, context -> {

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes()).hasSize(1)
					.contains(MediaTypes.HAL_FORMS_JSON);

			boolean found = false;

			for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

				if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

					found = true;

					AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
					List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
							.getField(processor, "messageConverters");

					assertThat(converters.get(0)).isInstanceOf(TypeConstrainedMappingJackson2HttpMessageConverter.class);
					assertThat(converters.get(0).getSupportedMediaTypes()).hasSize(1).contains(MediaTypes.HAL_FORMS_JSON);
				}
			}

			assertThat(found).isTrue();
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void collectionJsonSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withServletContext(CollectionJsonConfig.class, context -> {

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes()).hasSize(1)
					.contains(MediaTypes.COLLECTION_JSON);

			boolean found = false;

			for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

				if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

					found = true;

					AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
					List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
							.getField(processor, "messageConverters");

					assertThat(converters.get(0)).isInstanceOf(TypeConstrainedMappingJackson2HttpMessageConverter.class);
					assertThat(converters.get(0).getSupportedMediaTypes()).hasSize(1).contains(MediaTypes.COLLECTION_JSON);
				}
			}

			assertThat(found).isTrue();
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	public void uberSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withServletContext(UberConfig.class, context -> {

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes()) //
					.hasSize(1) //
					.contains(MediaTypes.UBER_JSON);

			boolean found = false;

			for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

				if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

					found = true;

					AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
					List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
							.getField(processor, "messageConverters");

					assertThat(converters.get(0)).isInstanceOf(TypeConstrainedMappingJackson2HttpMessageConverter.class);
					assertThat(converters.get(0).getSupportedMediaTypes()) //
							.hasSize(1) //
							.contains(MediaTypes.UBER_JSON);
				}
			}

			assertThat(found).isTrue();
		});
	}

	/**
	 * @see #293
	 */
	@Test
	public void registersHalHttpMessageConvertersForRestTemplate() {

		withServletContext(HalConfig.class, context -> {

			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes()) //
					.contains(MediaTypes.HAL_JSON, MediaTypes.HAL_JSON_UTF8);
		});
	}

	@Test
	public void registersHalFormsHttpMessageConvertersForRestTemplate() {

		withServletContext( //
				HalFormsConfig.class, //
				context -> foo( //
						context, //
						RestTemplate.class, //
						it -> it.getMessageConverters().get(0), //
						converter -> assertThat(converter.getSupportedMediaTypes()) // //
								.hasSize(1) //
								.contains(MediaTypes.HAL_FORMS_JSON) //
				) //
		);
	}

	private static <T, S> void foo(ApplicationContext context, Class<T> beanType, Function<T, S> extractor,
			ThrowingConsumer<S> consumer) {

		T bean = context.getBean(beanType);
		S result = extractor.apply(bean);

		try {
			consumer.accept(result);
		} catch (Throwable o_O) {
			throw new RuntimeException(o_O);
		}
	}

	private static <T, S> void assertObjectMapper(ApplicationContext context, MediaType mediaType,
			ThrowingConsumer<ObjectMapper> consumer) {

		Function<RequestMappingHandlerAdapter, ObjectMapper> mapper = adapter -> {

			Optional<ObjectMapper> result = adapter.getMessageConverters().stream()//
					.filter(it -> it.getSupportedMediaTypes().contains(mediaType)).findFirst() //
					.map(AbstractJackson2HttpMessageConverter.class::cast) //
					.map(AbstractJackson2HttpMessageConverter::getObjectMapper);

			if (!result.isPresent()) {
				fail("Couldn't find ObjectMapper from HttpMessageConverter supporting " + mediaType);
			}

			return result.orElseThrow(IllegalStateException::new);
		};

		foo(context, RequestMappingHandlerAdapter.class, mapper, consumer);
	}

	interface ThrowingConsumer<T> {
		void accept(T source) throws Throwable;
	}

	@Test
	public void registersCollectionJsonHttpMessageConvertersForRestTemplate() {

		withServletContext(CollectionJsonConfig.class, context -> {
			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes()).hasSize(1)
					.contains(MediaTypes.COLLECTION_JSON);
		});
	}

	@Test
	public void registersUberHttpMessageConvertersForRestTemplate() {

		withServletContext(UberConfig.class, context -> {
			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes()) //
					.hasSize(1) //
					.contains(MediaTypes.UBER_JSON);
		});
	}

	/**
	 * @see #341
	 */
	@Test
	public void configuresDefaultObjectMapperForHalToIgnoreUnknownProperties() {

		withServletContext( //
				HalConfig.class, //
				context -> assertObjectMapper( //
						context, //
						MediaTypes.HAL_JSON, //
						mapper -> assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) //
								.isFalse() //
				) //
		);
	}

	/**
	 * @see #341
	 */
	@Test
	public void configuresDefaultObjectMapperForHalFormsToIgnoreUnknownProperties() {

		withServletContext( //
				HalFormsConfig.class, //
				context -> assertObjectMapper( //
						context, //
						MediaTypes.HAL_FORMS_JSON, //
						mapper -> assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) //
								.isFalse() //
				) //
		);
	}

	@Test
	public void configuresDefaultObjectMapperForCollectionJsonToIgnoreUnknownProperties() {

		withServletContext( //
				CollectionJsonConfig.class, //
				context -> assertObjectMapper( //
						context, //
						MediaTypes.COLLECTION_JSON, //
						mapper -> assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) //
								.isFalse() //
				) //
		);
	}

	@Test
	public void configuresDefaultObjectMapperForUberToIgnoreUnknownProperties() {

		withServletContext( //
				UberConfig.class, //
				context -> assertObjectMapper( //
						context, //
						MediaTypes.UBER_JSON, //
						mapper -> assertThat(mapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) //
								.isFalse() //
				) //
		);
	}

	@Test
	public void verifyDefaultHalConfigurationRendersSingleItemAsSingleItem() throws JsonProcessingException {

		withServletContext(HalConfig.class, context -> {

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			Optional<ObjectMapper> mapper = adapter.getMessageConverters().stream() //
					.filter(it -> it.getSupportedMediaTypes().contains(MediaType.parseMediaType("application/hal+json"))) //
					.findFirst() //
					.map(AbstractJackson2HttpMessageConverter.class::cast) //
					.map(AbstractJackson2HttpMessageConverter::getObjectMapper);

			assertThat(mapper).hasValueSatisfying(it -> {

				ResourceSupport resourceSupport = new ResourceSupport();
				resourceSupport.add(new Link("localhost").withSelfRel());

				assertThatCode(() -> {
					assertThat(it.writeValueAsString(resourceSupport)) //
							.isEqualTo("{\"_links\":{\"self\":{\"href\":\"localhost\"}}}");
				}).doesNotThrowAnyException();
			});
		});
	}

	@Test
	public void verifyRenderSingleLinkAsArrayViaOverridingBean() {

		withServletContext( //
				RenderLinkAsSingleLinksConfig.class, //
				context -> assertObjectMapper( //
						context, //
						MediaTypes.HAL_JSON, //
						mapper -> { //
							ResourceSupport resourceSupport = new ResourceSupport(); //
							resourceSupport.add(new Link("localhost").withSelfRel()); //
							assertThat(mapper.writeValueAsString(resourceSupport)) //
									.isEqualTo("{\"_links\":{\"self\":[{\"href\":\"localhost\"}]}}"); //
						} //
				) //
		);
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

		withServletContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(HalLinkDiscoverer.class);

			RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
			assertThat(rmha.getMessageConverters())
					.anySatisfy(it -> assertThat(it).isInstanceOf(MappingJackson2HttpMessageConverter.class));
		});
	}

	private static void assertHalFormsSetupForConfigClass(Class<?> configClass) {

		withServletContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(HalFormsLinkDiscoverer.class);

			RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
			assertThat(rmha.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);

		});
	}

	private static void assertCollectionJsonSetupForConfigClass(Class<?> configClass) {

		withServletContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(CollectionJsonLinkDiscoverer.class);

			RequestMappingHandlerAdapter rmha = context.getBean(RequestMappingHandlerAdapter.class);
			assertThat(rmha.getMessageConverters().get(0)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
		});
	}

	private static void assertUberSetupForConfigClass(Class<?> configClass) {

		withServletContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(UberLinkDiscoverer.class);

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
	@EnableWebMvc
	@Import(DelegateConfig.class)
	static class HalConfig {

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

	@EnableWebMvc
	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class RenderLinkAsSingleLinksConfig {

		@Bean
		HalConfiguration halConfiguration() {
			return new HalConfiguration().withRenderSingleLinks(AS_ARRAY);
		}
	}

	@Import(DelegateHalFormsHypermediaConfig.class)
	static class HalFormsConfig {

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

	@EnableWebMvc
	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class DelegateHalFormsHypermediaConfig {

	}

	interface ConsumerWithException<T, E extends Exception> {

		void accept(T element) throws E;
	}

	@EnableWebMvc
	@Configuration
	@Import(AlternateDelegateConfig.class)
	static class CollectionJsonConfig {

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

	@EnableWebMvc
	@Configuration
	@Import(DelegateUberHypermediaConfig.class)
	static class UberConfig {

		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}

	@Configuration
	static class ExtendedUberConfig extends UberConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.UBER)
	static class DelegateUberHypermediaConfig {

	}
}
