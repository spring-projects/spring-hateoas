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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.reactive.HypermediaWebFilter.*;
import static org.springframework.hateoas.reactive.WebFluxLinkBuilder.linkTo;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
@RunWith(MockitoJUnitRunner.class)
public class WebFluxLinkBuilderTest {

	@Mock ServerWebExchange exchange;
	@Mock ServerHttpRequest request;

	/**
	 * @see #728
	 */
	@Test
	public void linkAtSameLevelAsExplicitServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
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
	public void linkAtSameLevelAsContextProvidedServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root()).withSelfRel().toMono() //
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)) //
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
	public void shallowLinkFromDeepExplicitServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api/employees"));
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
	public void shallowLinkFromDeepContextProvidedServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api/employees"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root()).withSelfRel().toMono() //
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)) //
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
	public void deepLinkFromShallowExplicitServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
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
	public void deepLinkFromShallowContextProvidedServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).deep()).withSelfRel().toMono() //
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)) //
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
	public void linkToRouteWithNoMappingShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);
		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController2.class).root()).withSelfRel().toMono() //
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)) //
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
	public void linkToRouteWithNoExchangeInTheContextShouldFallbackToRelativeUris() throws URISyntaxException {

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
	public void linkToRouteWithExplictExchangeBeingNullShouldFallbackToRelativeUris() throws URISyntaxException {

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
