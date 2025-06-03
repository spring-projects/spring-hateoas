/*
 * Copyright 2013-2024 the original author or authors.
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
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
import org.jspecify.annotations.Nullable;

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
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.VND_HAL_JSON))
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
			assertThat(discoverers.getLinkDiscovererFor(MediaTypes.VND_HAL_JSON))
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

	@Test
	void collectionJsonSetupIsAppliedToAllTransitiveComponentsInRequestMappingHandlerAdapter() {

		withServletContext(CollectionJsonConfig.class, context -> {
			assertMediaTypeSupported(context, MediaTypes.COLLECTION_JSON, RepresentationModel.class);
		});
	}

	private static Object assertMediaTypeSupported(ApplicationContext context, MediaType mediaType, Class<?> type) {
		return assertMediaTypeSupported(context, mediaType, type, null);
	}

	@Nullable
	private static String assertMediaTypeSupported(ApplicationContext context, MediaType mediaType, Class<?> type,
			@Nullable Object source) {

		context.getBeanProvider(RestTemplate.class).ifAvailable(it -> {
			assertMediaTypeSupported(it.getMessageConverters(), mediaType, type, source);
		});

		RequestMappingHandlerAdapter adapter = context.getBean(RequestMappingHandlerAdapter.class);

		boolean found = false;

		for (HandlerMethodArgumentResolver resolver : getResolvers(adapter)) {

			if (resolver instanceof AbstractMessageConverterMethodArgumentResolver) {

				found = true;

				AbstractMessageConverterMethodArgumentResolver processor = (AbstractMessageConverterMethodArgumentResolver) resolver;
				List<HttpMessageConverter<?>> converters = (List<HttpMessageConverter<?>>) ReflectionTestUtils
						.getField(processor, "messageConverters");

				assertMediaTypeSupported(converters, MediaTypes.HAL_FORMS_JSON, RepresentationModel.class);
			}
		}

		assertThat(found).isTrue();

		return assertMediaTypeSupported(adapter.getMessageConverters(), mediaType, type, source);
	}

	@Nullable
	private static Object assertMediaTypeSupported(List<HttpMessageConverter<?>> converters, MediaType mediaType,
			Class<?> type) {
		return assertMediaTypeSupported(converters, mediaType, type, null);
	}

	@Nullable
	private static String assertMediaTypeSupported(List<HttpMessageConverter<?>> converters, MediaType mediaType,
			Class<?> type, @Nullable Object source) {

		Optional<AbstractJackson2HttpMessageConverter> result = converters.stream()//
				.filter(AbstractJackson2HttpMessageConverter.class::isInstance) //
				.findFirst() //
				.map(AbstractJackson2HttpMessageConverter.class::cast);

		assertThat(result).hasValueSatisfying(it -> {
			assertThat(it.getSupportedMediaTypes(type));
		});

		if (source == null) {
			return null;
		}

		HttpMessageConverter<Object> converter = result.get();
		MockHttpOutputMessage message = new MockHttpOutputMessage();

		assertThatCode(() -> converter.write(source, mediaType, message)).doesNotThrowAnyException();

		return message.getBodyAsString();
	}

	@Test
	void verifyDefaultHalConfigurationRendersSingleItemAsSingleItem() throws JsonProcessingException {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(Link.of("localhost").withSelfRel());

		withServletContext(HalConfig.class, context -> {

			assertMediaTypeSupported(context.getBean(RestTemplate.class).getMessageConverters(), MediaTypes.HAL_FORMS_JSON,
					RepresentationModel.class);

			String result = assertMediaTypeSupported(context, MediaTypes.HAL_JSON, RepresentationModel.class,
					resourceSupport);

			assertThat(result).isEqualTo("{\"_links\":{\"self\":{\"href\":\"localhost\"}}}");
		});
	}

	@Test
	void verifyRenderSingleLinkAsArrayViaOverridingBean() {

		RepresentationModel<?> model = new RepresentationModel<>(); //
		model.add(Link.of("localhost").withSelfRel()); //

		withServletContext(RenderLinkAsSingleLinksConfig.class, context -> {

			String result = assertMediaTypeSupported(context, MediaTypes.HAL_JSON, RepresentationModel.class, model);

			assertThat(result).isEqualTo("{\"_links\":{\"self\":[{\"href\":\"localhost\"}]}}"); //
		});
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

	/*
	 * HAL FORMS, UBER
	 * RepresentationModel -> hal-forms, application/json, application/*+json
	 * RepresentationModel -> uber, application/json, application/*+json
	 *
	 * hal-forms, uber, application/json, application/*+json
	 */
	@Test
	void ordersMediaTypeIntegrationBasedOnConfiguration() {

		withServletContext(MediaTypeOrdering.class, context -> {

			WebConverters converters = context.getBean(WebConverters.class);

			assertThat(converters.getSupportedMediaTypes()) //
					.containsExactly(MediaTypes.UBER_JSON, MediaTypes.HAL_FORMS_JSON);
		});
	}

	@Test // #1521
	void bootstrapsWithOutDefaultMediaTypeEnabled() {

		assertThatNoException().isThrownBy(() -> {
			withServletContext(NoDefaultMediaTypes.class, context -> {});
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
			assertMediaTypeSupported(context, MediaTypes.HAL_FORMS_JSON, RepresentationModel.class);
		});
	}

	private static void assertCollectionJsonSetupForConfigClass(Class<?> configClass) {

		withServletContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(CollectionJsonLinkDiscoverer.class);
			assertMediaTypeSupported(context, MediaTypes.COLLECTION_JSON, RepresentationModel.class);
		});
	}

	private static void assertUberSetupForConfigClass(Class<?> configClass) {

		withServletContext(configClass, context -> {

			assertEntityLinksSetUp(context);
			assertThat(context.getBean(LinkDiscoverer.class)).isInstanceOf(UberLinkDiscoverer.class);
			assertMediaTypeSupported(context, MediaTypes.UBER_JSON, RepresentationModel.class);
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

	@Configuration
	@EnableHypermediaSupport(type = { HypermediaType.UBER, HypermediaType.HAL_FORMS })
	static class MediaTypeOrdering {

	}

	@Configuration
	@EnableHypermediaSupport(type = {})
	static class NoDefaultMediaTypes {}
}
