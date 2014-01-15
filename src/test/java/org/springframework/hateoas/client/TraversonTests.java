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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;

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

	private void setUpActors() {

		Resource<Actor> actor = new Resource<Actor>(new Actor("Keanu Reaves"));
		String actorUri = server.mockResourceFor(actor);

		Movie movie = new Movie("The Matrix");
		Resource<Movie> resource = new Resource<Movie>(movie);
		resource.add(new Link(actorUri, "actor"));

		server.mockResourceFor(resource);
		server.finishMocking();
	}
}
