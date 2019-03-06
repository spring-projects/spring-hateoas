/*
 * Copyright 2019 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.AssertionsForInterfaceTypes.*;
import static org.springframework.hateoas.MediaTypes.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.hateoas.support.MappingUtils.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.ModelBuilder;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.EvoInflectorLinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class EmbeddedIntegrationTest {

	private @Autowired WebApplicationContext context;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = webAppContextSetup(this.context).build();
	}

	@Test
	public void embeddedSpecUsingAPIs() throws Exception {

		String results = this.mockMvc.perform(get("/author/1").accept(HAL_JSON)) //
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, HAL_JSON.toString() + ";charset=UTF-8")) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();

		 assertThat(results).isEqualTo(read(new ClassPathResource("hal-embedded-author-illustrator.json", getClass())));
	}

	@Test
	public void singleItemUsingModelBuilder() throws Exception {

		String results = this.mockMvc.perform(get("/other-author").accept(HAL_JSON)) //
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, HAL_JSON.toString() + ";charset=UTF-8")) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();
		
		assertThat(results).isEqualTo(read(new ClassPathResource("hal-embedded-single-item.json", getClass())));
	}

	@Test
	public void collection() throws Exception {

		String results = this.mockMvc.perform(get("/authors").accept(HAL_JSON)) //
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, HAL_JSON.toString() + ";charset=UTF-8")) //
				.andReturn() //
				.getResponse() //
				.getContentAsString();

		System.out.println(results);

		assertThat(results).isEqualTo(read(new ClassPathResource("hal-embedded-collection.json", getClass())));
	}

	@RestController
	static class EmbeddedController {

		private LinkRelationProvider linkRelationProvider = new EvoInflectorLinkRelationProvider();

		@GetMapping("/other-author")
		RepresentationModel<?> singleItem() {

			return ModelBuilder //
					.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
					.link(new Link("/people/alan-watts")) //
					.build();
		}

		@GetMapping("/authors")
		RepresentationModel<?> collection() {

			return ModelBuilder //
					.collection() //

					.entity(new Author("Greg L. Turnquist", null, null)) //
					.link(linkTo(methodOn(EmbeddedController.class).authorDetails(1)).withSelfRel())
					.link(linkTo(methodOn(EmbeddedController.class).collection()).withRel("authors")) //

					.entity(new Author("Craig Walls", null, null)) //
					.link(linkTo(methodOn(EmbeddedController.class).authorDetails(2)).withSelfRel())
					.link(linkTo(methodOn(EmbeddedController.class).collection()).withRel("authors")) //

					.entity(new Author("Oliver Drotbhom", null, null)) //
					.link(linkTo(methodOn(EmbeddedController.class).authorDetails(2)).withSelfRel())
					.link(linkTo(methodOn(EmbeddedController.class).collection()).withRel("authors")) //

					.rootLink(linkTo(methodOn(EmbeddedController.class).collection()).withSelfRel()) //

					.build();
		}

		@GetMapping("/author/{id}")
		RepresentationModel<?> authorDetails(@PathVariable int id) {

			return ModelBuilder //
					.embed(LinkRelation.of("author")) //

					.entity(new Author("Alan Watts", "January 6, 1915", "November 16, 1973")) //
					.link(new Link("/people/alan-watts")) //

					.embed(LinkRelation.of("illustrator")) //
					.entity(new Author("John Smith", null, null)) //
					.link(new Link("/people/john-smith")) //

					.rootLink(new Link("/books/the-way-of-zen")) //
					.rootLink(new Link("/people/alan-watts", LinkRelation.of("author"))) //
					.rootLink(new Link("/people/john-smith", LinkRelation.of("illustrator"))) //

					.build();
		}
	}

	@Value
	@AllArgsConstructor
	static class Author extends RepresentationModel {
		private String name;
		@Getter(onMethod = @__({ @JsonInclude(Include.NON_NULL) })) private String born;
		@Getter(onMethod = @__({ @JsonInclude(Include.NON_NULL) })) private String died;
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class TestConfig {

		@Bean
		EmbeddedController controller() {
			return new EmbeddedController();
		}

		@Bean
		ObjectMapper testMapper() {

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			return objectMapper;
		}
	}
}
