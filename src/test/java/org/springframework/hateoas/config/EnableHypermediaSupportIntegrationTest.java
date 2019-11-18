/*
 * Copyright 2013-2020 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.mediatype.hal.HalConfiguration.RenderSingleLinks.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsLinkDiscoverer;
import org.springframework.hateoas.mediatype.uber.UberLinkDiscoverer;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.DelegatingEntityLinks;
import org.springframework.hateoas.server.core.DelegatingLinkRelationProvider;
import org.springframework.hateoas.server.mvc.TypeConstrainedMappingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
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
 * @author Greg Turnquist
 */
@ExtendWith(MockitoExtension.class)
class EnableHypermediaSupportIntegrationTest {

	@Test
	void bootstrapHalConfiguration() {
		assertHalSetupForConfigClass(HalConfig.class);
	}

	@Test
	void bootstrapHalFormsConfiguration() {
		assertHalFormsSetupForConfigClass(HalFormsConfig.class);
	}

	@Test
	void bootstrapJsonCollectionConfiguration() {
		assertCollectionJsonSetupForConfigClass(CollectionJsonConfig.class);
	}

	@Test
	void bootstrapUberConfiguration() {
		assertUberSetupForConfigClass(UberConfig.class);
	}

	@Test
	void registersHalLinkDiscoverers() {

		withServletContext(HalConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON))
					.hasValueSatisfying(HalLinkDiscoverer.class::isInstance);
			assertRelProvidersSetUp(context);
		});
	}

	@Test // #833
	void registersHalFormsLinkDiscoverers() {

		withServletContext(HalFormsConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_FORMS_JSON))
					.hasValueSatisfying(HalFormsLinkDiscoverer.class::isInstance);
			assertRelProvidersSetUp(context);
		});
	}

	@Test // #833
	void registersHalAndHalFormsLinkDiscoverers() {

		withServletContext(HalAndHalFormsConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_JSON))
					.hasValueSatisfying(HalLinkDiscoverer.class::isInstance);

			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.HAL_FORMS_JSON))
					.hasValueSatisfying(HalFormsLinkDiscoverer.class::isInstance);

			assertRelProvidersSetUp(context);
		});
	}

	@Test
	void registersCollectionJsonLinkDiscoverers() {

		withServletContext(CollectionJsonConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.COLLECTION_JSON))
					.hasValueSatisfying(CollectionJsonLinkDiscoverer.class::isInstance);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	void registersUberLinkDiscoverers() {

		withServletContext(UberConfig.class, context -> {

			LinkDiscoverers discoverers = context.getBean(LinkDiscoverers.class);

			assertThat(discoverers).isNotNull();
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.UBER_JSON))
					.hasValueSatisfying(UberLinkDiscoverer.class::isInstance);
			assertRelProvidersSetUp(context);
		});
	}

	@Test
	void bootstrapsHalConfigurationForSubclass() {
		assertHalSetupForConfigClass(ExtendedHalConfig.class);
	}

	@Test
	void bootstrapsHalFormsConfigurationForSubclass() {
		assertHalFormsSetupForConfigClass(ExtendedHalFormsConfig.class);
	}

	@Test
	void bootstrapsCollectionJsonConfigurationForSubclass() {
		assertCollectionJsonSetupForConfigClass(ExtendedCollectionJsonConfig.class);
	}

	@Test
	void bootstrapsUberConfigurationForSubclass() {
		assertUberSetupForConfigClass(ExtendedUberConfig.class);
	}

	/**
	 * @see #134, #219
	 */
	@Test
	@SuppressWarnings("unchecked")
	void halSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withServletContext(HalConfig.class, context -> {

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			assertThat(adapter.getMessageConverters().get(0).getSupportedMediaTypes()) //
					.contains(MediaTypes.HAL_JSON);

			boolean found = false;

			for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

				if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

					found = true;

					AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
					List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
							.getField(processor, "messageConverters");

					assertThat(converters.get(0)).isInstanceOfSatisfying(TypeConstrainedMappingJackson2HttpMessageConverter.class,
							it -> assertThat(it.getSupportedMediaTypes()).contains(MediaTypes.HAL_JSON));
				}
			}

			assertThat(found).isTrue();
		});
	}

	@Test
	@SuppressWarnings("unchecked")
	void halFormsSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

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
	void collectionJsonSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

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
	void uberSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

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
	void registersHalHttpMessageConvertersForRestTemplate() {

		withServletContext(HalConfig.class, context -> {

			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes()) //
					.contains(MediaTypes.HAL_JSON);
		});
	}

	@Test
	void registersHalFormsHttpMessageConvertersForRestTemplate() {

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
	void registersCollectionJsonHttpMessageConvertersForRestTemplate() {

		withServletContext(CollectionJsonConfig.class, context -> {
			RestTemplate template = context.getBean(RestTemplate.class);

			assertThat(template.getMessageConverters().get(0).getSupportedMediaTypes()).hasSize(1)
					.contains(MediaTypes.COLLECTION_JSON);
		});
	}

	@Test
	void registersUberHttpMessageConvertersForRestTemplate() {

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
	void configuresDefaultObjectMapperForHalToIgnoreUnknownProperties() {

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
	void configuresDefaultObjectMapperForHalFormsToIgnoreUnknownProperties() {

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
	void configuresDefaultObjectMapperForCollectionJsonToIgnoreUnknownProperties() {

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
	void configuresDefaultObjectMapperForUberToIgnoreUnknownProperties() {

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
	void verifyDefaultHalConfigurationRendersSingleItemAsSingleItem() throws JsonProcessingException {

		withServletContext(HalConfig.class, context -> {

			RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

			Optional<ObjectMapper> mapper = adapter.getMessageConverters().stream() //
					.filter(it -> it.getSupportedMediaTypes().contains(MediaType.parseMediaType("application/hal+json"))) //
					.findFirst() //
					.map(AbstractJackson2HttpMessageConverter.class::cast) //
					.map(AbstractJackson2HttpMessageConverter::getObjectMapper);

			assertThat(mapper).hasValueSatisfying(it -> {

				RepresentationModel<?> resourceSupport = new RepresentationModel<>();
				resourceSupport.add(Link.of("localhost").withSelfRel());

				assertThatCode(() -> {
					assertThat(it.writeValueAsString(resourceSupport)) //
							.isEqualTo("{\"_links\":{\"self\":{\"href\":\"localhost\"}}}");
				}).doesNotThrowAnyException();
			});
		});
	}

	@Test
	void verifyRenderSingleLinkAsArrayViaOverridingBean() {

		withServletContext( //
				RenderLinkAsSingleLinksConfig.class, //
				context -> assertObjectMapper( //
						context, //
						MediaTypes.HAL_JSON, //
						mapper -> { //
							RepresentationModel<?> resourceSupport = new RepresentationModel<>(); //
							resourceSupport.add(Link.of("localhost").withSelfRel()); //
							assertThat(mapper.writeValueAsString(resourceSupport)) //
									.isEqualTo("{\"_links\":{\"self\":[{\"href\":\"localhost\"}]}}"); //
						} //
				) //
		);
	}

	@Test // #1019
	void registersNoOpMessageResolverIfMessagesBundleMissing() {

		withServletContext(HateoasConfiguration.class, //
				context -> {
					assertThat(context.getBean(MessageResolver.class)).isEqualTo(MessageResolver.of(null));
				});
	}

	@Test // #1019
	void registersMessageResolverIfMessagesBundleAvailable() {

		String originalBaseName = HateoasConfiguration.I18N_BASE_NAME;

		try {

			HateoasConfiguration.I18N_BASE_NAME = "org/springframework/hateoas/config/rest-messages";

			withServletContext(HateoasConfiguration.class, simulateResourceBundle(), context -> {

				MessageResolver bean = context.getBean(MessageResolver.class);

				assertThat(bean).isNotEqualTo(MessageResolver.of(null));
				assertThat(bean.resolve(() -> new String[] { "key" })).isEqualTo("Schlüssel");
			});

		} finally {
			HateoasConfiguration.I18N_BASE_NAME = originalBaseName;
		}
	}

	@Test // #1019, DATAREST-686
	void defaultsEncodingOfMessageSourceToUtf8() throws Exception {

		withServletContext(HalConfig.class, simulateResourceBundle(), context -> {

			MessageResolver resolver = context.getBean(MessageResolver.class);

			Object accessor = ReflectionTestUtils.getField(resolver, "accessor");
			Object messageSource = ReflectionTestUtils.getField(accessor, "messageSource");

			assertThat((String) ReflectionTestUtils.getField(messageSource, "defaultEncoding")).isEqualTo("UTF-8");
		});
	}

	private static void assertEntityLinksSetUp(ApplicationContext context) {

		assertThat(context.getBeansOfType(EntityLinks.class).values()) //
				.anySatisfy(it -> assertThat(it).isInstanceOf(DelegatingEntityLinks.class));
	}

	private static void assertRelProvidersSetUp(ApplicationContext context) {

		assertThat(context.getBeansOfType(LinkRelationProvider.class).values()) //
				.anySatisfy(it -> assertThat(it).isInstanceOf(DelegatingLinkRelationProvider.class));
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

	private static <T extends AnnotationConfigWebApplicationContext> Function<T, T> simulateResourceBundle() {

		return context -> {

			T spy = Mockito.spy(context);

			ClassPathResource resource = new ClassPathResource("rest-messages.properties",
					EnableHypermediaSupportIntegrationTest.class);
			assertThat(resource.exists()).isTrue();

			try {

				doReturn(new Resource[0]).when(spy).getResources("classpath:rest-default-messages.properties");
				doReturn(new Resource[] { resource }).when(spy).getResources(contains("rest-messages"));

			} catch (IOException o_O) {
				fail("Couldn't mock resource lookup!", o_O);
			}

			return spy;
		};
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

	@EnableHypermediaSupport(type = { HypermediaType.HAL, HypermediaType.HAL_FORMS })
	static class HalAndHalFormsConfig {

	}
}
