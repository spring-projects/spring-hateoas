/*
 * Copyright 2019-2024 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import tools.jackson.databind.DeserializationFeature;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonJacksonModule;
import org.springframework.hateoas.mediatype.hal.HalJacksonModule;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsJacksonModule;
import org.springframework.hateoas.mediatype.uber.UberJacksonModule;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.hateoas.server.core.DummyInvocationUtils;
import org.springframework.hateoas.server.core.TypeReferences.CollectionModelType;
import org.springframework.hateoas.support.Employee;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ConcurrentLruCache;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Greg Turnquist
 */
class HypermediaWebMvcConfigurerTest {

	MockMvc mockMvc;
	ContextualMapper $ = MappingTestUtils.createMapper();

	void setUp(Class<?> context) {

		var ctx = new AnnotationConfigWebApplicationContext();
		ctx.register(context);
		ctx.setServletContext(new MockServletContext());
		ctx.refresh();

		this.mockMvc = webAppContextSetup(ctx).build();
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalShouldServeHal() throws Exception {

		setUp(HalWebMvcConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalFormsShouldServeHalForms() throws Exception {

		setUp(HalFormsWebMvcConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringCollectionJsonShouldServerCollectionJson() throws Exception {

		setUp(CollectionJsonWebMvcConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringUberShouldServerUber() throws Exception {

		setUp(UberWebMvcConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.UBER_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.UBER_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.UBER_JSON);

		verifyCreatingNewEntityWorks(MediaTypes.UBER_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndHalFormsShouldServerHalAndHalForms() throws Exception {

		setUp(AllHalWebMvcConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndHalFormsShouldAllowCreatingViaHalAndHalForms() throws Exception {

		setUp(AllHalWebMvcConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndCollectionJsonShouldServerHalAndCollectionJson() throws Exception {

		setUp(HalAndCollectionJsonWebMvcConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringHalAndCollectionJsonShouldAllowCreatingViaHalAndCollectionJson() throws Exception {

		setUp(HalAndCollectionJsonWebMvcConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringAllHypermediaTypesShouldServerThemAll() throws Exception {

		setUp(AllHypermediaTypesWebMvcConfig.class);

		verifyRootUriServesHypermedia(MediaTypes.HAL_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_JSON);

		verifyRootUriServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.HAL_FORMS_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.HAL_FORMS_JSON);

		verifyRootUriServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.COLLECTION_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.COLLECTION_JSON);

		verifyRootUriServesHypermedia(MediaTypes.UBER_JSON);
		verifyAggregateRootServesHypermedia(MediaTypes.UBER_JSON);
		verifySingleItemResourceServesHypermedia(MediaTypes.UBER_JSON);
	}

	/**
	 * @see #728
	 */
	@Test
	void registeringAllHypermediaTypesShouldAllowCreatingThroughAllFormats() throws Exception {

		setUp(AllHypermediaTypesWebMvcConfig.class);

		verifyCreatingNewEntityWorks(MediaTypes.HAL_JSON);
		verifyCreatingNewEntityWorks(MediaTypes.HAL_FORMS_JSON);
		verifyCreatingNewEntityWorks(MediaTypes.COLLECTION_JSON);
		verifyCreatingNewEntityWorks(MediaTypes.UBER_JSON);
	}

	@Test
	void callingForUnregisteredMediaTypeShouldFallBackToDefaultHandler() throws Exception {

		setUp(HalWebMvcConfig.class);

		this.mockMvc.perform(get("/").accept(MediaTypes.UBER_JSON))
				.andExpect(status().isNotAcceptable());
	}

	@Test // #118
	void linkCreationConsidersRegisteredConverters() throws Exception {

		setUp(WithConversionService.class);

		this.mockMvc.perform(get("/sample/4711"))
				.andExpect(status().isIAmATeapot());
	}

	@Test // #1830
	void wipesDummyInvocationsCachePerRequest() throws Exception {

		setUp(HalWebMvcConfig.class);

		this.mockMvc.perform(get("/dynamicLink"))
				.andExpect(status().isOk());

		var cache = (ThreadLocal<ConcurrentLruCache<?, ?>>) ReflectionTestUtils.getField(null, DummyInvocationUtils.class,
				"CACHE");

		assertThat(cache.get().size()).isZero();
	}

	private void verifyRootUriServesHypermedia(MediaType mediaType) throws Exception {
		verifyRootUriServesHypermedia(mediaType, mediaType);
	}

	private void verifyRootUriServesHypermedia(MediaType requestType, MediaType responseType) throws Exception {

		getMapper(responseType)
				.assertDeserializes(mockMvc.perform(get("/").accept(requestType)) //
						.andExpect(status().isOk()) //
						.andExpect(header().string(HttpHeaders.CONTENT_TYPE, responseType.toString())) //
						.andReturn() //
						.getResponse().getContentAsString())
				.into(RepresentationModel.class)
				.matching(model -> {

					assertThat(model.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/", IanaLinkRelations.SELF), Link.of("/employees", "employees"));
				});
	}

	private void verifyAggregateRootServesHypermedia(MediaType mediaType) throws Exception {
		verifyAggregateRootServesHypermedia(mediaType, mediaType);
	}

	private void verifyAggregateRootServesHypermedia(MediaType requestType, MediaType responseType) throws Exception {

		getMapper(requestType)
				.assertDeserializes(mockMvc.perform(get("/employees").accept(requestType)) //
						.andExpect(status().isOk()) //
						.andExpect(header().string(HttpHeaders.CONTENT_TYPE, responseType.toString())) //
						.andReturn().getResponse().getContentAsString())
				.into(new CollectionModelType<EntityModel<Employee>>() {})
				.matching(resources -> {

					assertThat(resources.getLinks())
							.containsExactlyInAnyOrder(Link.of("/employees", IanaLinkRelations.SELF));
					assertThat(resources.getContent())
							.hasSize(1)
							.element(0)
							.satisfies(resource -> {

								assertThat(resource.getContent()).isEqualTo(new Employee("Frodo Baggins", "ring bearer"));
								assertThat(resource.getLinks()) //
										.containsExactlyInAnyOrder(Link.of("/employees/1", IanaLinkRelations.SELF),
												Link.of("/employees", "employees"));
							});
				});
	}

	private void verifySingleItemResourceServesHypermedia(MediaType mediaType) throws Exception {
		verifySingleItemResourceServesHypermedia(mediaType, mediaType);
	}

	private void verifySingleItemResourceServesHypermedia(MediaType requestType, MediaType responseType)
			throws Exception {

		getMapper(responseType)
				.assertDeserializes(mockMvc.perform(get("/employees/1").accept(requestType)) //
						.andExpect(status().isOk()) //
						.andExpect(header().string(HttpHeaders.CONTENT_TYPE, responseType.toString())) //
						.andReturn().getResponse().getContentAsString())
				.intoEntityModel(Employee.class)
				.matching(result -> {

					assertThat(result.getContent())
							.isEqualTo(new Employee("Frodo Baggins", "ring bearer"));
					assertThat(result.getLinks())
							.containsExactlyInAnyOrder(Link.of("/employees/1", IanaLinkRelations.SELF),
									Link.of("/employees", "employees"));
				});

	}

	private void verifyCreatingNewEntityWorks(MediaType mediaType) throws Exception {
		verifyCreatingNewEntityWorks(mediaType, mediaType);
	}

	private void verifyCreatingNewEntityWorks(MediaType contentType, MediaType responseType) throws Exception {
		verifyCreation("/employees", contentType, responseType);
	}

	private void verifyCreation(String uri, MediaType contentType, MediaType responseType) throws Exception {

		getMapper(responseType)
				.assertSerializes(new Employee("Samwise Gamgee", "gardener"))
				.map(payload -> mockMvc.perform( //
						post(uri) //
								.accept(contentType) //
								.contentType(contentType) //
								.content(payload)) //
						.andExpect(status().isOk()) //
						.andExpect(header().string(HttpHeaders.CONTENT_TYPE, responseType.toString())) //
						.andReturn().getResponse().getContentAsString())
				.intoEntityModel(Employee.class)
				.matching(result -> {
					assertThat(result.getContent()).isEqualTo(new Employee("Samwise Gamgee", "gardener"));
					assertThat(result.getLinks()) //
							.containsExactlyInAnyOrder(Link.of("/employees/1", IanaLinkRelations.SELF),
									Link.of("/employees", "employees"));
				});

	}

	private static ContextualMapper getMapper(MediaType mediaType) {

		return MappingTestUtils.createMapper(builder -> {

			builder = builder.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			if (mediaType == MediaTypes.HAL_JSON) {
				return builder.addModule(new HalJacksonModule());
			} else if (mediaType == MediaTypes.UBER_JSON) {
				return builder.addModule(new UberJacksonModule());
			} else if (mediaType == MediaTypes.HAL_FORMS_JSON) {
				return builder.addModule(new HalFormsJacksonModule());
			} else if (mediaType == MediaTypes.COLLECTION_JSON) {
				return builder.addModule(new CollectionJsonJacksonModule());
			}

			return builder;
		});
	}

	@Configuration
	@WebAppConfiguration
	@EnableWebMvc
	static abstract class BaseConfig {

		@Bean
		TestController testController() {
			return new TestController();
		}
	}

	@EnableHypermediaSupport(type = HAL)
	static class HalWebMvcConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = HAL_FORMS)
	static class HalFormsWebMvcConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = COLLECTION_JSON)
	static class CollectionJsonWebMvcConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = UBER)
	static class UberWebMvcConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS })
	static class AllHalWebMvcConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, COLLECTION_JSON })
	static class HalAndCollectionJsonWebMvcConfig extends BaseConfig {}

	@EnableHypermediaSupport(type = { HAL, HAL_FORMS, COLLECTION_JSON, UBER })
	static class AllHypermediaTypesWebMvcConfig extends BaseConfig {}

	@RestController
	static class TestController {

		private List<Employee> employees;
		private EmployeeResourceAssembler assembler = new EmployeeResourceAssembler();

		TestController() {

			this.employees = new ArrayList<>();
			this.employees.add(new Employee("Frodo Baggins", "ring bearer"));
		}

		@GetMapping("/")
		RepresentationModel<?> root() {

			RepresentationModel<?> root = new RepresentationModel<>();

			root.add(Link.of("/").withSelfRel());
			root.add(Link.of("/employees").withRel("employees"));

			return root;
		}

		@GetMapping("/employees")
		CollectionModel<EntityModel<Employee>> employees() {
			return this.assembler.toCollectionModel(this.employees);
		}

		@PostMapping("/employees")
		EntityModel<Employee> newEmployee(@RequestBody Employee newEmployee) {

			this.employees.add(newEmployee);

			return this.assembler.toModel(newEmployee);
		}

		@GetMapping("/employees/{id}")
		EntityModel<Employee> employee(@PathVariable String id) {
			return this.assembler.toModel(this.employees.get(0));
		}

		@PutMapping("/employees/{id}")
		EntityModel<Employee> updateEmployee(@RequestBody Employee newEmployee, @PathVariable String id) {

			this.employees.add(newEmployee);

			return this.assembler.toModel(newEmployee);
		}

		@GetMapping("/dynamicLink")
		EntityModel<Employee> dynamicLink() {
			return EntityModel.of(employees.get(0)) //
					.add(linkTo(methodOn(TestController.class).employee("1")).withSelfRel());
		}
	}

	static class EmployeeResourceAssembler implements SimpleRepresentationModelAssembler<Employee> {

		@Override
		public void addLinks(EntityModel<Employee> resource) {

			resource.add(Link.of("/employees/1").withSelfRel());
			resource.add(Link.of("/employees").withRel("employees"));
		}

		@Override
		public void addLinks(CollectionModel<EntityModel<Employee>> resources) {
			resources.add(Link.of("/employees").withSelfRel());
		}
	}

	// #118

	@Configuration
	static class WithConversionService extends BaseConfig implements WebMvcConfigurer {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurer#addFormatters(org.springframework.format.FormatterRegistry)
		 */
		@Override
		public void addFormatters(FormatterRegistry registry) {
			registry.addConverter(Sample.class, String.class, source -> "sample");
			registry.addConverter(String.class, Sample.class, source -> new Sample());
		}

		static class Sample {}

		@Controller
		static class SampleController {

			@GetMapping("/sample/{sample}")
			HttpEntity<?> sample(@PathVariable Sample sample) {

				linkTo(methodOn(SampleController.class).sample(new Sample())).withSelfRel();

				return new ResponseEntity<>(HttpStatus.I_AM_A_TEAPOT);
			}
		}
	}
}
