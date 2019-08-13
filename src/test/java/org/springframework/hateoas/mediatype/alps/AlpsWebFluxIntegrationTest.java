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

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;

import reactor.test.StepVerifier;

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
				.returnResult(Alps.class).getResponseBody() //
				.as(StepVerifier::create) //
				.expectNextMatches(alps -> {

					assertThat(alps.getVersion()).isEqualTo("1.0");
					assertThat(alps.getDoc().getFormat()).isEqualByComparingTo(Format.TEXT);
					assertThat(alps.getDoc().getHref()).isEqualTo("https://example.org/samples/full/doc.html");
					assertThat(alps.getDoc().getValue()).isEqualTo("value goes here");
					assertThat(alps.getDescriptor()).hasSize(2);

					assertThat(alps.getDescriptor().get(0).getId()).isEqualTo("class field [name]");
					assertThat(alps.getDescriptor().get(0).getName()).isEqualTo("name");
					assertThat(alps.getDescriptor().get(0).getType()).isEqualByComparingTo(Type.SEMANTIC);
					assertThat(alps.getDescriptor().get(0).getDescriptor()).hasSize(1);
					assertThat(alps.getDescriptor().get(0).getDescriptor().get(0).getId()).isEqualTo("embedded");
					assertThat(alps.getDescriptor().get(0).getExt().getId()).isEqualTo("ext [name]");
					assertThat(alps.getDescriptor().get(0).getExt().getHref()).isEqualTo("https://example.org/samples/ext/name");
					assertThat(alps.getDescriptor().get(0).getExt().getValue()).isEqualTo("value goes here");
					assertThat(alps.getDescriptor().get(0).getRt()).isEqualTo("rt for [name]");

					assertThat(alps.getDescriptor().get(1).getId()).isEqualTo("class field [role]");
					assertThat(alps.getDescriptor().get(1).getName()).isEqualTo("role");
					assertThat(alps.getDescriptor().get(1).getType()).isEqualByComparingTo(Type.SEMANTIC);
					assertThat(alps.getDescriptor().get(1).getDescriptor()).hasSize(1);
					assertThat(alps.getDescriptor().get(1).getDescriptor().get(0).getId()).isEqualTo("embedded");
					assertThat(alps.getDescriptor().get(1).getExt().getId()).isEqualTo("ext [role]");
					assertThat(alps.getDescriptor().get(1).getExt().getHref()).isEqualTo("https://example.org/samples/ext/role");
					assertThat(alps.getDescriptor().get(1).getExt().getValue()).isEqualTo("value goes here");
					assertThat(alps.getDescriptor().get(1).getRt()).isEqualTo("rt for [role]");

					return true;
				}) //
				.verifyComplete();
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
