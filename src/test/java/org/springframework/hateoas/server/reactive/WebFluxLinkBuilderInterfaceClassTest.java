/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.hateoas.server.reactive;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.WebClientConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class WebFluxLinkBuilderInterfaceClassTest {

	@Autowired WebTestClient testClient;

	@Test
	void parentInterfaceCanHoldSpringWebAnnotations() throws Exception {

		this.testClient.get().uri("http://example.com/api?view=short") //
				.accept(MediaTypes.HAL_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.HAL_JSON) //
				.returnResult(RepresentationModel.class) //
				.getResponseBody() //
				.as(StepVerifier::create) //
				.expectNextMatches(resourceSupport -> {

					assertThat(resourceSupport.getLinks())//
							.containsExactly(Link.of("http://example.com/api?view=short", IanaLinkRelations.SELF));

					return true;
				}) //
				.verifyComplete();
	}

	interface WebFluxInterface {

		@GetMapping("/api")
		Mono<RepresentationModel<?>> root(@RequestParam String view);
	}

	@RestController
	static class WebFluxClass implements WebFluxInterface {

		@Override
		public Mono<RepresentationModel<?>> root(String view) {

			Mono<Link> selfLink = linkTo(methodOn(WebFluxClass.class).root(view)).withSelfRel().toMono();

			return selfLink.map(RepresentationModel::new);
		}
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = { HypermediaType.HAL })
	static class TestConfig {

		@Bean
		WebFluxClass concreteController() {
			return new WebFluxClass();
		}

		@Bean
		WebTestClient webTestClient(WebClientConfigurer webClientConfigurer, ApplicationContext ctx) {

			return WebTestClient.bindToApplicationContext(ctx).build().mutate()
					.exchangeStrategies(webClientConfigurer.hypermediaExchangeStrategies()).build();
		}
	}

}
