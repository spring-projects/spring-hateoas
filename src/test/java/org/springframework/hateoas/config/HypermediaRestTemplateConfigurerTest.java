package org.springframework.hateoas.config;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class HypermediaRestTemplateConfigurerTest {

	private static MediaType FRODO_JSON = MediaType.parseMediaType("application/frodo+json");

	@Test // #1223
	void webConvertersShouldAddHypermediaMessageConverters() {

		withContext(HalConfig.class, context -> {

			HypermediaRestTemplateConfigurer configurer = context.getBean(HypermediaRestTemplateConfigurer.class);

			RestTemplate restTemplate = configurer.registerHypermediaTypes(new RestTemplate());

			assertThat(restTemplate.getMessageConverters()).flatExtracting(HttpMessageConverter::getSupportedMediaTypes)
					.contains(MediaTypes.HAL_JSON) //
					.doesNotContain(MediaTypes.HAL_FORMS_JSON, MediaTypes.COLLECTION_JSON, MediaTypes.UBER_JSON);
		});
	}

	@Test // #1223
	void webConvertersShouldAddAllHypermediaMessageConverters() {

		withContext(AllHypermediaConfig.class, context -> {

			HypermediaRestTemplateConfigurer configurer = context.getBean(HypermediaRestTemplateConfigurer.class);

			RestTemplate restTemplate = configurer.registerHypermediaTypes(new RestTemplate());

			assertThat(restTemplate.getMessageConverters()).flatExtracting(HttpMessageConverter::getSupportedMediaTypes)
					.contains(MediaTypes.HAL_JSON, MediaTypes.HAL_FORMS_JSON, MediaTypes.COLLECTION_JSON, MediaTypes.UBER_JSON);
		});
	}

	@Test // #1223
	void webConvertersShouldSupportCustomHypermediaTypes() {

		withContext(CustomHypermediaConfig.class, context -> {

			HypermediaRestTemplateConfigurer configurer = context.getBean(HypermediaRestTemplateConfigurer.class);

			RestTemplate restTemplate = configurer.registerHypermediaTypes(new RestTemplate());

			assertThat(restTemplate.getMessageConverters()).flatExtracting(HttpMessageConverter::getSupportedMediaTypes)
					.contains(MediaTypes.HAL_JSON, FRODO_JSON)
					.doesNotContain(MediaTypes.HAL_FORMS_JSON, MediaTypes.COLLECTION_JSON, MediaTypes.UBER_JSON);
		});
	}

	@EnableHypermediaSupport(type = HAL)
	static class HalConfig {

	}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, COLLECTION_JSON, UBER })
	static class AllHypermediaConfig {

	}

	static class CustomHypermediaConfig extends HalConfig {

		@Bean
		HypermediaMappingInformation hypermediaMappingInformation() {
			return () -> Collections.singletonList(FRODO_JSON);
		}
	}
}
