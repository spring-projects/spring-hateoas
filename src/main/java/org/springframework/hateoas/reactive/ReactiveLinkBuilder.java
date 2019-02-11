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

import java.util.List;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.TemplateVariableAwareLinkBuilderSupport;
import org.springframework.hateoas.core.WebHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility for building reactive {@link Link}s.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
public class ReactiveLinkBuilder extends TemplateVariableAwareLinkBuilderSupport<ReactiveLinkBuilder> {

	private ReactiveLinkBuilder(UriComponentsBuilder builder, TemplateVariables variables, List<Affordance> affordances) {
		super(builder, variables, affordances);
	}

	private ReactiveLinkBuilder(UriComponents components, TemplateVariables variables, List<Affordance> affordances) {
		super(components, variables, affordances);
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

		return WebHandler.linkTo(invocationValue, //
				path -> getBuilder(exchange).replacePath(path == null ? "/" : path), //
				ReactiveLinkBuilder::new);
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.TemplateVariableAwareLinkBuilderSupport#createNewInstance(org.springframework.web.util.UriComponentsBuilder, java.util.List, org.springframework.hateoas.TemplateVariables)
	 */
	@Override
	protected ReactiveLinkBuilder createNewInstance(UriComponentsBuilder builder, List<Affordance> affordances,
			TemplateVariables variables) {
		return new ReactiveLinkBuilder(builder, variables, affordances);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.LinkBuilderSupport#getThis()
	 */
	@Override
	protected ReactiveLinkBuilder getThis() {
		return this;
	}
}
