/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.mediatype.alps;

import static org.hamcrest.Matchers.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.WebClientConfigurer;
import org.springframework.hateoas.support.WebFluxEmployeeController;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class AlpsWebFluxIntegrationTest {

	@Autowired WebTestClient testClient;

	@Test
	void profileEndpointReturnsAlps() {

		this.testClient.get().uri("/profile") //
				.accept(MediaTypes.ALPS_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectBody() //
				.jsonPath("$.version").isEqualTo("1.0") //
				.jsonPath("$.doc.format").isEqualTo("TEXT") //
				.jsonPath("$.doc.href").isEqualTo("https://example.org/samples/full/doc.html") //
				.jsonPath("$.doc.value").isEqualTo("value goes here") //
				.jsonPath("$.descriptor").value(hasSize(2)) //

				.jsonPath("$.descriptor[0].id").isEqualTo("class field [name]") //
				.jsonPath("$.descriptor[0].name").isEqualTo("name") //
				.jsonPath("$.descriptor[0].type").isEqualTo("SEMANTIC") //
				.jsonPath("$.descriptor[0].descriptor").value(hasSize(1)) //
				.jsonPath("$.descriptor[0].descriptor[0].id").isEqualTo("embedded") //
				.jsonPath("$.descriptor[0].ext.id").isEqualTo("ext [name]") //
				.jsonPath("$.descriptor[0].ext.href").isEqualTo("https://example.org/samples/ext/name") //
				.jsonPath("$.descriptor[0].ext.value").isEqualTo("value goes here") //
				.jsonPath("$.descriptor[0].rt").isEqualTo("rt for [name]") //

				.jsonPath("$.descriptor[1].id").isEqualTo("class field [role]") //
				.jsonPath("$.descriptor[1].name").isEqualTo("role") //
				.jsonPath("$.descriptor[1].type").isEqualTo("SEMANTIC") //
				.jsonPath("$.descriptor[1].descriptor").value(hasSize(1)) //
				.jsonPath("$.descriptor[1].descriptor[0].id").isEqualTo("embedded") //
				.jsonPath("$.descriptor[1].ext.id").isEqualTo("ext [role]") //
				.jsonPath("$.descriptor[1].ext.href").isEqualTo("https://example.org/samples/ext/role") //
				.jsonPath("$.descriptor[1].ext.value").isEqualTo("value goes here") //
				.jsonPath("$.descriptor[1].rt").isEqualTo("rt for [role]");
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = HAL)
	static class TestConfig {

		@Bean
		WebFluxEmployeeController employeeController() {
			return new WebFluxEmployeeController();
		}

		@Bean
		WebTestClient webTestClient(WebClientConfigurer webClientConfigurer, ApplicationContext ctx) {

			return WebTestClient.bindToApplicationContext(ctx).build() //
					.mutate() //
					.exchangeStrategies(webClientConfigurer.hypermediaExchangeStrategies()) //
					.build();
		}
	}

}
