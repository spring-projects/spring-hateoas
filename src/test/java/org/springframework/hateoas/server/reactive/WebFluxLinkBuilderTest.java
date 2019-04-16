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
package org.springframework.hateoas.server.reactive;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;
import static org.springframework.web.filter.reactive.ServerWebExchangeContextFilter.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * Unit tests for {@link WebFluxLinkBuilder}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@ExtendWith(MockitoExtension.class)
class WebFluxLinkBuilderTest {

	@Mock ServerWebExchange exchange;
	@Mock ServerHttpRequest request;

	/**
	 * @see #728
	 */
	@Test
	void linkAtSameLevelAsExplicitServerExchangeShouldWork() {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(URI.create("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root(), this.exchange).withSelfRel().toMono() //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");

					return true;

				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void linkAtSameLevelAsContextProvidedServerExchangeShouldWork() {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(URI.create("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root()).withSelfRel().toMono() //
				.subscriberContext(Context.of(EXCHANGE_CONTEXT_ATTRIBUTE, this.exchange)) //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");

					return true;
				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void shallowLinkFromDeepExplicitServerExchangeShouldWork() {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(URI.create("http://localhost:8080/api/employees"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root(), this.exchange).withSelfRel().toMono() //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");

					return true;

				}).verifyComplete();

	}

	/**
	 * @see #728
	 */
	@Test
	void shallowLinkFromDeepContextProvidedServerExchangeShouldWork() {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(URI.create("http://localhost:8080/api/employees"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root()).withSelfRel().toMono() //
				.subscriberContext(Context.of(EXCHANGE_CONTEXT_ATTRIBUTE, this.exchange)) //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");

					return true;

				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void deepLinkFromShallowExplicitServerExchangeShouldWork() {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(URI.create("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).deep(), this.exchange).withSelfRel().toMono() //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api/employees");

					return true;

				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void deepLinkFromShallowContextProvidedServerExchangeShouldWork() {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(URI.create("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).deep()).withSelfRel().toMono() //
				.subscriberContext(Context.of(EXCHANGE_CONTEXT_ATTRIBUTE, this.exchange)) //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api/employees");

					return true;

				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void linkToRouteWithNoMappingShouldWork() {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(URI.create("http://localhost:8080/"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController2.class).root()).withSelfRel().toMono() //
				.subscriberContext(Context.of(EXCHANGE_CONTEXT_ATTRIBUTE, this.exchange)) //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/");

					return true;

				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void linkToRouteWithNoExchangeInTheContextShouldFallbackToRelativeUris() {

		linkTo(methodOn(TestController2.class).root()).withSelfRel().toMono() //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("/");

					return true;

				}).verifyComplete();
	}

	/**
	 * @see #728
	 */
	@Test
	void linkToRouteWithExplictExchangeBeingNullShouldFallbackToRelativeUris() {

		linkTo(methodOn(TestController2.class).root(), null).withSelfRel().toMono() //
				.as(StepVerifier::create).expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("/");

					return true;

				}).verifyComplete();

	}

	@RestController
	@RequestMapping("/api")
	static class TestController {

		@GetMapping
		Mono<Object> root() {
			return Mono.empty();
		}

		@GetMapping("/employees")
		Mono<Object> deep() {
			return Mono.empty();
		}
	}

	@RestController
	static class TestController2 {

		@GetMapping
		Mono<Object> root() {
			return Mono.empty();
		}
	}
}
