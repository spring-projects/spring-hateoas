/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.MockRestServiceServer.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mvc.TypeReferences.ResourceType;
import org.springframework.hateoas.mvc.TypeReferences.ResourcesType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for {@link TypeReferences}.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class TypeReferencesIntegrationTest {

	private static final String USER = "\"firstname\" : \"Dave\", \"lastname\" : \"Matthews\"";
	private static final String RESOURCE = String.format("{ \"_links\" : { \"self\" : \"/resource\" }, %s }", USER);
	private static final String RESOURCES_OF_USER = String
			.format("{ \"_links\" : { \"self\" : \"/resources\" }, \"_embedded\" : { \"users\" : [ { %s } ] }}", USER);
	private static final String RESOURCES_OF_RESOURCE = String
			.format("{ \"_links\" : { \"self\" : \"/resources\" }, \"_embedded\" : { \"users\" : [ %s ] }}", RESOURCE);

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class Config {

		public @Bean RestTemplate template() {
			return new RestTemplate();
		}
	}

	@Autowired RestTemplate template;
	MockRestServiceServer server;

	@Before
	public void setUp() {
		this.server = createServer(template);
	}

	/**
	 * @see #306
	 */
	@Test
	public void usesResourceTypeReference() {

		server.expect(requestTo("/resource")).andRespond(withSuccess(RESOURCE, MediaTypes.HAL_JSON));

		ResponseEntity<Resource<User>> response = template.exchange("/resource", HttpMethod.GET, null,
				new ResourceType<User>() {});

		assertExpectedUserResource(response.getBody());
	}

	/**
	 * @see #306
	 */
	@Test
	public void usesResourcesTypeReference() {

		server.expect(requestTo("/resources")).andRespond(withSuccess(RESOURCES_OF_USER, MediaTypes.HAL_JSON));

		ResponseEntity<Resources<User>> response = template.exchange("/resources", HttpMethod.GET, null,
				new ResourcesType<User>() {});
		Resources<User> body = response.getBody();

		assertThat(body.hasLink("self")).isTrue();

		Collection<User> nested = body.getContent();

		assertThat(nested).hasSize(1);
		assertExpectedUser(nested.iterator().next());
	}

	/**
	 * @see #306
	 */
	@Test
	public void usesResourcesOfResourceTypeReference() {

		server.expect(requestTo("/resources")).andRespond(withSuccess(RESOURCES_OF_RESOURCE, MediaTypes.HAL_JSON));

		ResponseEntity<Resources<Resource<User>>> response = template.exchange("/resources", HttpMethod.GET, null,
				new ResourcesType<Resource<User>>() {});
		Resources<Resource<User>> body = response.getBody();

		assertThat(body.hasLink("self")).isTrue();

		Collection<Resource<User>> nested = body.getContent();

		assertThat(nested).hasSize(1);
		assertExpectedUserResource(nested.iterator().next());
	}

	private static void assertExpectedUserResource(Resource<User> user) {

		assertThat(user.hasLink("self")).isTrue();
		assertExpectedUser(user.getContent());
	}

	private static void assertExpectedUser(User user) {

		assertThat(user.firstname).isEqualTo("Dave");
		assertThat(user.lastname).isEqualTo("Matthews");
	}

	static class User {
		public String firstname, lastname;
	}
}
