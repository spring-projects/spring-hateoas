/*
 * Copyright 2017-2026 the original author or authors.
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
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author Greg Turnquist
 * @author Jens Schauder
 */
public class UberJacksonModuleIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			Link.of("localhost", IanaLinkRelations.SELF), //
			Link.of("foo", IanaLinkRelations.NEXT), //
			Link.of("bar", IanaLinkRelations.PREV) //
	);

	ContextualMapper $ = MappingTestUtils.createMapper(it -> it.addModule(new UberJacksonModule()));

	/**
	 * @see #784
	 */
	@Test
	void rendersSingleLinkAsObject() throws Exception {

		$.assertSerializes(new RepresentationModel<>().add(Link.of("localhost").withSelfRel()))
				.intoContentOf("resource-support.json")
				.andBack();
	}

	/**
	 * @see #784
	 */
	@Test
	void rendersMultipleLinkAsArray() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>()
				.add(Link.of("localhost"))
				.add(Link.of("localhost2").withRel("orders"));

		$.assertSerializes(resourceSupport)
				.intoContentOf("resource-support-2.json")
				.andBack();
	}

	/**
	 * @see #784
	 */
	@Test
	void rendersSimpleResourcesAsEmbedded() throws Exception {

		var model = CollectionModel.of(List.of("first", "second"))
				.add(Link.of("localhost"));

		$.assertSerializes(model)
				.intoContentOf("resources.json")
				.andBack(String.class);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeComplexResourcesSimply() throws IOException {

		var expected = CollectionModel
				.of(List.of(EntityModel.of("first"), EntityModel.of("second")))
				.add(Link.of("localhost"));

		$.assertDeserializesFile("resources.json")
				.into(expected, EntityModel.class, String.class);

	}

	/**
	 * @see #784
	 */
	@Test
	void renderSimpleResource() throws Exception {

		var model = EntityModel.of("first", Link.of("localhost"));

		$.assertSerializes(model).intoContentOf("resource.json");
	}

	/**
	 * @see #784
	 */
	@Test
	void renderResourceWithCustomRel() throws Exception {

		var model = EntityModel.of("second", Link.of("localhost").withRel("custom"));

		$.assertSerializes(model).intoContentOf("resource2.json");
	}

	/**
	 * @see #784
	 */
	@Test
	void renderResourceWithMultipleLinks() throws Exception {

		var model = EntityModel.of("third")
				.add(Link.of("localhost"))
				.add(Link.of("second").withRel("second"))
				.add(Link.of("third").withRel("third"));

		$.assertSerializes(model).intoContentOf("resource3.json");
	}

	/**
	 * @see #784
	 */
	@Test
	void renderResourceWithMultipleRels() {

		var model = EntityModel.of("third", Link.of("localhost"),
				Link.of("localhost").withRel("https://example.org/rels/todo"), Link.of("second").withRel("second"),
				Link.of("third").withRel("third"));

		$.assertSerializes(model).intoContentOf("resource4.json");
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeResource() throws IOException {

		$.assertDeserializesFile("resource.json")
				.into(EntityModel.of("first",
						Link.of("localhost")));

		$.assertDeserializesFile("resource2.json")
				.into(EntityModel.of("second",
						Link.of("localhost").withRel("custom")));

		$.assertDeserializesFile("resource3.json")
				.into(EntityModel.of("third",
						Link.of("localhost"),
						Link.of("second").withRel("second"),
						Link.of("third").withRel("third")));

		$.assertDeserializesFile("resource4.json")
				.into(EntityModel.of("third",
						Link.of("localhost"),
						Link.of("localhost").withRel("https://example.org/rels/todo"),
						Link.of("second").withRel("second"),
						Link.of("third").withRel("third")));
	}

	/**
	 * @see #784
	 */
	@Test
	void renderComplexStructure() throws Exception {

		var data = List.of(
				EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")),
				EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		var model = CollectionModel.of(data)
				.add(Link.of("localhost"))
				.add(Link.of("/page/2").withRel("next"));

		$.assertSerializes(model).intoContentOf("resources-with-resource-objects.json");
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeResources() throws Exception {

		var data = List.of(
				EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")),
				EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		var expected = CollectionModel.of(data)
				.add(Link.of("localhost"))
				.add(Link.of("/page/2").withRel("next"));

		$.assertDeserializesFile("resources-with-resource-objects.json")
				.into(expected, EntityModel.class, String.class);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeEmptyValue() throws Exception {

		var data = List.of(
				EntityModel.of("", Link.of("localhost"), Link.of("orders").withRel("orders")),
				EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		var expected = CollectionModel.of(data)
				.add(Link.of("localhost"))
				.add(Link.of("/page/2").withRel("next"));

		$.assertDeserializesFile("resources-with-resource-objects-and-empty-value.json")
				.into(expected, EntityModel.class, String.class);

	}

	/**
	 * @see #784
	 */
	@Test
	void serializeEmptyResources() throws Exception {

		var data = List.of(
				EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")),
				EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		var model = CollectionModel.of(data)
				.add(Link.of("localhost"))
				.add(Link.of("/page/2").withRel("next"));

		$.assertSerializes(model)
				.intoContentOf("resources-with-resource-objects.json");
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeEmptyResources() {

		assertThatRuntimeException().isThrownBy(
				() -> $.readFileIntoEntityCollectionModel("resources-with-empty-resource-objects.json", String.class));
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeResourcesSimply() throws Exception {

		var expected = CollectionModel.of(List.of("first", "second"))
				.add(Link.of("localhost"))
				.add(Link.of("/page/2").withRel("next"));

		$.assertDeserializesFile("resources-with-resource-objects.json")
				.into(expected, String.class);
	}

	/**
	 * @see #784
	 */
	@Test
	void serializeWrappedSimplePojo() throws Exception {

		var employee = new Employee("Frodo", "ring bearer");
		var expected = EntityModel.of(employee, Link.of("/employees/1").withSelfRel());

		$.assertSerializes(expected)
				.intoContentOf("resource-with-simple-pojo.json")
				.andBack(Employee.class);

	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeWrappedEmptyPojo() throws IOException {

		var expected = EntityModel.of(new Employee(), Link.of("/employees/1").withSelfRel());

		$.assertDeserializesFile("resource-with-empty-pojo.json").into(expected, Employee.class);
	}

	/**
	 * @see #784
	 */
	@Test
	void serializeConcreteResourceSupport() throws Exception {

		var expected = new EmployeeModel("Frodo", "ring bearer")
				.add(Link.of("/employees/1").withSelfRel())
				.add(Link.of("/employees").withRel("employees"));

		$.assertSerializes(expected)
				.intoContentOf("resource-support-pojo.json")
				.andBack();
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializeEmptyConcreteResourceSupport() throws Exception {

		var expected = new EmployeeModel(null, null)
				.add(Link.of("/employees/1").withSelfRel())
				.add(Link.of("/employees").withRel("employees"));

		$.assertDeserializesFile("resource-support-pojo-empty.json").into(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	void serializesPagedResource() throws Exception {

		$.assertSerializes(setupAnnotatedPagedModel())
				.intoContentOf("paged-resources.json")
				.andBack(EntityModel.class, Employee.class);
	}

	/**
	 * @see #784
	 */
	@Test
	void deserializesPagedResourceWithEmptyPageInformation() throws Exception {

		$.assertDeserializesFile("paged-resources-empty-page.json")
				.into(setupAnnotatedPagedModel(0, 0), EntityModel.class, Employee.class);
	}

	/**
	 * @see #784
	 */
	@Test
	void handleTemplatedLinksOnDeserialization() throws IOException {

		var original = new RepresentationModel<>()
				.add(Link.of("/orders{?id}", "order"));

		$.assertSerializes(original)
				.intoContentOf("resource-with-templated-link.json")
				.andBack();
	}

	private static PagedModel<EntityModel<Employee>> setupAnnotatedPagedModel() {
		return setupAnnotatedPagedModel(2, 4);
	}

	@NotNull
	private static PagedModel<EntityModel<Employee>> setupAnnotatedPagedModel(int size, int totalElements) {

		var employee = new Employee("Frodo", "ring bearer");
		var employeeResource = EntityModel.of(employee, Link.of("/employees/1").withSelfRel());

		return PagedModel.of(List.of(employeeResource), new PagedModel.PageMetadata(size, 0, totalElements),
				PAGINATION_LINKS);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Employee {

		private String name;
		private String role;
	}

	@Data
	@AllArgsConstructor(onConstructor = @__(@JsonCreator))
	@NoArgsConstructor
	@EqualsAndHashCode(callSuper = true)
	static class EmployeeModel extends RepresentationModel<EmployeeModel> {
		private @Nullable String name, role;
	}
}
