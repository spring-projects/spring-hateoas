package org.springframework.hateoas.config;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.MediaTypes.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

public class HypermediaWebClientConfigurerTest {

	private static MediaType FRODO_JSON = MediaType.parseMediaType("application/frodo+json");

	@Test // #1224
	void webClientConfigurerHandlesSingleHypermediaType() {

		withContext(HalConfig.class, context -> {

			HypermediaWebClientConfigurer configurer = context.getBean(HypermediaWebClientConfigurer.class);

			WebClient webClient = configurer.registerHypermediaTypes(WebClient.builder()).build();

			assertThat(exchangeStrategies(webClient).messageReaders())
					.flatExtracting(HttpMessageReader::getReadableMediaTypes) //
					.contains(HAL_JSON) //
					.doesNotContain(HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	@Test // #1224
	void webClientConfigurerHandlesMultipleHypermediaTypes() {

		withContext(AllHypermediaConfig.class, context -> {

			HypermediaWebClientConfigurer configurer = context.getBean(HypermediaWebClientConfigurer.class);

			WebClient webClient = configurer.registerHypermediaTypes(WebClient.builder()).build();

			assertThat(exchangeStrategies(webClient).messageReaders())
					.flatExtracting(HttpMessageReader::getReadableMediaTypes) //
					.contains(HAL_JSON, HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	@Test // #1224
	void webClientConfigurerHandlesCustomHypermediaTypes() {

		withContext(CustomHypermediaConfig.class, context -> {

			HypermediaWebClientConfigurer configurer = context.getBean(HypermediaWebClientConfigurer.class);

			WebClient webClient = configurer.registerHypermediaTypes(WebClient.builder()).build();

			assertThat(exchangeStrategies(webClient).messageReaders())
					.flatExtracting(HttpMessageReader::getReadableMediaTypes) //
					.contains(HAL_JSON, FRODO_JSON) //
					.doesNotContain(HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	/**
	 * Extract the {@link ExchangeStrategies} from a {@link WebTestClient} to assert it has the proper message readers and
	 * writers.
	 *
	 * @param webClient
	 * @return
	 */
	private static ExchangeStrategies exchangeStrategies(WebClient webClient) {

		return (ExchangeStrategies) ReflectionTestUtils
				.getField(ReflectionTestUtils.getField(webClient, "exchangeFunction"), "strategies");
	}

	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig {

	}

	@EnableHypermediaSupport(
			type = { HypermediaType.HAL, HypermediaType.HAL_FORMS, HypermediaType.COLLECTION_JSON, HypermediaType.UBER })
	static class AllHypermediaConfig {

	}

	static class CustomHypermediaConfig extends HalConfig {

		@Bean
		HypermediaMappingInformation hypermediaMappingInformation() {
			return () -> Collections.singletonList(FRODO_JSON);
		}
	}
}
