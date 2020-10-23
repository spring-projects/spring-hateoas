/*
 * Copyright 2013-2020 the original author or authors.
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

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson.TraversalBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for {@link Traverson}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @author Michael Wirth
 * @since 0.11
 */
class TraversonTest {

	static URI baseUri;
	static Server server;

	Traverson traverson;

	@BeforeAll
	public static void setUpClass() {

		server = new Server();
		baseUri = URI.create(server.rootResource());

		setUpActors();
	}

	@BeforeEach
	void setUp() {
		this.traverson = new Traverson(baseUri, MediaTypes.HAL_JSON);

	}

	@AfterAll
	public static void tearDown() throws IOException {

		if (server != null) {
			server.close();
		}
	}

	/**
	 * @see #131
	 */
	@Test
	void rejectsNullBaseUri() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new Traverson(null, MediaTypes.HAL_JSON);
		});
	}

	/**
	 * @see #131
	 */
	@Test
	void rejectsEmptyMediaTypes() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new Traverson(baseUri);
		});
	}

	/**
	 * @see #131
	 */
	@Test
	void sendsConfiguredMediaTypesInAcceptHeader() {

		traverson.follow().toObject(String.class);

		verifyThatRequest() //
				.havingPathEqualTo("/") //
				.havingHeader("Accept", contains(MediaTypes.HAL_JSON_VALUE)); //
	}

	/**
	 * @see #131
	 */
	@Test
	void readsTraversalIntoJsonPathExpression() {
		assertThat(traverson.follow("movies", "movie", "actor").<String> toObject("$.name")).isEqualTo("Keanu Reaves");
	}

	/**
	 * @see #131
	 */
	@Test
	void readsJsonPathTraversalIntoJsonPathExpression() {

		assertThat(traverson.follow(//
				"$._links.movies.href", //
				"$._links.movie.href", //
				"$._links.actor.href").<String> toObject("$.name")) //
						.isEqualTo("Keanu Reaves");
	}

	/**
	 * @see #131
	 */
	@Test
	void readsTraversalIntoResourceInstance() {

		ParameterizedTypeReference<EntityModel<Actor>> typeReference = new ParameterizedTypeReference<EntityModel<Actor>>() {};
		EntityModel<Actor> result = traverson.follow("movies", "movie", "actor").toObject(typeReference);

		assertThat(result.getContent().name).isEqualTo("Keanu Reaves");
	}

	/**
	 * @see #187
	 */
	@Test
	void sendsConfiguredHeadersForJsonPathExpression() {

		String expectedHeader = "<https://www.example.com>;rel=\"home\"";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Link", expectedHeader);

		assertThat(traverson.follow("movies", "movie", "actor") //
				.withHeaders(headers).<String> toObject("$.name")).isEqualTo("Keanu Reaves");

		verifyThatRequest() //
				.havingPathEqualTo("/actors/d95dbf62-f900-4dfa-9de8-0fc71e02ffa4") //
				.havingHeader("Link", hasItem(expectedHeader));
	}

	/**
	 * @see #187
	 */
	@Test
	void sendsConfiguredHeadersForToEntity() {

		String expectedHeader = "<https://www.example.com>;rel=\"home\"";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Link", expectedHeader);

		traverson.follow("movies", "movie", "actor").//
				withHeaders(headers).toEntity(Actor.class);

		verifyThatRequest(). //
				havingPathEqualTo("/actors/d95dbf62-f900-4dfa-9de8-0fc71e02ffa4"). //
				havingHeader("Link", hasItem(expectedHeader));
	}

	/**
	 * @see #201, #203
	 */
	@Test
	void allowsCustomizingRestTemplate() {

		CountingInterceptor interceptor = new CountingInterceptor();

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Arrays.asList(interceptor));

		this.traverson = new Traverson(baseUri, MediaTypes.HAL_JSON);
		this.traverson.setRestOperations(restTemplate);

		traverson.follow("movies", "movie", "actor").<String> toObject("$.name");
		assertThat(interceptor.intercepted).isEqualTo(4);
	}

	/**
	 * @see #185
	 */
	@Test
	void usesCustomLinkDiscoverer() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/github"), MediaType.APPLICATION_JSON);
		this.traverson.setLinkDiscoverers(Arrays.asList(new GitHubLinkDiscoverer()));

		String value = this.traverson.follow("foo").toObject("$.key");
		assertThat(value).isEqualTo("value");
	}

	/**
	 * @see #212
	 */
	@Test
	void shouldReturnLastLinkFound() {

		Link result = traverson.follow("movies").asLink();

		assertThat(result.getHref()).endsWith("/movies");
		assertThat(result.hasRel("movies")).isTrue();
	}

	/**
	 * @see #307
	 */
	@Test
	void returnsTemplatedLinkIfRequested() {

		TraversalBuilder follow = new Traverson(URI.create(server.rootResource().concat("/link")), MediaTypes.HAL_JSON)
				.follow("self");

		Link link = follow.asTemplatedLink();

		assertThat(link.isTemplated()).isTrue();
		assertThat(link.getVariableNames()).contains("template");

		link = follow.asLink();

		assertThat(link.isTemplated()).isFalse();
	}

	@Test // #971
	void returnsTemplatedRequiredLinkIfRequested() {

		Link templatedLink = new Traverson(URI.create(server.rootResource() + "/github-with-template"), MediaTypes.HAL_JSON) //
				.follow("rel_to_templated_link") //
				.asTemplatedLink();

		assertThat(templatedLink.isTemplated()).isTrue();
		assertThat(templatedLink.getVariableNames()).contains("issue");

		Link expandedLink = templatedLink.expand("42");

		assertThat(expandedLink.isTemplated()).isFalse();
		assertThat(expandedLink.getHref()).isEqualTo("/github/42");
	}

	/**
	 * @see #258
	 */
	@Test
	void returnsDefaultMessageConvertersForHal() {

		List<HttpMessageConverter<?>> converters = Traverson.getDefaultMessageConverters(MediaTypes.HAL_JSON);

		assertThat(converters).hasSize(2);
		assertThat(converters.get(0)).isInstanceOf(StringHttpMessageConverter.class);
		assertThat(converters.get(1)).isInstanceOf(MappingJackson2HttpMessageConverter.class);

		converters = Traverson.getDefaultMessageConverters(MediaTypes.HAL_JSON);

		assertThat(converters).hasSize(2);
		assertThat(converters.get(0)).isInstanceOf(StringHttpMessageConverter.class);
		assertThat(converters.get(1)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
	}

	/**
	 * @see #258
	 */
	@Test
	void returnsDefaultMessageConverters() {

		List<HttpMessageConverter<?>> converters = Traverson.getDefaultMessageConverters();

		assertThat(converters).hasSize(1);
		assertThat(converters.get(0)).isInstanceOf(StringHttpMessageConverter.class);
	}

	/**
	 * @see #346
	 */
	@Test
	void chainMultipleFollowOperations() {

		ParameterizedTypeReference<EntityModel<Actor>> typeReference = new ParameterizedTypeReference<EntityModel<Actor>>() {};
		EntityModel<Actor> result = traverson.follow("movies").follow("movie").follow("actor").toObject(typeReference);

		assertThat(result.getContent().name).isEqualTo("Keanu Reaves");
	}

	/**
	 * @see #346
	 */
	@Test
	void allowAlteringTheDetailsOfASingleHop() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		// tag::hop-with-param[]
		ParameterizedTypeReference<EntityModel<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<EntityModel<Item>>() {};

		EntityModel<Item> itemResource = traverson.//
				follow(rel("items").withParameter("projection", "noImages")).//
				follow("$._embedded.items[0]._links.self.href").//
				toObject(resourceParameterizedTypeReference);
		// end::hop-with-param[]

		assertThat(itemResource.hasLink("self")).isTrue();
		assertThat(itemResource.getRequiredLink("self").expand().getHref())
				.isEqualTo(server.rootResource() + "/springagram/items/1");

		final Item item = itemResource.getContent();
		assertThat(item.image).isEqualTo(server.rootResource() + "/springagram/file/cat");
		assertThat(item.description).isEqualTo("cat");
	}

	/**
	 * @see #346
	 */
	@Test
	void allowAlteringTheDetailsOfASingleHopByMapOperations() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		// tag::hop-put[]
		ParameterizedTypeReference<EntityModel<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<EntityModel<Item>>() {};

		Map<String, Object> params = Collections.singletonMap("projection", "noImages");

		EntityModel<Item> itemResource = traverson.//
				follow(rel("items").withParameters(params)).//
				follow("$._embedded.items[0]._links.self.href").//
				toObject(resourceParameterizedTypeReference);
		// end::hop-put[]

		assertThat(itemResource.hasLink("self")).isTrue();
		assertThat(itemResource.getRequiredLink("self").expand().getHref())
				.isEqualTo(server.rootResource() + "/springagram/items/1");

		final Item item = itemResource.getContent();
		assertThat(item.image).isEqualTo(server.rootResource() + "/springagram/file/cat");
		assertThat(item.description).isEqualTo("cat");
	}

	/**
	 * @see #346
	 */
	@Test
	void allowGlobalsToImpactSingleHops() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		Map<String, Object> params = new HashMap<>();
		params.put("projection", "thisShouldGetOverwrittenByLocalHop");

		ParameterizedTypeReference<EntityModel<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<EntityModel<Item>>() {};
		EntityModel<Item> itemResource = traverson.follow(rel("items").withParameter("projection", "noImages"))
				.follow("$._embedded.items[0]._links.self.href") // retrieve first Item in the collection
				.withTemplateParameters(params).toObject(resourceParameterizedTypeReference);

		assertThat(itemResource.hasLink("self")).isTrue();
		assertThat(itemResource.getRequiredLink("self").expand().getHref())
				.isEqualTo(server.rootResource() + "/springagram/items/1");

		final Item item = itemResource.getContent();
		assertThat(item.image).isEqualTo(server.rootResource() + "/springagram/file/cat");
		assertThat(item.description).isEqualTo("cat");
	}

	/**
	 * @see #337
	 */
	@Test
	void doesNotDoubleEncodeURI() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		EntityModel<?> itemResource = traverson.//
				follow(rel("items").withParameters(Collections.singletonMap("projection", "no images"))).//
				toObject(EntityModel.class);

		assertThat(itemResource.hasLink("self")).isTrue();
		assertThat(itemResource.getRequiredLink("self").expand().getHref())
				.isEqualTo(server.rootResource() + "/springagram/items");
	}

	@Test
	void customHeaders() {

		String customHeaderName = "X-CustomHeader";

		traverson
				.follow(rel("movies").header(customHeaderName, "alpha").header(HttpHeaders.LOCATION,
						"http://localhost:8080/my/custom/location"))
				.follow(rel("movie").header(customHeaderName, "bravo")).follow(rel("actor").header(customHeaderName, "charlie"))
				.toObject("$.name");

		verifyThatRequest() //
				.havingPathEqualTo("/") //
				.havingHeader(HttpHeaders.ACCEPT, contains(MediaTypes.HAL_JSON_VALUE)); //

		verifyThatRequest().havingPathEqualTo("/movies") // aggregate root movies
				.havingHeader(HttpHeaders.ACCEPT, contains(MediaTypes.HAL_JSON_VALUE)) //
				.havingHeader(customHeaderName, contains("alpha")) //
				.havingHeader(HttpHeaders.LOCATION, contains("http://localhost:8080/my/custom/location")); //

		verifyThatRequest().havingPath(startsWith("/movies/")) // single movie
				.havingHeader(HttpHeaders.ACCEPT, contains(MediaTypes.HAL_JSON_VALUE)) //
				.havingHeader(customHeaderName, contains("bravo")); //

		verifyThatRequest().havingPath(startsWith("/actors/")) // single actor
				.havingHeader(HttpHeaders.ACCEPT, contains(MediaTypes.HAL_JSON_VALUE)) //
				.havingHeader(customHeaderName, contains("charlie")); //
	}

	private static void setUpActors() {

		EntityModel<Actor> actor = EntityModel.of(new Actor("Keanu Reaves"));
		String actorUri = server.mockResourceFor(actor);

		Movie movie = new Movie("The Matrix");
		EntityModel<Movie> resource = EntityModel.of(movie);
		resource.add(Link.of(actorUri, "actor"));

		server.mockResourceFor(resource);
		server.finishMocking();
	}

	static class CountingInterceptor implements ClientHttpRequestInterceptor {

		int intercepted;

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
				throws IOException {
			this.intercepted++;
			return execution.execute(request, body);
		}
	}

	static class GitHubLinkDiscoverer extends JsonPathLinkDiscoverer {

		public GitHubLinkDiscoverer() {
			super("$.%s_url", MediaType.APPLICATION_JSON);
		}
	}
}
