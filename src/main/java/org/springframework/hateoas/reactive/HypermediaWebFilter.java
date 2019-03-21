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

import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

/**
 * {@link WebFilter} that ensures a copy of the {@link ServerWebExchange} is added to the Reactor {@link Context}.
 * 
 * @author Greg Turnquist
 * @since 1.0
 */
public class HypermediaWebFilter implements WebFilter {

	public static final String SERVER_WEB_EXCHANGE = "serverWebExchange";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

		return chain.filter(exchange)
			.subscriberContext(Context.of(SERVER_WEB_EXCHANGE, exchange));
	}
}
