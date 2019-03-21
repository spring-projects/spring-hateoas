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
package org.springframework.hateoas.reactive;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;
import static org.springframework.hateoas.reactive.WebFluxLinkBuilder.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.reactive.WebClientConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Greg Turnquist
 */
public class HypermediaWebFilterTest {

	WebTestClient testClient;

	@Before
	public void setUp() {

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(WebFluxConfig.class);
		ctx.refresh();

		WebClientConfigurer webClientConfigurer = ctx.getBean(WebClientConfigurer.class);

		this.testClient = WebTestClient.bindToApplicationContext(ctx).build().mutate()
				.exchangeStrategies(webClientConfigurer.hypermediaExchangeStrategies()).build();
	}

	/**
	 * @see #728
	 */
	@Test
	public void webFilterShouldEmbedExchangeIntoContext() {

		this.testClient.get().uri("http://example.com/api") //
				.accept(MediaTypes.HAL_JSON) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(MediaTypes.HAL_JSON_UTF8) //
				.returnResult(ResourceSupport.class) //
				.getResponseBody() //
				.as(StepVerifier::create) //
				.expectNextMatches(resourceSupport -> {

					assertThat(resourceSupport.getLinks())//
							.containsExactly(new Link("http://example.com/api", IanaLinkRelations.SELF));

					return true;
				}).verifyComplete();
	}

	@RestController
	@RequestMapping("/api")
	static class TestController {

		@GetMapping
		Mono<ResourceSupport> root() {
			return linkTo(methodOn(TestController.class).root()).withSelfRel().toMono().map(ResourceSupport::new);
		}
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = HAL)
	static class WebFluxConfig {

		@Bean
		TestController controller() {
			return new TestController();
		}
	}
}
