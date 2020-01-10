/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.WebFluxLink;
import org.springframework.lang.Nullable;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
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

	/**
	 * @see #728
	 */
	@Test
	void linkAtSameLevelAsExplicitServerExchangeShouldWork() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api").build();
		WebFluxLink link = linkTo(methodOn(TestController.class).root()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("http://localhost:8080/api");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	void linkAtSameLevelAsContextProvidedServerExchangeShouldWork() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api").build();
		WebFluxLink link = linkTo(methodOn(TestController.class).root()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("http://localhost:8080/api");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	void shallowLinkFromDeepExplicitServerExchangeShouldWork() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/employees").build();
		WebFluxLink link = linkTo(methodOn(TestController.class).root()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("http://localhost:8080/api");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	void shallowLinkFromDeepContextProvidedServerExchangeShouldWork() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api/employees").build();

		WebFluxLink link = linkTo(methodOn(TestController.class).root()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("http://localhost:8080/api");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	void deepLinkFromShallowExplicitServerExchangeShouldWork() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api").build();
		WebFluxLink link = linkTo(methodOn(TestController.class).deep()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("http://localhost:8080/api/employees");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	void deepLinkFromShallowContextProvidedServerExchangeShouldWork() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api").build();
		WebFluxLink link = linkTo(methodOn(TestController.class).deep()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("http://localhost:8080/api/employees");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	void linkToRouteWithNoMappingShouldWork() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080").build();
		WebFluxLink link = linkTo(methodOn(TestController2.class).root()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("http://localhost:8080");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	void linkToRouteWithNoExchangeInTheContextShouldFallbackToRelativeUris() {

		WebFluxLink link = linkTo(methodOn(TestController2.class).root()).withSelfRel();

		verify(null, link, result -> {
			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("/");
		});
	}

	/**
	 * @see #728
	 */
	@Test
	@SuppressWarnings("null")
	void linkToRouteWithExplictExchangeBeingNullShouldFallbackToRelativeUris() {

		WebFluxLink link = linkTo(methodOn(TestController2.class).root(), null).withSelfRel();

		verify(null, link, result -> {

			assertThat(result.getRel()).isEqualTo(IanaLinkRelations.SELF);
			assertThat(result.getHref()).isEqualTo("/");
		});
	}

	@Test // #1150
	void considersContextPath() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/context/api") //
				.contextPath("/context") //
				.build();

		WebFluxLink link = linkTo(methodOn(TestController.class).deep()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getHref()).endsWith("/context/api/employees");
		});
	}

	@Test // #1152
	void allowsAppendingPathSegments() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api") //
				.build();

		WebFluxLink link = linkTo(methodOn(TestController.class).deep()).slash("foo").withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getHref()).endsWith("/api/employees/foo");
		});
	}

	@Test // #1152
	void removesIncomingQueryParameters() {

		MockServerHttpRequest request = MockServerHttpRequest.get("http://localhost:8080/api?some=parameter") //
				.build();

		WebFluxLink link = linkTo(methodOn(TestController.class).deep()).withSelfRel();

		verify(request, link, result -> {
			assertThat(result.getHref()).endsWith("/api/employees");
		});
	}

	private void verify(@Nullable MockServerHttpRequest request, WebFluxLink link, Consumer<Link> verifications) {

		Mono<Link> mono = link.toMono();

		if (request != null) {

			when(this.exchange.getRequest()).thenReturn(request);
			mono = mono.subscriberContext(Context.of(EXCHANGE_CONTEXT_ATTRIBUTE, this.exchange));
		}
		mono.as(StepVerifier::create).expectNextMatches(signal -> {

			verifications.accept(signal);
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
