/*
 * Copyright 2017-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.uber;

import static org.assertj.core.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 * @author Jens Schauder
 */
class Jackson2UberIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			Link.of("localhost", IanaLinkRelations.SELF), //
			Link.of("foo", IanaLinkRelations.NEXT), //
			Link.of("bar", IanaLinkRelations.PREV) //
	);

	@BeforeEach
	void setUpModule() {

		this.mapper.registerModule(new Jackson2UberModule());
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * @see #784
	 */
	@Test
	void rendersSingleLinkAsObject() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(Link.of("localhost").withSelfRel());

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeSingleLink() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(Link.of("localhost"));

		assertThat(
				read(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())), RepresentationModel.class))
						.isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void rendersMultipleLinkAsArray() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(Link.of("localhost"));
		resourceSupport.add(Link.of("localhost2").withRel("orders"));

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeMultipleLinks() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(Link.of("localhost"));
		expected.add(Link.of("localhost2").withRel("orders"));

		assertThat(read(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())),
				RepresentationModel.class)).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> resources = CollectionModel.of(content);
		resources.add(Link.of("localhost"));

		assertThat(write(resources)).isEqualTo(MappingUtils.read(new ClassPathResource("resources.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializesSimpleResourcesWithNoLinks() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> expected = CollectionModel.of(content);
		expected.add(Link.of("localhost"));

		String resourcesJson = MappingUtils.read(new ClassPathResource("resources.json", getClass()));
		JavaType resourcesType = mapper.getTypeFactory().constructParametricType(CollectionModel.class, String.class);
		CollectionModel<String> result = mapper.readValue(resourcesJson, resourcesType);

		assertThat(result).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeComplexResourcesSimply() throws IOException {

		List<EntityModel<String>> content = new ArrayList<>();
		content.add(EntityModel.of("first"));
		content.add(EntityModel.of("second"));

		CollectionModel<EntityModel<String>> expected = CollectionModel.of(content);
		expected.add(Link.of("localhost"));

		String resourcesJson = MappingUtils.read(new ClassPathResource("resources.json", getClass()));

		JavaType resourcesType = mapper.getTypeFactory().constructParametricType(CollectionModel.class,
				mapper.getTypeFactory().constructParametricType(EntityModel.class, String.class));

		CollectionModel<EntityModel<String>> result = mapper.readValue(resourcesJson, resourcesType);

		assertThat(result).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void renderSimpleResource() throws Exception {

		EntityModel<String> data = EntityModel.of("first", Link.of("localhost"));

		assertThat(write(data)).isEqualTo(MappingUtils.read(new ClassPathResource("resource.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void renderResourceWithCustomRel() throws Exception {

		EntityModel<String> data2 = EntityModel.of("second", Link.of("localhost").withRel("custom"));

		assertThat(write(data2)).isEqualTo(MappingUtils.read(new ClassPathResource("resource2.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void renderResourceWithMultipleLinks() throws Exception {

		EntityModel<String> data3 = EntityModel.of("third", Link.of("localhost"), Link.of("second").withRel("second"),
				Link.of("third").withRel("third"));

		assertThat(write(data3)).isEqualTo(MappingUtils.read(new ClassPathResource("resource3.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void renderResourceWithMultipleRels() throws Exception {

		EntityModel<String> data4 = EntityModel.of("third", Link.of("localhost"),
				Link.of("localhost").withRel("https://example.org/rels/todo"), Link.of("second").withRel("second"),
				Link.of("third").withRel("third"));

		assertThat(write(data4)).isEqualTo(MappingUtils.read(new ClassPathResource("resource4.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeResource() throws IOException {

		JavaType resourceStringType = mapper.getTypeFactory().constructParametricType(EntityModel.class, String.class);

		EntityModel<?> expected = EntityModel.of("first", Link.of("localhost"));
		EntityModel<String> actual = mapper.readValue(MappingUtils.read(new ClassPathResource("resource.json", getClass())),
				resourceStringType);

		assertThat(actual).isEqualTo(expected);

		EntityModel<String> expected2 = EntityModel.of("second", Link.of("localhost").withRel("custom"));
		EntityModel<String> actual2 = mapper
				.readValue(MappingUtils.read(new ClassPathResource("resource2.json", getClass())), resourceStringType);

		assertThat(actual2).isEqualTo(expected2);

		EntityModel<String> expected3 = EntityModel.of("third", Link.of("localhost"),
				Link.of("second").withRel("second"), Link.of("third").withRel("third"));
		EntityModel<String> actual3 = mapper
				.readValue(MappingUtils.read(new ClassPathResource("resource3.json", getClass())), resourceStringType);

		assertThat(actual3).isEqualTo(expected3);

		EntityModel<String> expected4 = EntityModel.of("third", Link.of("localhost"),
				Link.of("localhost").withRel("https://example.org/rels/todo"), Link.of("second").withRel("second"),
				Link.of("third").withRel("third"));
		EntityModel<String> actual4 = mapper
				.readValue(MappingUtils.read(new ClassPathResource("resource4.json", getClass())), resourceStringType);

		assertThat(actual4).isEqualTo(expected4);
	}

	/**
	 * @see #784
	 */
	@Test
	void renderComplexStructure() throws Exception {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		CollectionModel<EntityModel<String>> resources = CollectionModel.of(data);
		resources.add(Link.of("localhost"));
		resources.add(Link.of("/page/2").withRel("next"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeResources() throws Exception {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		CollectionModel<?> expected = CollectionModel.of(data);
		expected.add(Link.of("localhost"));
		expected.add(Link.of("/page/2").withRel("next"));

		CollectionModel<EntityModel<String>> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, String.class)));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeEmptyValue() throws Exception {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(EntityModel.of("", Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		CollectionModel<?> expected = CollectionModel.of(data);
		expected.add(Link.of("localhost"));
		expected.add(Link.of("/page/2").withRel("next"));

		CollectionModel<EntityModel<String>> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources-with-resource-objects-and-empty-value.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, String.class)));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void serializeEmptyResources() throws Exception {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		CollectionModel<?> source = CollectionModel.of(data);
		source.add(Link.of("localhost"));
		source.add(Link.of("/page/2").withRel("next"));

		assertThat(write(source))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeEmptyResources() {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		CollectionModel<?> expected = CollectionModel.of(data);
		expected.add(Link.of("localhost"));
		expected.add(Link.of("/page/2").withRel("next"));

		assertThatThrownBy(() -> mapper.readValue( //
				MappingUtils.read(new ClassPathResource("resources-with-empty-resource-objects.json", getClass())), //
				mapper.getTypeFactory() //
						.constructParametricType( //
								CollectionModel.class, //
								mapper.getTypeFactory().constructParametricType(EntityModel.class, String.class) //
						) //
		)).isInstanceOf(RuntimeException.class);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeResourcesSimply() throws Exception {

		List<String> data = new ArrayList<>();
		data.add("first");
		data.add("second");

		CollectionModel<?> expected = CollectionModel.of(data);
		expected.add(Link.of("localhost"));
		expected.add(Link.of("/page/2").withRel("next"));

		CollectionModel<String> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class, String.class));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void serializeWrappedSimplePojo() throws Exception {

		Employee employee = new Employee("Frodo", "ring bearer");
		EntityModel<Employee> expected = EntityModel.of(employee, Link.of("/employees/1").withSelfRel());

		String actual = MappingUtils.read(new ClassPathResource("resource-with-simple-pojo.json", getClass()));

		assertThat(write(expected)).isEqualTo(actual);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeWrappedSimplePojo() throws IOException {

		Employee employee = new Employee("Frodo", "ring bearer");
		EntityModel<Employee> expected = EntityModel.of(employee, Link.of("/employees/1").withSelfRel());

		EntityModel<Employee> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-with-simple-pojo.json", getClass())),
				mapper.getTypeFactory().constructParametricType(EntityModel.class, Employee.class));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeWrappedEmptyPojo() throws IOException {

		Employee employee = new Employee();
		EntityModel<Employee> expected = EntityModel.of(employee, Link.of("/employees/1").withSelfRel());

		EntityModel<Employee> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-with-empty-pojo.json", getClass())),
				mapper.getTypeFactory().constructParametricType(EntityModel.class, Employee.class));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void serializeConcreteResourceSupport() throws Exception {

		EmployeeResource expected = new EmployeeResource("Frodo", "ring bearer");
		expected.add(Link.of("/employees/1").withSelfRel());
		expected.add(Link.of("/employees").withRel("employees"));

		String actual = MappingUtils.read(new ClassPathResource("resource-support-pojo.json", getClass()));

		assertThat(write(expected)).isEqualTo(actual);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeConcreteResourceSupport() throws Exception {

		EmployeeResource expected = new EmployeeResource("Frodo", "ring bearer");
		expected.add(Link.of("/employees/1").withSelfRel());
		expected.add(Link.of("/employees").withRel("employees"));

		EmployeeResource actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-support-pojo.json", getClass())), EmployeeResource.class);

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeEmptyConcreteResourceSupport() throws Exception {

		EmployeeResource expected = new EmployeeResource(null, null);
		expected.add(Link.of("/employees/1").withSelfRel());
		expected.add(Link.of("/employees").withRel("employees"));

		EmployeeResource actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-support-pojo-empty.json", getClass())),
				EmployeeResource.class);

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void serializesPagedResource() throws Exception {

		String actual = write(setupAnnotatedPagedResources());
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializesPagedResource() throws Exception {

		PagedModel<EntityModel<Employee>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, Employee.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializesPagedResourceWithEmptyPageInformation() throws Exception {

		PagedModel<EntityModel<Employee>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("paged-resources-empty-page.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, Employee.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources(0, 0));
	}

	/**
	 * @see #784
	 */
	@Test
	void handleTemplatedLinksOnDeserialization() throws IOException {

		RepresentationModel<?> original = new RepresentationModel<>();
		original.add(Link.of("/orders{?id}", "order"));

		String serialized = mapper.writeValueAsString(original);

		String expected = MappingUtils.read(new ClassPathResource("resource-with-templated-link.json", getClass()));

		assertThat(serialized).isEqualTo(expected);

		RepresentationModel<?> deserialized = mapper.readValue(serialized, RepresentationModel.class);

		assertThat(deserialized).isEqualTo(original);
	}

	private static CollectionModel<EntityModel<Employee>> setupAnnotatedPagedResources() {

		return setupAnnotatedPagedResources(2, 4);
	}

	@NotNull
	private static CollectionModel<EntityModel<Employee>> setupAnnotatedPagedResources(int size, int totalElements) {

		List<EntityModel<Employee>> content = new ArrayList<>();
		Employee employee = new Employee("Frodo", "ring bearer");
		EntityModel<Employee> employeeResource = EntityModel.of(employee, Link.of("/employees/1").withSelfRel());
		content.add(employeeResource);

		return PagedModel.of(content, new PagedModel.PageMetadata(size, 0, totalElements), PAGINATION_LINKS);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Employee {

		private String name;
		private String role;
	}

	@Data
	@AllArgsConstructor()
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper = true)
	static class EmployeeResource extends RepresentationModel<EmployeeResource> {
		private @Nullable String name, role;
	}
}
