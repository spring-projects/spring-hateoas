package org.springframework.hateoas.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.HypermediaRestTemplateConfigurer;
import org.springframework.web.client.RestTemplate;

// tag::code[]
@Configuration
public class HypermediaConfiguration {

	/**
	 * Use the {@link HypermediaRestTemplateConfigurer} to configure a {@link RestTemplate}.
	 */
	@Bean
	RestTemplate hypermediaRestTemplate(HypermediaRestTemplateConfigurer configurer) { // <1>
		return configurer.registerHypermediaTypes(new RestTemplate()); // <2>
	}
}
// end::code[]
