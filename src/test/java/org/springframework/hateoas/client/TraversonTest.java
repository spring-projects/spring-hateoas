/*
 * Copyright 2013-2017 the original author or authors.
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson.TraversalBuilder;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for {@link Traverson}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 * @since 0.11
 */
public class TraversonTest {

	URI baseUri;
	Server server;
	Traverson traverson;

	@Before
	public void setUp() {

		this.server = new Server();
		this.baseUri = URI.create(server.rootResource());
		this.traverson = new Traverson(baseUri, MediaTypes.HAL_JSON_UTF8, MediaTypes.HAL_JSON);

		setUpActors();
	}

	@After
	public void tearDown() throws IOException {
		if (server != null) {
			server.close();
		}
	}

	/**
	 * @see #131
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullBaseUri() {
		new Traverson(null, MediaTypes.HAL_JSON);
	}

	/**
	 * @see #131
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsEmptyMediaTypes() {
		new Traverson(baseUri, new MediaType[0]);
	}

	/**
	 * @see #131
	 */
	@Test
	public void sendsConfiguredMediaTypesInAcceptHeader() {

		traverson.follow().toObject(String.class);

		verifyThatRequest() //
				.havingPathEqualTo("/") //
				.havingHeader("Accept", contains(MediaTypes.HAL_JSON_UTF8_VALUE + ", " + MediaTypes.HAL_JSON_VALUE)) //
				.receivedOnce();
	}

	/**
	 * @see #131
	 */
	@Test
	public void readsTraversalIntoJsonPathExpression() {
		assertThat(traverson.follow("movies", "movie", "actor").<String> toObject("$.name")).isEqualTo("Keanu Reaves");
	}

