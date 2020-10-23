package org.springframework.hateoas.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.HypermediaRestTemplateConfigurer;
import org.springframework.hateoas.config.HypermediaWebClientConfigurer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HypermediaConfiguration {

    // tag::rest-template[]
	/**
	 * Use the {@link HypermediaRestTemplateConfigurer} to configure a {@link RestTemplate}.
	 */
	@Bean
	RestTemplate hypermediaRestTemplate(HypermediaRestTemplateConfigurer configurer) { // <1>
		return configurer.registerHypermediaTypes(new RestTemplate()); // <2>
	}
    // end::rest-template[]

    // tag::web-client[]
    @Bean
    WebClient.Builder hypermediaWebClient(HypermediaWebClientConfigurer configurer) { // <1>
	    return configurer.registerHypermediaTypes(WebClient.builder()); // <2>
    }
    // end::web-client[]
}
