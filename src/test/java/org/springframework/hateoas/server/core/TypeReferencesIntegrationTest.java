/*
 * Copyright 2015-2020 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.MockRestServiceServer.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import lombok.Data;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.server.core.TypeReferences.CollectionModelType;
import org.springframework.hateoas.server.core.TypeReferences.EntityModelType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

/**
 * Integration tests for {@link TypeReferences}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class TypeReferencesIntegrationTest {

	private static final String HAL_USER = "\"firstname\" : \"Dave\", \"lastname\" : \"Matthews\"";

	private static final String COLLECTION_JSON_USER = "{ \"name\" : \"firstname\", \"value\" : \"Dave\" }, { \"name\" : \"lastname\", \"value\" : \"Matthews\" }";

	private static final String RESOURCE_HAL = String.format("{ \"_links\" : { \"self\" : \"/resource\" }, %s }",
			HAL_USER);
	private static final String RESOURCES_OF_USER_HAL = String
			.format("{ \"_links\" : { \"self\" : \"/resources\" }, \"_embedded\" : { \"users\" : [ { %s } ] }}", HAL_USER);
	private static final String RESOURCES_OF_RESOURCE_HAL = String
			.format("{ \"_links\" : { \"self\" : \"/resources\" }, \"_embedded\" : { \"users\" : [ %s ] }}", RESOURCE_HAL);

	private static final String RESOURCE_COLLECTION_JSON = String.format(
			"{ \"collection\": { \"version\": \"1.0\", \"href\": \"localhost\", \"links\": [{ \"rel\": \"self\", \"href\": \"localhost\" }], \"items\": [{\"href\": \"localhost\", \"data\": [%s]}]}}",
			COLLECTION_JSON_USER);
	private static final String RESOURCES_OF_USER_COLLECTION_JSON = String.format(
			"{ \"collection\": { \"version\": \"1.0\", \"href\": \"localhost\", \"links\": [{ \"rel\": \"self\", \"href\": \"localhost\" }], \"items\": [{\"href\": \"localhost\", \"data\": [%s]}]}}",
			COLLECTION_JSON_USER);
	private static final String RESOURCES_OF_RESOURCE_COLLECTION_JSON = String.format(
			"{ \"collection\": { \"version\": \"1.0\", \"href\": \"localhost\", \"links\": [{ \"rel\": \"self\", \"href\": \"localhost\" }], \"items\": [{\"href\": \"localhost\", \"data\": [%s], \"links\": [{\"rel\":\"self\", \"href\": \"localhost\"}]}]}}",
			COLLECTION_JSON_USER);

	@Configuration
	@EnableHypermediaSupport(type = { HypermediaType.HAL, HypermediaType.COLLECTION_JSON })
	static class Config {

		public @Bean RestTemplate template() {
			return new RestTemplate();
		}
	}

	@Autowired RestTemplate template;
	MockRestServiceServer server;

	@BeforeEach
	void setUp() {
		this.server = createServer(template);
	}

	/**
	 * @see #306
	 */
	@Test
	void usesResourceTypeReferenceWithHal() {

		server.expect(requestTo("/resource")).andRespond(withSuccess(RESOURCE_HAL, MediaTypes.HAL_JSON));

		ResponseEntity<EntityModel<User>> response = template.exchange("/resource", HttpMethod.GET, null,
				new EntityModelType<User>() {});

		assertExpectedUserResource(response.getBody());
	}

	/**
	 * @see #482
	 */
	@Test
	void usesResourceTypeReferenceWithCollectionJson() {

		server.expect(requestTo("/resource")).andRespond(withSuccess(RESOURCE_COLLECTION_JSON, MediaTypes.COLLECTION_JSON));

		ResponseEntity<EntityModel<User>> response = template.exchange("/resource", HttpMethod.GET, null,
				new EntityModelType<User>() {});

		assertExpectedUserResource(response.getBody());
	}

	/**
	 * @see #306
	 */
	@Test
	void usesResourcesTypeReferenceWithHal() {

		server.expect(requestTo("/resources")).andRespond(withSuccess(RESOURCES_OF_USER_HAL, MediaTypes.HAL_JSON));

		ResponseEntity<CollectionModel<User>> response = template.exchange("/resources", HttpMethod.GET, null,
				new CollectionModelType<User>() {});
		CollectionModel<User> body = response.getBody();

		assertThat(body.hasLink("self")).isTrue();

		Collection<User> nested = body.getContent();

		assertThat(nested).hasSize(1);
		assertExpectedUser(nested.iterator().next());
	}

	/**
	 * @see #482
	 */
	@Test
	void usesResourcesTypeReferenceWithCollectionJson() {

		server.expect(requestTo("/resources"))
				.andRespond(withSuccess(RESOURCES_OF_USER_COLLECTION_JSON, MediaTypes.COLLECTION_JSON));

		ResponseEntity<CollectionModel<User>> response = template.exchange("/resources", HttpMethod.GET, null,
				new CollectionModelType<User>() {});
		CollectionModel<User> body = response.getBody();

		assertThat(body.hasLink("self")).isTrue();

		Collection<User> nested = body.getContent();

		assertThat(nested).hasSize(1);
		assertExpectedUser(nested.iterator().next());
	}

	/**
	 * @see #306
	 */
	@Test
	void usesResourcesOfResourceTypeReferenceWithHal() {

		server.expect(requestTo("/resources")).andRespond(withSuccess(RESOURCES_OF_RESOURCE_HAL, MediaTypes.HAL_JSON));

		ResponseEntity<CollectionModel<EntityModel<User>>> response = template.exchange("/resources", HttpMethod.GET, null,
				new CollectionModelType<EntityModel<User>>() {});
		CollectionModel<EntityModel<User>> body = response.getBody();

		assertThat(body.hasLink("self")).isTrue();

		Collection<EntityModel<User>> nested = body.getContent();

		assertThat(nested).hasSize(1);
		assertExpectedUserResource(nested.iterator().next());
	}

	/**
	 * @see #482
	 */
	@Test
	void usesResourcesOfResourceTypeReferenceWithCollectionJson() {

		server.expect(requestTo("/resources"))
				.andRespond(withSuccess(RESOURCES_OF_RESOURCE_COLLECTION_JSON, MediaTypes.COLLECTION_JSON));

		ResponseEntity<CollectionModel<EntityModel<User>>> response = template.exchange("/resources", HttpMethod.GET, null,
				new CollectionModelType<EntityModel<User>>() {});
		CollectionModel<EntityModel<User>> body = response.getBody();

		assertThat(body.hasLink("self")).isTrue();

		Collection<EntityModel<User>> nested = body.getContent();

		assertThat(nested).hasSize(1);
		assertExpectedUserResource(nested.iterator().next());
	}

	private static void assertExpectedUserResource(EntityModel<User> user) {

		assertThat(user.hasLink("self")).isTrue();
		assertExpectedUser(user.getContent());
	}

	private static void assertExpectedUser(User user) {

		assertThat(user.firstname).isEqualTo("Dave");
		assertThat(user.lastname).isEqualTo("Matthews");
	}

	@Data
	static class User {
		public String firstname, lastname;
	}
}
