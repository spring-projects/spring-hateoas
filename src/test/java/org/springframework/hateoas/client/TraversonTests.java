/*
 * Copyright 2013-2014 the original author or authors.
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
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.core.JsonPathLinkDiscoverer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for {@link Traverson}.
 * 
 * @author Oliver Gierke
 * @since 0.11
 */
public class TraversonTests {

	URI baseUri;
	Server server;
	Traverson traverson;

	@Before
	public void setUp() {

		this.server = new Server();
		this.baseUri = URI.create(server.rootResource());
		this.traverson = new Traverson(baseUri, MediaTypes.HAL_JSON);

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

		verifyThatRequest(). //
				havingPathEqualTo("/"). //
				havingHeader("Accept", hasItem("application/hal+json"));
	}

	/**
	 * @see #131
	 */
	@Test
	public void readsTraversalIntoJsonPathExpression() {
		assertThat(traverson.follow("movies", "movie", "actor").<String> toObject("$.name"), is("Keanu Reaves"));
	}

	/**
	 * @see #131
	 */
	@Test
	public void readsJsonPathTraversalIntoJsonPathExpression() {
		assertThat(traverson.follow(//
				"$._links.movies.href", //
				"$._links.movie.href", //
				"$._links.actor.href").<String> toObject("$.name"), is("Keanu Reaves"));
	}

	/**
	 * @see #131
	 */
	@Test
	public void readsTraversalIntoResourceInstance() {

		ParameterizedTypeReference<Resource<Actor>> typeReference = new ParameterizedTypeReference<Resource<Actor>>() {};
		Resource<Actor> result = traverson.follow("movies", "movie", "actor").toObject(typeReference);

		assertThat(result.getContent().name, is("Keanu Reaves"));
	}

	/**
	 * @see #187
	 */
	@Test
	public void sendsConfiguredHeadersForJsonPathExpression() {

		String expectedHeader = "<http://www.example.com>;rel=\"home\"";

		HttpHeaders headers = new HttpHeaders();
		headers.add("Link", expectedHeader);

		assertThat(traverson.follow("movies", "movie", "actor").//
				withHeaders(headers).<String> toObject("$.name"), is("Keanu Reaves"));

		verifyThatRequest(). //
				havingPathEqualTo("/actors/d95dbf62-f900-4dfa-9de8-0fc71e02ffa4"). //
				havingHeader("Link", hasItem(expectedHeader));
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
		assertThat(interceptor.intercepted, is(4));
	}

	/**
	 * @see #185
	 */
	@Test
	public void usesCustomLinkDiscoverer() {

		this.traverson = new Traverson(URI.create(server.rootResource() + "/github"), MediaType.APPLICATION_JSON);
		this.traverson.setLinkDiscoverers(Arrays.asList(new GitHubLinkDiscoverer()));

		String value = this.traverson.follow("foo").toObject("$.key");
		assertThat(value, is("value"));
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
