package org.springframework.hateoas.config.reactive;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.core.codec.StringDecoder;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Assembles {@link ExchangeStrategies} needed to wire a {@link WebClient} with hypermedia support.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfigurer {

	private final ObjectMapper mapper;
	private final Collection<HypermediaMappingInformation> hypermediaTypes;

	/**
	 * Return a set of {@link ExchangeStrategies} driven by registered {@link HypermediaType}s.
	 *
	 * @return a collection of {@link Encoder}s and {@link Decoder} assembled into a {@link ExchangeStrategies}.
	 */
	public ExchangeStrategies hypermediaExchangeStrategies() {

		List<Encoder<?>> encoders = new ArrayList<>();
		List<Decoder<?>> decoders = new ArrayList<>();

		this.hypermediaTypes.forEach(hypermedia -> {

			ObjectMapper objectMapper = hypermedia.configureObjectMapper(this.mapper.copy());
			MimeType[] mimeTypes = hypermedia.getMediaTypes().toArray(new MimeType[0]);

			encoders.add(new Jackson2JsonEncoder(objectMapper, mimeTypes));
			decoders.add(new Jackson2JsonDecoder(objectMapper, mimeTypes));
		});

		encoders.add(CharSequenceEncoder.allMimeTypes());
		decoders.add(StringDecoder.allMimeTypes());

		return ExchangeStrategies.builder().codecs(clientCodecConfigurer -> {

			encoders.forEach(encoder -> clientCodecConfigurer.customCodecs().encoder(encoder));
			decoders.forEach(decoder -> clientCodecConfigurer.customCodecs().decoder(decoder));

			clientCodecConfigurer.registerDefaults(false);
		}).build();
	}

	/**
	 * Register the proper {@link ExchangeStrategies} for a given {@link WebClient}.
	 *
	 * @param webClient
	 * @return mutated webClient with hypermedia support.
	 */
	public WebClient registerHypermediaTypes(WebClient webClient) {
		return webClient.mutate().exchangeStrategies(hypermediaExchangeStrategies()).build();
	}
}
