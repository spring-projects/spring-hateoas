package org.springframework.hateoas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * Assembles {@link ExchangeStrategies} needed to wire a {@link WebClient} with hypermedia support.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 * @since 1.0
 * @deprecated Migrate to {@link HypermediaWebClientConfigurer} and it's {@link WebClient.Builder}-oriented
 *             approach.
 */
@Deprecated
public class WebClientConfigurer {

	private final HypermediaWebClientConfigurer hypermediaWebClientConfigurer;

	/**
	 * Creates a new {@link WebClientConfigurer} for the given {@link ObjectMapper} and
	 * {@link HypermediaMappingInformation}s.
	 *
	 * @param mapper must not be {@literal null}.
	 * @param hypermediaTypes must not be {@literal null}.
	 */
	public WebClientConfigurer(ObjectMapper mapper, List<HypermediaMappingInformation> hypermediaTypes) {
		this.hypermediaWebClientConfigurer = new HypermediaWebClientConfigurer(mapper, hypermediaTypes);
	}

	/**
	 * Return a set of {@link ExchangeStrategies} driven by registered {@link HypermediaType}s.
	 *
	 * @return a collection of {@link Encoder}s and {@link Decoder} assembled into a {@link ExchangeStrategies}.
	 */
	public ExchangeStrategies hypermediaExchangeStrategies() {

		return ExchangeStrategies.builder() //
				.codecs(this.hypermediaWebClientConfigurer.configurer) //
				.build();
	}

	/**
	 * Register the proper {@link ExchangeStrategies} for a given {@link WebClient}.
	 *
	 * @param webClient
	 * @return mutated webClient with hypermedia support.
	 */
	public WebClient registerHypermediaTypes(WebClient webClient) {
		return this.hypermediaWebClientConfigurer.registerHypermediaTypes(webClient.mutate()).build();
	}
}
