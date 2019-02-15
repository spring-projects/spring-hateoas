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

import static org.springframework.hateoas.reactive.HypermediaWebFilter.*;

import reactor.core.publisher.Mono;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.WebHandler;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility for building reactive {@link Link}s.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public class ReactiveLinkBuilder {

	private final ControllerLinkBuilder controllerLinkBuilder;

	private ReactiveLinkBuilder(ControllerLinkBuilder controllerLinkBuilder) {
		this.controllerLinkBuilder = controllerLinkBuilder;
	}

	/**
	 * Create a {@link ReactiveLinkBuilder} by checking if the Reactor Context contains a {@link ServerWebExchange} and
	 * using that combined with the Spring Web annotations to build a full URI. If there is no exchange, then fall back to
	 * relative URIs.
	 *
	 * @param invocationValue
	 */
	public static Mono<ReactiveLinkBuilder> linkTo(Object invocationValue) {

		return Mono.subscriberContext()
				.map(context -> linkTo(invocationValue, context.getOrDefault(SERVER_WEB_EXCHANGE, null)));
	}

	/**
	 * Create a {@link ReactiveLinkBuilder} using an explicitly defined {@link ServerWebExchange}. This is possible if
	 * your WebFlux method includes the exchange and you want to pass it straight in.
	 *
	 * @param invocationValue
	 * @param exchange
	 */
	public static ReactiveLinkBuilder linkTo(Object invocationValue, ServerWebExchange exchange) {

		ControllerLinkBuilder controllerLinkBuilder = WebHandler //
				.linkTo(invocationValue, path -> getBuilder(exchange).replacePath(path == null ? "/" : path), null);

		return new ReactiveLinkBuilder(controllerLinkBuilder);
	}

	/**
	 * Utility method to transform {@link ReactiveLinkBuilder} into a {@link Link}.
	 */
	public Link withSelfRel() {
		return this.controllerLinkBuilder.withSelfRel();
	}

	/**
	 * Utility method to transform {@link ReactiveLinkBuilder} into a {@link Link}.
	 *
	 * @param rel
	 */
	public Link withRel(String rel) {
		return this.controllerLinkBuilder.withRel(rel);
	}

	/**
	 * Returns a {@link UriComponentsBuilder} obtained from the {@link ServerWebExchange}.
	 *
	 * @param exchange
	 */
	private static UriComponentsBuilder getBuilder(ServerWebExchange exchange) {

		return exchange == null //
				? UriComponentsBuilder.fromPath("/") //
				: UriComponentsBuilder.fromHttpRequest(exchange.getRequest());
	}
}
