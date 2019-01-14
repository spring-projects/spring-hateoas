/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.uber;

import static org.assertj.core.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.support.MappingUtils;
import org.springframework.hateoas.uber.Jackson2UberModule.UberHandlerInstantiator;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Greg Turnquist
 * @author Jens Schauder
 */
public class Jackson2UberIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final Links PAGINATION_LINKS = new Links( //
			new Link("localhost", Link.REL_SELF), //
			new Link("foo", Link.REL_NEXT), //
			new Link("bar", Link.REL_PREVIOUS)//
	);

	@Before
	public void setUpModule() {

		this.mapper.registerModule(new Jackson2UberModule());
		this.mapper.setHandlerInstantiator(new UberHandlerInstantiator());
		this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * @see #784
	 */
	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeSingleLink() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));

		assertThat(
				read(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())), ResourceSupport.class))
						.isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2").withRel("orders"));

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeMultipleLinks() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2").withRel("orders"));

		assertThat(
				read(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())), ResourceSupport.class))
						.isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		Resources<String> resources = new Resources<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources)).isEqualTo(MappingUtils.read(new ClassPathResource("resources.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializesSimpleResourcesWithNoLinks() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		Resources<String> expected = new Resources<>(content);
		expected.add(new Link("localhost"));

		String resourcesJson = MappingUtils.read(new ClassPathResource("resources.json", getClass()));
		JavaType resourcesType = mapper.getTypeFactory().constructParametricType(Resources.class, String.class);
		Resources<String> result = mapper.readValue(resourcesJson, resourcesType);

		assertThat(result).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeComplexResourcesSimply() throws IOException {

		List<Resource<String>> content = new ArrayList<>();
		content.add(new Resource<>("first"));
		content.add(new Resource<>("second"));

		Resources<Resource<String>> expected = new Resources<>(content);
		expected.add(new Link("localhost"));

		String resourcesJson = MappingUtils.read(new ClassPathResource("resources.json", getClass()));

		JavaType resourcesType = mapper.getTypeFactory().constructParametricType(Resources.class,
				mapper.getTypeFactory().constructParametricType(Resource.class, String.class));

		Resources<Resource<String>> result = mapper.readValue(resourcesJson, resourcesType);

		assertThat(result).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void renderSimpleResource() throws Exception {

		Resource<String> data = new Resource<>("first", new Link("localhost"));

		assertThat(write(data)).isEqualTo(MappingUtils.read(new ClassPathResource("resource.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void renderResourceWithCustomRel() throws Exception {

		Resource<String> data2 = new Resource<>("second", new Link("localhost").withRel("custom"));

		assertThat(write(data2)).isEqualTo(MappingUtils.read(new ClassPathResource("resource2.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void renderResourceWithMultipleLinks() throws Exception {

		Resource<String> data3 = new Resource<>("third", new Link("localhost"), new Link("second").withRel("second"),
				new Link("third").withRel("third"));

		assertThat(write(data3)).isEqualTo(MappingUtils.read(new ClassPathResource("resource3.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void renderResourceWithMultipleRels() throws Exception {

		Resource<String> data4 = new Resource<>("third", new Link("localhost"),
				new Link("localhost").withRel("http://example.org/rels/todo"), new Link("second").withRel("second"),
				new Link("third").withRel("third"));

		assertThat(write(data4)).isEqualTo(MappingUtils.read(new ClassPathResource("resource4.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeResource() throws IOException {

		JavaType resourceStringType = mapper.getTypeFactory().constructParametricType(Resource.class, String.class);

		Resource expected = new Resource<>("first", new Link("localhost"));
		Resource<String> actual = mapper.readValue(MappingUtils.read(new ClassPathResource("resource.json", getClass())),
				resourceStringType);

		assertThat(actual).isEqualTo(expected);

		Resource<String> expected2 = new Resource<>("second", new Link("localhost").withRel("custom"));
		Resource<String> actual2 = mapper.readValue(MappingUtils.read(new ClassPathResource("resource2.json", getClass())),
				resourceStringType);

		assertThat(actual2).isEqualTo(expected2);

		Resource<String> expected3 = new Resource<>("third", new Link("localhost"), new Link("second").withRel("second"),
				new Link("third").withRel("third"));
		Resource<String> actual3 = mapper.readValue(MappingUtils.read(new ClassPathResource("resource3.json", getClass())),
				resourceStringType);

		assertThat(actual3).isEqualTo(expected3);

		Resource<String> expected4 = new Resource<>("third", new Link("localhost"),
				new Link("localhost").withRel("http://example.org/rels/todo"), new Link("second").withRel("second"),
				new Link("third").withRel("third"));
		Resource<String> actual4 = mapper.readValue(MappingUtils.read(new ClassPathResource("resource4.json", getClass())),
				resourceStringType);

		assertThat(actual4).isEqualTo(expected4);
	}

	/**
	 * @see #784
	 */
	@Test
	public void renderComplexStructure() throws Exception {

		List<Resource<String>> data = new ArrayList<>();
		data.add(new Resource<>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources<Resource<String>> resources = new Resources<>(data);
		resources.add(new Link("localhost"));
		resources.add(new Link("/page/2").withRel("next"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeResources() throws Exception {

		List<Resource<String>> data = new ArrayList<Resource<String>>();
		data.add(new Resource<>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources expected = new Resources<>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		Resources<Resource<String>> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, String.class)));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeEmptyValue() throws Exception {

		List<Resource<String>> data = new ArrayList<Resource<String>>();
		data.add(new Resource<>("", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources expected = new Resources<>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		Resources<Resource<String>> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources-with-resource-objects-and-empty-value.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, String.class)));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeEmptyResources() throws Exception {

		List<Resource<String>> data = new ArrayList<Resource<String>>();
		data.add(new Resource<>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources expected = new Resources<>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		assertThatThrownBy(() -> mapper.readValue( //
				MappingUtils.read(new ClassPathResource("resources-with-empty-resource-objects.json", getClass())), //
				mapper.getTypeFactory() //
						.constructParametricType( //
								Resources.class, //
								mapper.getTypeFactory().constructParametricType(Resource.class, String.class) //
						) //
		)).isInstanceOf(RuntimeException.class);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeResourcesSimply() throws Exception {

		List<String> data = new ArrayList<>();
		data.add("first");
		data.add("second");

		Resources expected = new Resources<>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		Resources<String> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class, String.class));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void serializeWrappedSimplePojo() throws Exception {

		Employee employee = new Employee("Frodo", "ring bearer");
		Resource<Employee> expected = new Resource<>(employee, new Link("/employees/1").withSelfRel());

		String actual = MappingUtils.read(new ClassPathResource("resource-with-simple-pojo.json", getClass()));

		assertThat(write(expected)).isEqualTo(actual);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeWrappedSimplePojo() throws IOException {

		Employee employee = new Employee("Frodo", "ring bearer");
		Resource<Employee> expected = new Resource<>(employee, new Link("/employees/1").withSelfRel());

		Resource<Employee> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-with-simple-pojo.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resource.class, Employee.class));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeWrappedEmptyPojo() throws IOException {

		Employee employee = new Employee();
		Resource<Employee> expected = new Resource<>(employee, new Link("/employees/1").withSelfRel());

		Resource<Employee> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-with-empty-pojo.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resource.class, Employee.class));

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void serializeConcreteResourceSupport() throws Exception {

		EmployeeResource expected = new EmployeeResource("Frodo", "ring bearer");
		expected.add(new Link("/employees/1").withSelfRel());
		expected.add(new Link("/employees").withRel("employees"));

		String actual = MappingUtils.read(new ClassPathResource("resource-support-pojo.json", getClass()));

		assertThat(write(expected)).isEqualTo(actual);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeConcreteResourceSupport() throws Exception {

		EmployeeResource expected = new EmployeeResource("Frodo", "ring bearer");
		expected.add(new Link("/employees/1").withSelfRel());
		expected.add(new Link("/employees").withRel("employees"));

		EmployeeResource actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-support-pojo.json", getClass())), EmployeeResource.class);

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializeEmptyConcreteResourceSupport() throws Exception {

		EmployeeResource expected = new EmployeeResource(null, null);
		expected.add(new Link("/employees/1").withSelfRel());
		expected.add(new Link("/employees").withRel("employees"));

		EmployeeResource actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resource-support-pojo-empty.json", getClass())),
				EmployeeResource.class);

		assertThat(actual).isEqualTo(expected);
	}

	/**
	 * @see #784
	 */
	@Test
	public void serializesPagedResource() throws Exception {

		String actual = write(setupAnnotatedPagedResources());
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())));
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializesPagedResource() throws Exception {

		PagedResources<Resource<Employee>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedResources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, Employee.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	/**
	 * @see #784
	 */
	@Test
	public void deserializesPagedResourceWithEmptyPageInformation() throws Exception {

		PagedResources<Resource<Employee>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("paged-resources-empty-page.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedResources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, Employee.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources(0,0));
	}

	private static Resources<Resource<Employee>> setupAnnotatedPagedResources() {

		return setupAnnotatedPagedResources(2, 4);
	}

	@NotNull
	private static Resources<Resource<Employee>> setupAnnotatedPagedResources(int size, int totalElements) {

		List<Resource<Employee>> content = new ArrayList<>();
		Employee employee = new Employee("Frodo", "ring bearer");
		Resource<Employee> employeeResource = new Resource<>(employee, new Link("/employees/1").withSelfRel());
		content.add(employeeResource);

		return new PagedResources<>(content, new PagedResources.PageMetadata(size, 0, totalElements), PAGINATION_LINKS);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	static class Employee {

		private String name;
		private String role;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class EmployeeResource extends ResourceSupport {

		private String name;
		private String role;
	}

}
