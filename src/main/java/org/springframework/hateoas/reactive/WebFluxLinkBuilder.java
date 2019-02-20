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

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.core.TemplateVariableAwareLinkBuilderSupport;
import org.springframework.hateoas.core.WebHandler;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Utility for building reactive {@link Link}s.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.0
 */
public class WebFluxLinkBuilder extends TemplateVariableAwareLinkBuilderSupport<WebFluxLinkBuilder> {

	private WebFluxLinkBuilder(UriComponentsBuilder builder, TemplateVariables variables, List<Affordance> affordances) {
		super(builder, variables, affordances);
	}

	private WebFluxLinkBuilder(UriComponents components, TemplateVariables variables, List<Affordance> affordances) {
		super(components, variables, affordances);
	}

	/**
	 * Create a {@link WebFluxLinkBuilder} by checking if the Reactor Context contains a {@link ServerWebExchange} and
	 * using that combined with the Spring Web annotations to build a full URI. If there is no exchange, then fall back to
	 * relative URIs. Usually used with {@link #methodOn(Class, Object...)} to refer to a method invocation.
	 *
	 * @param invocation must not be {@literal null}.
	 * @see #methodOn(Class, Object...)
	 */
	public static WebFluxBuilder linkTo(Object invocation) {

		Assert.notNull(invocation, "Invocation must not be null!");

		return new WebFluxBuilder(linkToInternal(invocation));
	}

	/**
	 * Create a {@link WebFluxLinkBuilder} using an explicitly defined {@link ServerWebExchange}. This is possible if your
	 * WebFlux method includes the exchange and you want to pass it straight in.
	 *
	 * @param invocation must not be {@literal null}.
	 * @param exchange must not be {@literal null}.
	 */
	public static WebFluxBuilder linkTo(Object invocation, ServerWebExchange exchange) {
		return new WebFluxBuilder(linkToInternal(invocation, exchange));
	}

	/**
	 * Wrapper for {@link DummyInvocationUtils#methodOn(Class, Object...)} to be available in case you work with static
	 * imports of {@link WebFluxLinkBuilder}.
	 *
	 * @param controller must not be {@literal null}.
	 * @param parameters parameters to extend template variables in the type level mapping.
	 * @return
	 */
	public static <T> T methodOn(Class<T> controller, Object... parameters) {
		return DummyInvocationUtils.methodOn(controller, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.TemplateVariableAwareLinkBuilderSupport#createNewInstance(org.springframework.web.util.UriComponentsBuilder, java.util.List, org.springframework.hateoas.TemplateVariables)
	 */
	@Override
	protected WebFluxLinkBuilder createNewInstance(UriComponentsBuilder builder, List<Affordance> affordances,
			TemplateVariables variables) {
		return new WebFluxLinkBuilder(builder, variables, affordances);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.LinkBuilderSupport#getThis()
	 */
	@Override
	protected WebFluxLinkBuilder getThis() {
		return this;
	}

	@RequiredArgsConstructor
	public static class WebFluxBuilder {

		private final Mono<WebFluxLinkBuilder> builder;

		/**
		 * Creates a new {@link WebFluxLink} for the {@link Link} with the given {@link LinkRelation}
		 *
		 * @param relation must not be {@literal null}.
		 * @return
		 */
		public WebFluxLink withRel(LinkRelation relation) {
			return new WebFluxLink(builder.map(it -> it.withRel(relation)));
		}

		/**
		 * Creates a new {@link WebFluxLink} for the {@link Link} with the given link relation.
		 *
		 * @param relation must not be {@literal null}.
		 * @return
		 */
		public WebFluxLink withRel(String relation) {
			return new WebFluxLink(builder.map(it -> it.withRel(relation)));
		}

		/**
		 * Creates a new {@link WebFluxLink} for the {@link Link} with the {@link IanaLinkRelations#SELF}.
		 *
		 * @return
		 */
		public WebFluxLink withSelfRel() {
			return new WebFluxLink(builder.map(WebFluxLinkBuilder::withSelfRel));
		}

		/**
		 * General callback to produce a {@link Link} from the given {@link WebFluxLinkBuilder}.
		 *
		 * @param finisher must not be {@literal null}.
		 * @return
		 */
		public WebFluxLink toLink(Function<WebFluxLinkBuilder, Mono<Link>> finisher) {

			Assert.notNull(finisher, "Finisher must not be null!");

			return new WebFluxLink(builder.flatMap(finisher));
		}
	}

	/**
	 * Intermediate representation of a {@link Link} within a reactive pipeline to easily add {@link Affordance}s from
	 * method invocations.
	 *
	 * @author Oliver Gierke
	 */
	@RequiredArgsConstructor
	public static class WebFluxLink {

		private final Mono<Link> link;

		/**
		 * Adds the affordance created by the given virtual method invocation.
		 *
		 * @param invocation must not be {@literal null}.
		 * @return
		 * @see WebFluxLinkBuilder#methodOn(Class, Object...)
		 */
		public WebFluxLink andAffordance(Object invocation) {

			Assert.notNull(invocation, "Invocation must not be null!");

			return new WebFluxLink(link.flatMap(it -> linkToInternal(invocation) //
					.flatMapIterable(WebFluxLinkBuilder::getAffordances) //
					.singleOrEmpty() //
					.map(it::andAffordance)));
		}

		/**
		 * Creates a new {@link WebFluxLink} with the current {@link Link} instance transformed using the given mapper.
		 *
		 * @param mapper must not be {@literal null}.
		 * @return
		 */
		public WebFluxLink map(Function<Link, Link> mapper) {

			Assert.notNull(mapper, "Function must not be null!");

			return new WebFluxLink(link.map(mapper));
		}

		/**
		 * Returns the underlying {@link Mono} of {@link Link} for further handling within a reactive pipeline.
		 *
		 * @return
		 */
		public Mono<Link> toMono() {
			return link;
		}

		/**
		 * Returns a {@link Mono} of {@link Link} with the current one augmented by the given {@link Function}. Allows
		 * immediate customization of the {@link Link} instance and immediately return to a general reactive API.
		 *
		 * @param finisher must not be {@literal null}.
		 * @return
		 */
		public Mono<Link> toMono(Function<Link, Link> finisher) {

			Assert.notNull(finisher, "Function must not be null!");

			return link.map(finisher);
		}
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

	private static Mono<WebFluxLinkBuilder> linkToInternal(Object invocation) {

		return Mono.subscriberContext() //
				.flatMap(context -> linkToInternal(invocation, context.getOrDefault(SERVER_WEB_EXCHANGE, null)));
	}

	private static Mono<WebFluxLinkBuilder> linkToInternal(Object invocation, ServerWebExchange exchange) {

		return Mono.just(WebHandler.linkTo(invocation, //
				path -> getBuilder(exchange).replacePath(path == null ? "/" : path), //
				WebFluxLinkBuilder::new));
	}
}