	/**
	 * @see #131
	 */
	@Test
	public void readsJsonPathTraversalIntoJsonPathExpression() {
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
	public void readsTraversalIntoResourceInstance() {

		ParameterizedTypeReference<Resource<Actor>> typeReference = new ParameterizedTypeReference<Resource<Actor>>() {};
		Resource<Actor> result = traverson.follow("movies", "movie", "actor").toObject(typeReference);

		assertThat(result.getContent().name).isEqualTo("Keanu Reaves");
	}

	/**
	 * @see #187
	 */
	@Test
	public void sendsConfiguredHeadersForJsonPathExpression() {

		String expectedHeader = "<http://www.example.com>;rel=\"home\"";

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
	public void sendsConfiguredHeadersForToEntity() {

		String expectedHeader = "<http://www.example.com>;rel=\"home\"";

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
	public void allowsCustomizingRestTemplate() {

		CountingInterceptor interceptor = new CountingInterceptor();

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Arrays.<ClientHttpRequestInterceptor> asList(interceptor));

		this.traverson = new Traverson(baseUri, MediaTypes.HAL_JSON);
		this.traverson.setRestOperations(restTemplate);

		traverson.follow("movies", "movie", "actor").<String> toObject("$.name");
		assertThat(interceptor.intercepted).isEqualTo(4);
	}

	/**
	 * @see #185
	 */
	@Test
	public void usesCustomLinkDiscoverer() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/github"), MediaType.APPLICATION_JSON);
		this.traverson.setLinkDiscoverers(Arrays.asList(new GitHubLinkDiscoverer()));

		String value = this.traverson.follow("foo").toObject("$.key");
		assertThat(value).isEqualTo("value");
	}

	/**
	 * @see #212
	 */
	@Test
	public void shouldReturnLastLinkFound() {

		Link result = traverson.follow("movies").asLink();

		assertThat(result.getHref()).endsWith("/movies");
		assertThat(result.getRel()).isEqualTo("movies");
	}

	/**
	 * @see #307
	 */
	@Test
	public void returnsTemplatedLinkIfRequested() {

		TraversalBuilder follow = new Traverson(URI.create(server.rootResource().concat("/link")), MediaTypes.HAL_JSON)
				.follow("self");

		Link link = follow.asTemplatedLink();

		assertThat(link.isTemplated()).isTrue();
		assertThat(link.getVariableNames()).contains("template");

		link = follow.asLink();

		assertThat(link.isTemplated()).isFalse();
	}

	/**
	 * @see #258
	 */
	@Test
	public void returnsDefaultMessageConvertersForHal() {

		List<HttpMessageConverter<?>> converters = Traverson.getDefaultMessageConverters(MediaTypes.HAL_JSON);

		assertThat(converters).hasSize(2);
		assertThat(converters.get(0)).isInstanceOf(StringHttpMessageConverter.class);
		assertThat(converters.get(1)).isInstanceOf(MappingJackson2HttpMessageConverter.class);

		converters = Traverson.getDefaultMessageConverters(MediaTypes.HAL_JSON_UTF8);

		assertThat(converters).hasSize(2);
		assertThat(converters.get(0)).isInstanceOf(StringHttpMessageConverter.class);
		assertThat(converters.get(1)).isInstanceOf(MappingJackson2HttpMessageConverter.class);
	}

	/**
	 * @see #258
	 */
	@Test
	public void returnsDefaultMessageConverters() {

		List<HttpMessageConverter<?>> converters = Traverson
				.getDefaultMessageConverters(Collections.<MediaType> emptyList());

		assertThat(converters).hasSize(1);
		assertThat(converters.get(0)).isInstanceOf(StringHttpMessageConverter.class);
	}

	/**
	 * @see #346
	 */
	@Test
	public void chainMultipleFollowOperations() {

		ParameterizedTypeReference<Resource<Actor>> typeReference = new ParameterizedTypeReference<Resource<Actor>>() {};
		Resource<Actor> result = traverson.follow("movies").follow("movie").follow("actor").toObject(typeReference);

		assertThat(result.getContent().name).isEqualTo("Keanu Reaves");
	}

	/**
	 * @see #346
	 */
	@Test
	public void allowAlteringTheDetailsOfASingleHop() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		// tag::hop-with-param[]
		ParameterizedTypeReference<Resource<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Item>>() {};

		Resource<Item> itemResource = traverson.//
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
	public void allowAlteringTheDetailsOfASingleHopByMapOperations() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		// tag::hop-put[]
		ParameterizedTypeReference<Resource<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Item>>() {};

		Map<String, Object> params = Collections.singletonMap("projection", "noImages");

		Resource<Item> itemResource = traverson.//
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
	public void allowGlobalsToImpactSingleHops() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("projection", "thisShouldGetOverwrittenByLocalHop");

		ParameterizedTypeReference<Resource<Item>> resourceParameterizedTypeReference = new ParameterizedTypeReference<Resource<Item>>() {};
		Resource<Item> itemResource = traverson.follow(rel("items").withParameter("projection", "noImages"))
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
	public void doesNotDoubleEncodeURI() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/springagram"), MediaTypes.HAL_JSON);

		Resource<?> itemResource = traverson.//
				follow(rel("items").withParameters(Collections.singletonMap("projection", "no images"))).//
				toObject(Resource.class);

		assertThat(itemResource.hasLink("self")).isTrue();
		assertThat(itemResource.getRequiredLink("self").expand().getHref())
				.isEqualTo(server.rootResource() + "/springagram/items");
	}

	private void setUpActors() {

		Resource<Actor> actor = new Resource<Actor>(new Actor("Keanu Reaves"));
		String actorUri = server.mockResourceFor(actor);

		Movie movie = new Movie("The Matrix");
		Resource<Movie> resource = new Resource<Movie>(movie);
		resource.add(new Link(actorUri, "actor"));

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
	};

	static class GitHubLinkDiscoverer extends JsonPathLinkDiscoverer {

		public GitHubLinkDiscoverer() {
			super("$.%s_url", MediaType.APPLICATION_JSON);
		}
	}
}
