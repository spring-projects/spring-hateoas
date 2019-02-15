/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.hateoas.reactive.HypermediaWebFilter.*;
import static org.springframework.hateoas.reactive.ReactiveLinkBuilder.linkTo;

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
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

/**
 * @author Greg Turnquist
 */
@RunWith(MockitoJUnitRunner.class)
public class ReactiveLinkBuilderTest {

	@Mock ServerWebExchange exchange;
	@Mock ServerHttpRequest request;

	@Test
	public void linkAtSameLevelAsExplicitServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		Link link = linkTo(methodOn(TestController.class).root(), this.exchange).withSelfRel();

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");
	}

	@Test
	public void linkAtSameLevelAsContextProvidedServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root()).map(ReactiveLinkBuilder::withSelfRel)
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)).as(StepVerifier::create)
				.expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");

					return true;
				}).verifyComplete();
	}

	@Test
	public void shallowLinkFromDeepExplicitServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api/employees"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		Link link = linkTo(methodOn(TestController.class).root(), this.exchange).withSelfRel();

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");
	}

	@Test
	public void shallowLinkFromDeepContextProvidedServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api/employees"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).root()).map(ReactiveLinkBuilder::withSelfRel)
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)).as(StepVerifier::create)
				.expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api");

					return true;
				}).verifyComplete();
	}

	@Test
	public void deepLinkFromShallowExplicitServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		Link link = linkTo(methodOn(TestController.class).deep(), this.exchange).withSelfRel();

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).isEqualTo("http://localhost:8080/api/employees");
	}

	@Test
	public void deepLinkFromShallowContextProvidedServerExchangeShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/api"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController.class).deep()).map(ReactiveLinkBuilder::withSelfRel)
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)).as(StepVerifier::create)
				.expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/api/employees");

					return true;
				}).verifyComplete();
	}

	@Test
	public void linkToRouteWithNoMappingShouldWork() throws URISyntaxException {

		when(this.exchange.getRequest()).thenReturn(this.request);

		when(this.request.getURI()).thenReturn(new URI("http://localhost:8080/"));
		when(this.request.getHeaders()).thenReturn(new HttpHeaders());

		linkTo(methodOn(TestController2.class).root()).map(ReactiveLinkBuilder::withSelfRel)
				.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, this.exchange)).as(StepVerifier::create)
				.expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("http://localhost:8080/");

					return true;
				}).verifyComplete();
	}

	@Test
	public void linkToRouteWithNoExchangeInTheContextShouldFallbackToRelativeUris() throws URISyntaxException {

		linkTo(methodOn(TestController2.class).root())//
				.map(ReactiveLinkBuilder::withSelfRel) //
				.as(StepVerifier::create) //
				.expectNextMatches(link -> {

					assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
					assertThat(link.getHref()).isEqualTo("/");

					return true;
				}).verifyComplete();
	}

	@Test
	public void linkToRouteWithExplictExchangeBeingNullShouldFallbackToRelativeUris() throws URISyntaxException {

		Link link = linkTo(methodOn(TestController2.class).root(), null).withSelfRel();

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).isEqualTo("/");
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
