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
package org.springframework.hateoas.config.reactive;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.support.ContextTester.*;

import reactor.test.StepVerifier;

import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.client.Actor;
import org.springframework.hateoas.client.Movie;
import org.springframework.hateoas.client.Server;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mvc.TypeReferences.ResourceType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Tests registration of proper decoders by the {@link HypermediaWebClientBeanPostProcessor}.
 * 
 * @author Greg Turnquist
 */
public class HypermediaWebClientBeanPostProcessorTest {

	private URI baseUri;
	private Server server;

	@Before
	public void setUp() {

		this.server = new Server();

		Resource<Actor> actor = new Resource<>(new Actor("Keanu Reaves"));
		String actorUri = this.server.mockResourceFor(actor);

		Movie movie = new Movie("The Matrix");
		Resource<Movie> resource = new Resource<>(movie);
		resource.add(new Link(actorUri, "actor"));

		this.server.mockResourceFor(resource);
		this.server.finishMocking();

		this.baseUri = URI.create(this.server.rootResource());
	}

	@After
	public void tearDown() {

		if (this.server != null) {
			this.server.close();
		}
	}

	/**
	 * @see #728
	 */
	@Test
	public void shouldHandleRootHalDocument() {

		withContext(HalConfig.class, context -> {

			WebClient webClient = context.getBean(WebClient.class);

			webClient //
					.get().uri(this.baseUri) //
					.accept(MediaTypes.HAL_JSON) //
					.retrieve() //
					.bodyToMono(ResourceSupport.class) //
					.as(StepVerifier::create) //
					.expectNextMatches(root -> { //
						assertThat(root.getLinks()).hasSize(2);
						return true;

					}).verifyComplete();
		});
	}

	/**
	 * @see #728
	 */
	@Test
	public void shouldHandleNavigatingToAResourceObject() {

		ParameterizedTypeReference<Resource<Actor>> typeReference = new ResourceType<Actor>() {};

		withContext(HalConfig.class, context -> {

			WebClient webClient = context.getBean(WebClient.class);

			webClient //
					.get().uri(this.baseUri) //
					.retrieve() //
					.bodyToMono(ResourceSupport.class) //
					.map(resourceSupport -> resourceSupport.getRequiredLink("actors")) //
					.flatMap(link -> webClient //
							.get().uri(link.expand().getHref()) //
							.retrieve() //
							.bodyToMono(ResourceSupport.class)) //
					.map(resourceSupport -> resourceSupport.getLinks().toList().get(0)) //
					.flatMap(link -> webClient //
							.get().uri(link.expand().getHref()) //
							.retrieve() //
							.bodyToMono(typeReference)) //
					.as(StepVerifier::create) //
					.expectNext(new Resource<>(new Actor("Keanu Reaves"))) //
					.verifyComplete();
		});
	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig {

		@Bean
		WebClient webClient() {
			return WebClient.create();
		}
	}
}
