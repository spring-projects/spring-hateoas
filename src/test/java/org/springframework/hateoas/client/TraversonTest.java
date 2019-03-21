/*
 * Copyright 2013-2019 the original author or authors.
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
package org.springframework.hateoas.client;

import static net.jadler.Jadler.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.hateoas.client.Hop.*;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson.TraversalBuilder;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for {@link Traverson}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.11
 */
public class TraversonTest {

	static URI baseUri;
	static Server server;

	@BeforeClass
	public static void setUpClass() {
		setUp(MediaTypes.HAL_JSON);
	}

	private static void setUp(MediaType mediaType) {

		server = new Server(mediaType);
		baseUri = URI.create(server.rootResource());

		setUpActors();
	}

	@AfterClass
	public static void tearDown() {

		if (server != null) {
			server.close();
		}
	}

	@Test // #131
	public void sendsConfiguredMediaTypesInAcceptHeader() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource().concat("/link"))) //
					.accept(MediaTypes.HAL_JSON);

			traverson.follow().as(String.class);

			verifyThatRequest() //
					.havingPathEqualTo("/") //
					.havingHeader("Accept", contains(MediaTypes.HAL_JSON_UTF8_VALUE + ", " + MediaTypes.HAL_JSON_VALUE)); //
		});
	}

	@Test // #131
	public void readsTraversalIntoJsonPathExpression() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			assertThat(traverson.follow( //
					"movies", //
					"movie", //
					"actor").<String> as("$.name")) //
							.isEqualTo("Keanu Reaves");
		});
	}

	@Test // #131
	public void readsJsonPathTraversalIntoJsonPathExpression() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			assertThat(traverson.follow(//
					"$._links.movies.href", //
					"$._links.movie.href", //
					"$._links.actor.href").<String> as("$.name")) //
							.isEqualTo("Keanu Reaves");
		});
	}

	@Test // #131
	public void readsTraversalIntoResourceInstance() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			ParameterizedTypeReference<Resource<Actor>> typeReference = new ParameterizedTypeReference<Resource<Actor>>() {};
			Resource<Actor> result = traverson.follow("movies", "movie", "actor").as(typeReference);

			assertThat(result.getContent().name).isEqualTo("Keanu Reaves");
		});
	}

	@Test // #187
	public void sendsConfiguredHeadersForJsonPathExpression() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			String expectedHeader = "<http://www.example.com>;rel=\"home\"";

			HttpHeaders headers = new HttpHeaders();
			headers.add("Link", expectedHeader);

			assertThat(traverson.follow("movies", "movie", "actor") //
					.withHeaders(headers).<String> as("$.name")).isEqualTo("Keanu Reaves");

			verifyThatRequest() //
					.havingPathEqualTo("/actors/d95dbf62-f900-4dfa-9de8-0fc71e02ffa4") //
					.havingHeader("Link", hasItem(expectedHeader));
		});
	}

	@Test // #187
	public void sendsConfiguredHeadersForToEntity() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			String expectedHeader = "<http://www.example.com>;rel=\"home\"";

			HttpHeaders headers = new HttpHeaders();
			headers.add("Link", expectedHeader);

			traverson.follow("movies", "movie", "actor") //
					.withHeaders(headers) //
					.toEntity(Actor.class);

			verifyThatRequest() //
					.havingPathEqualTo("/actors/d95dbf62-f900-4dfa-9de8-0fc71e02ffa4") //
					.havingHeader("Link", hasItem(expectedHeader));
		});
	}

	@Test // #212
	public void shouldReturnLastLinkFound() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			Link result = traverson.follow("movies").asLink();

			assertThat(result.getHref()).endsWith("/movies");
			assertThat(result.hasRel("movies")).isTrue();
		});
	}

	@Test // #307
	public void returnsTemplatedLinkIfRequested() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource().concat("/link"))) //
					.accept(MediaTypes.HAL_JSON);

			TraversalBuilder follow = traverson.follow("self");

			Link link = follow.asTemplatedLink();

			assertThat(link.isTemplated()).isTrue();
			assertThat(link.getVariableNames()).contains("template");

			link = follow.asLink();

			assertThat(link.isTemplated()).isFalse();
		});
	}

	@Test // #346
	public void chainMultipleFollowOperations() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			ParameterizedTypeReference<Resource<Actor>> typeReference = new ParameterizedTypeReference<Resource<Actor>>() {};
			Resource<Actor> result = traverson //
					.follow("movies") //
					.follow("movie") //
					.follow("actor") //
					.as(typeReference);

			assertThat(result.getContent().name).isEqualTo("Keanu Reaves");
		});
	}

	@Test // #346
	public void allowAlteringTheDetailsOfASingleHop() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource() + "/springagram")) //
					.accept(MediaTypes.HAL_JSON);

			// tag::hop-with-param[]
			ParameterizedTypeReference<Resource<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Item>>() {};

			Resource<Item> itemResource = traverson //
					.follow(rel("items").withParameter("projection", "noImages")) //
					.follow("$._embedded.items[0]._links.self.href") //
					.as(resourceParameterizedTypeReference);
			// end::hop-with-param[]

			assertThat(itemResource.hasLink("self")).isTrue();
			assertThat(itemResource.getRequiredLink("self").expand().getHref()) //
					.isEqualTo(server.rootResource() + "/springagram/items/1");

			final Item item = itemResource.getContent();
			assertThat(item.image).isEqualTo(server.rootResource() + "/springagram/file/cat");
			assertThat(item.description).isEqualTo("cat");
		});
	}

	@Test // #346
	public void allowAlteringTheDetailsOfASingleHopByMapOperations() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource() + "/springagram")) //
					.accept(MediaTypes.HAL_JSON);

			// tag::hop-put[]
			ParameterizedTypeReference<Resource<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Item>>() {};

			Map<String, Object> params = Collections.singletonMap("projection", "noImages");

			Resource<Item> itemResource = traverson //
					.follow(rel("items").withParameters(params)) //
					.follow("$._embedded.items[0]._links.self.href") //
					.as(resourceParameterizedTypeReference);
			// end::hop-put[]

			assertThat(itemResource.hasLink("self")).isTrue();
			assertThat(itemResource.getRequiredLink("self").expand().getHref()) //
					.isEqualTo(server.rootResource() + "/springagram/items/1");

			final Item item = itemResource.getContent();
			assertThat(item.image).isEqualTo(server.rootResource() + "/springagram/file/cat");
			assertThat(item.description).isEqualTo("cat");
		});
	}

	@Test // #346
	public void allowGlobalsToImpactSingleHops() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource() + "/springagram")) //
					.accept(MediaTypes.HAL_JSON);

			Map<String, Object> params = new HashMap<>();
			params.put("projection", "thisShouldGetOverwrittenByLocalHop");

			ParameterizedTypeReference<Resource<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Item>>() {};
			Resource<Item> itemResource = traverson //
					.follow(rel("items").withParameter("projection", "noImages")) //
					.follow("$._embedded.items[0]._links.self.href") // retrieve first Item in the collection
					.withTemplateParameters(params) //
					.as(resourceParameterizedTypeReference);

			assertThat(itemResource.hasLink("self")).isTrue();
			assertThat(itemResource.getRequiredLink("self").expand().getHref()) //
					.isEqualTo(server.rootResource() + "/springagram/items/1");

			final Item item = itemResource.getContent();
			assertThat(item.image).isEqualTo(server.rootResource() + "/springagram/file/cat");
			assertThat(item.description).isEqualTo("cat");
		});
	}

	@Test // #337
	public void doesNotDoubleEncodeURI() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource() + "/springagram")) //
					.accept(MediaTypes.HAL_JSON);

			Resource<?> itemResource = traverson
					.follow(rel("items").withParameters(Collections.singletonMap("projection", "no images"))) //
					.as(Resource.class);

			assertThat(itemResource.hasLink("self")).isTrue();
			assertThat(itemResource.getRequiredLink("self").expand().getHref()) //
					.isEqualTo(server.rootResource() + "/springagram/items");
		});
	}

	@Test
	public void customHeaders() {

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_JSON);

			String customHeaderName = "X-CustomHeader";

			traverson //
					.follow( //
							rel("movies") //
									.header(customHeaderName, "alpha") //
									.header(HttpHeaders.LOCATION, "http://localhost:8080/my/custom/location")) //
					.follow( //
							rel("movie") //
									.header(customHeaderName, "bravo")) //
					.follow( //
							rel("actor") //
									.header(customHeaderName, "charlie")) //
					.as("$.name");

			verifyThatRequest() //
					.havingPathEqualTo("/") //
					.havingHeader(HttpHeaders.ACCEPT,
							contains(MediaTypes.HAL_JSON_UTF8_VALUE + ", " + MediaTypes.HAL_JSON_VALUE)); //

			verifyThatRequest() //
					.havingPathEqualTo("/movies") // aggregate root movies
					.havingHeader(HttpHeaders.ACCEPT, contains(MediaTypes.HAL_JSON_UTF8_VALUE + ", " + MediaTypes.HAL_JSON_VALUE)) //
					.havingHeader(customHeaderName, contains("alpha")) //
					.havingHeader(HttpHeaders.LOCATION, contains("http://localhost:8080/my/custom/location")); //

			verifyThatRequest() //
					.havingPath(startsWith("/movies/")) // single movie
					.havingHeader(HttpHeaders.ACCEPT, contains(MediaTypes.HAL_JSON_UTF8_VALUE + ", " + MediaTypes.HAL_JSON_VALUE)) //
					.havingHeader(customHeaderName, contains("bravo")); //

			verifyThatRequest() //
					.havingPath(startsWith("/actors/")) // single actor
					.havingHeader(HttpHeaders.ACCEPT, contains(MediaTypes.HAL_JSON_UTF8_VALUE + ", " + MediaTypes.HAL_JSON_VALUE)) //
					.havingHeader(customHeaderName, contains("charlie")); //
		});
	}

	@Test
	public void handlesHalForms() {

		// Reconfigure Jadler for HAL-FORMS (it's a bit fussy with static initialization)
		server.close();
		setUp(MediaTypes.HAL_FORMS_JSON);

		withContext(TestConfig.class, context -> {

			Traverson traverson = context.getBean(Traverson.class) //
					.uri(URI.create(server.rootResource())) //
					.accept(MediaTypes.HAL_FORMS_JSON);

			assertThat(traverson.follow( //
					"movies", //
					"movie", //
					"actor").<String> as("$.name")) //
							.isEqualTo("Keanu Reaves");

		});

		// Reconfigure Jadler for HAL
		server.close();
		setUp(MediaTypes.HAL_JSON);
	}

	private static void setUpActors() {

		Resource<Actor> actor = new Resource<>(new Actor("Keanu Reaves"));
		String actorUri = server.mockResourceFor(actor);

		Movie movie = new Movie("The Matrix");
		Resource<Movie> resource = new Resource<>(movie);
		resource.add(new Link(actorUri, "actor"));

		server.mockResourceFor(resource);
		server.finishMocking();
	}

	@Configuration
	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, UBER, COLLECTION_JSON })
	static class TestConfig {

		@Bean
		RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}
}
