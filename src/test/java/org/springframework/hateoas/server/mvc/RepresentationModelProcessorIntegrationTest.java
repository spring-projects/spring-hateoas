/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.hamcrest.CoreMatchers.*;
import static org.springframework.hateoas.MediaTypes.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import lombok.Getter;
import lombok.Setter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.mediatype.hal.HalLinkDiscoverer;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.support.Employee;
import org.springframework.hateoas.support.WebMvcEmployeeController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
public class RepresentationModelProcessorIntegrationTest {

	private static final LinkRelation COLLECTION_LINK_RELATION = LinkRelation.of("collection-processor");
	private static final LinkRelation ENTITY_LINK_RELATION = LinkRelation.of("entity-processor");
	private static final LinkRelation WILDCARD_LINK_RELATION = LinkRelation.of("wildcard-processor");

	private static final LinkDiscoverer DISCOVERER = new HalLinkDiscoverer();

	private @Autowired WebApplicationContext context;

	private @Autowired CollectionModelProcessor collectionModelProcessor;
	private @Autowired EntityModelProcessor entityModelProcessor;
	private @Autowired NonSpecificDomainObjectProcessor wildcardProcessor;

	private MockMvc mockMvc;

	@BeforeEach
	public void setUp() {

		this.mockMvc = webAppContextSetup(this.context).build();

		WebMvcEmployeeController.reset();

		collectionModelProcessor.setTriggered(false);
		entityModelProcessor.setTriggered(false);
		wildcardProcessor.setTriggered(false);
	}

	@Test
	public void collectionModelProcessorShouldWork() throws Exception {

		String results = this.mockMvc.perform(get("/employees").accept(HAL_JSON)) //
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, HAL_JSON.toString())) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();

		assertThat(DISCOVERER.findRequiredLinkWithRel(COLLECTION_LINK_RELATION, results).getHref())
				.isEqualTo("/collection/link");

		assertThat(DISCOVERER.findLinkWithRel(ENTITY_LINK_RELATION, results)).isEmpty();

		assertThat(collectionModelProcessor.isTriggered()).isTrue();
		assertThat(entityModelProcessor.isTriggered()).isTrue();
		assertThat(wildcardProcessor.isTriggered()).isTrue();
	}

	@Test
	public void problemReturningControllerMethod() throws Exception {

		this.mockMvc.perform(get("/employees/problem").accept(HTTP_PROBLEM_DETAILS_JSON)) //
				.andExpect(content().contentType(HTTP_PROBLEM_DETAILS_JSON)) //
				.andExpect(status().is(HttpStatus.BAD_REQUEST.value())) //
				.andExpect(jsonPath("$.type", is("http://example.com/problem"))) //
				.andExpect(jsonPath("$.title", is("Employee-based problem"))) //
				.andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value()))) //
				.andExpect(jsonPath("$.detail", is("This is a test case")));

	}

	@Test
	public void entityModelProcessorShouldWork() throws Exception {

		String results = this.mockMvc.perform(get("/employees/1").accept(HAL_JSON)) //
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, HAL_JSON.toString())) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();

		assertThat(DISCOVERER.findRequiredLinkWithRel(ENTITY_LINK_RELATION, results).getHref()).isEqualTo("/entity/link");

		assertThat(collectionModelProcessor.isTriggered()).isFalse();
		assertThat(entityModelProcessor.isTriggered()).isTrue();
		assertThat(wildcardProcessor.isTriggered()).isFalse();
	}

	@Test
	public void wildcardProcessorShouldNotWork() throws Exception {

		String results = this.mockMvc.perform(get("/employees").accept(HAL_JSON)) //
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, HAL_JSON.toString())) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();

		assertThat(DISCOVERER.findRequiredLinkWithRel(WILDCARD_LINK_RELATION, results).getHref())
				.isEqualTo("/non-specific-collection/link");

		assertThat(collectionModelProcessor.isTriggered()).isTrue();
		assertThat(entityModelProcessor.isTriggered()).isTrue();
		assertThat(wildcardProcessor.isTriggered()).isTrue();
	}

	static class EntityModelProcessor implements RepresentationModelProcessor<EntityModel<Employee>> {

		private @Getter @Setter boolean triggered = false;

		@Override
		public EntityModel<Employee> process(EntityModel<Employee> model) {

			triggered = true;
			model.add(Link.of("/entity/link", ENTITY_LINK_RELATION));
			return model;
		}
	}

	static class CollectionModelProcessor
			implements RepresentationModelProcessor<CollectionModel<EntityModel<Employee>>> {

		private @Getter @Setter boolean triggered = false;

		@Override
		public CollectionModel<EntityModel<Employee>> process(CollectionModel<EntityModel<Employee>> model) {

			triggered = true;
			model.add(Link.of("/collection/link", COLLECTION_LINK_RELATION));
			return model;
		}
	}

	static class NonSpecificDomainObjectProcessor implements RepresentationModelProcessor<CollectionModel<?>> {

		private @Getter @Setter boolean triggered = false;

		@Override
		public CollectionModel<?> process(CollectionModel<?> model) {

			triggered = true;
			model.add(Link.of("/non-specific-collection/link", WILDCARD_LINK_RELATION));
			return model;
		}
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class TestConfig {

		@Bean
		EntityModelProcessor entityModelProcessor() {
			return new EntityModelProcessor();
		}

		@Bean
		CollectionModelProcessor collectionModelProcessor() {
			return new CollectionModelProcessor();
		}

		@Bean
		NonSpecificDomainObjectProcessor wildcardProcessor() {
			return new NonSpecificDomainObjectProcessor();
		}

		@Bean
		WebMvcEmployeeController employeeController() {
			return new WebMvcEmployeeController();
		}

		@Bean
		ObjectMapper testMapper() {

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			return objectMapper;
		}
	}
}
