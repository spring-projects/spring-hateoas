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

import static net.jadler.Jadler.verifyThatRequest;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.DefaultLinkDiscoverer;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.plugin.core.config.EnablePluginRegistries;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * Integration tests for default json format with {@link Traverson}.
 * 
 * @author Dietrich Schulten
 * @since 0.12
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader=AnnotationConfigContextLoader.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class TraversonDefaultJsonTest {

	
	@Autowired
	URI baseUri;
	
	@Autowired
	DefaultJsonServer server;
	
	@Autowired
	Traverson traverson;
	
	@Configuration
	@EnablePluginRegistries(LinkDiscoverer.class)
  static class ContextConfiguration {

      @Bean
      public Traverson traverson() {      		
          return new Traverson(uri(), MediaType.APPLICATION_JSON);
      }
      
      @Bean
      public DefaultLinkDiscoverer defaultLinkDiscoverer() {
      	return new DefaultLinkDiscoverer();
      }
      
      @Bean
      public DefaultJsonServer server() {
      	DefaultJsonServer server = new DefaultJsonServer();
      	return server;
      }
      
      
      @Bean
      public URI uri() {
      	return URI.create(server().rootResource());
      }
  }


	@Before
	public void setUp() {
		setUpActors();
	}

	@After
	public void tearDown() throws IOException {
		if (server != null) {
			server.close();
		}
	}

	@Test
	public void sendsConfiguredMediaTypesInAcceptHeader() {

		traverson.follow().toObject(String.class);

		verifyThatRequest(). //
				havingPathEqualTo("/"). //
				havingHeader("Accept", hasItem("application/json")).receivedOnce();
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
	public void readsTraversalIntoResourceInstance() {

		ParameterizedTypeReference<Resource<Actor>> typeReference = new ParameterizedTypeReference<Resource<Actor>>() {
		};
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
