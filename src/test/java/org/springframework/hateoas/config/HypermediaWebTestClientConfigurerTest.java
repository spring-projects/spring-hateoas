package org.springframework.hateoas.config;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.MediaTypes.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.hateoas.support.Employee;
import org.springframework.hateoas.support.WebFluxEmployeeController;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

public class HypermediaWebTestClientConfigurerTest {

	private static MediaType FRODO_JSON = MediaType.parseMediaType("application/frodo+json");

	@Test // #1225
	void webTestClientConfigurerHandlesSingleHypermediaType() {

		withContext(HalConfig.class, context -> {

			HypermediaWebTestClientConfigurer configurer = context.getBean(HypermediaWebTestClientConfigurer.class);

			WebTestClient webTestClient = WebTestClient.bindToServer().apply(configurer).build();

			assertThat(exchangeStrategies(webTestClient).messageReaders())
					.flatExtracting(HttpMessageReader::getReadableMediaTypes) //
					.contains(HAL_JSON) //
					.doesNotContain(HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	@Test // #1225
	void webTestClientConfigurerHandlesAllHypermediaTypes() {

		withContext(AllHypermediaConfig.class, context -> {

			HypermediaWebTestClientConfigurer configurer = context.getBean(HypermediaWebTestClientConfigurer.class);

			WebTestClient webTestClient = WebTestClient.bindToServer().apply(configurer).build();

			assertThat(exchangeStrategies(webTestClient).messageReaders())
					.flatExtracting(HttpMessageReader::getReadableMediaTypes) //
					.contains(HAL_JSON, HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	@Test // #1225
	void webTestClientConfigurerHandlesCustomHypermediaTypes() {

		withContext(CustomHypermediaConfig.class, context -> {

			HypermediaWebTestClientConfigurer configurer = context.getBean(HypermediaWebTestClientConfigurer.class);

			WebTestClient webTestClient = WebTestClient.bindToServer().apply(configurer).build();

			assertThat(exchangeStrategies(webTestClient).messageReaders())
					.flatExtracting(HttpMessageReader::getReadableMediaTypes) //
					.contains(HAL_JSON, FRODO_JSON) //
					.doesNotContain(HAL_FORMS_JSON, COLLECTION_JSON, UBER_JSON);
		});
	}

	// tag::web-test-client[]
	@Test // #1225
	void webTestClientShouldSupportHypermediaDeserialization() {

		// Configure an application context programmatically.
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.register(HalConfig.class); // <1>
		context.refresh();

		// Create an instance of a controller for testing
		WebFluxEmployeeController controller = context.getBean(WebFluxEmployeeController.class);
		controller.reset();

		// Extract the WebTestClientConfigurer from the app context.
		HypermediaWebTestClientConfigurer configurer = context.getBean(HypermediaWebTestClientConfigurer.class);

		// Create a WebTestClient by binding to the controller and applying the hypermedia configurer.
		WebTestClient client = WebTestClient.bindToApplicationContext(context).build().mutateWith(configurer); // <2>

		// Exercise the controller.
		client.get().uri("http://localhost/employees").accept(HAL_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectBody(new TypeReferences.CollectionModelType<EntityModel<Employee>>() {}) // <3>
				.consumeWith(result -> {
					CollectionModel<EntityModel<Employee>> model = result.getResponseBody(); // <4>

					// Assert against the hypermedia model.
					assertThat(model.getRequiredLink(IanaLinkRelations.SELF)).isEqualTo(Link.of("http://localhost/employees"));
					assertThat(model.getContent()).hasSize(2);
				});
	}
	// end::web-test-client[]

	/**
	 * Extract the {@link ExchangeStrategies} from a {@link WebTestClient} to assert it has the proper message readers and
	 * writers.
	 *
	 * @param webTestClient
	 * @return
	 */
	@SuppressWarnings("null")
	private static ExchangeStrategies exchangeStrategies(WebTestClient webTestClient) {

		Object exchangeFunction = ReflectionTestUtils.getField(webTestClient, "exchangeFunction");

		return (ExchangeStrategies) ReflectionTestUtils.getField(exchangeFunction, "strategies");
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig {

		@Bean
		WebFluxEmployeeController controller() {
			return new WebFluxEmployeeController();
		}
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
