/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import static org.assertj.core.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.core.MethodParameters;
import org.springframework.hateoas.support.Employee;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Greg Turnquist
 */
class PropertyUtilsTest {

	@Test
	void simpleObject() {

		Employee employee = new Employee("Frodo Baggins", "ring bearer");

		Map<String, Object> properties = PropertyUtils.extractPropertyValues(employee);

		assertThat(properties).hasSize(2);
		assertThat(properties.keySet()).contains("name", "role");
		assertThat(properties.get("name")).isEqualTo("Frodo Baggins");
		assertThat(properties.get("role")).isEqualTo("ring bearer");
	}

	@Test
	void simpleObjectWrappedAsResource() {

		Employee employee = new Employee("Frodo Baggins", "ring bearer");
		EntityModel<Employee> employeeResource = new EntityModel<>(employee);

		Map<String, Object> properties = PropertyUtils.extractPropertyValues(employeeResource);

		assertThat(properties).hasSize(2);
		assertThat(properties.keySet()).contains("name", "role");
		assertThat(properties.get("name")).isEqualTo("Frodo Baggins");
		assertThat(properties.get("role")).isEqualTo("ring bearer");
	}

	@Test
	void resourceWrappedSpringMvcParameter() {

		Method method = ReflectionUtils.findMethod(TestController.class, "newEmployee", EntityModel.class);
		MethodParameters parameters = MethodParameters.of(method);

		ResolvableType resolvableType = parameters.getParametersWith(RequestBody.class).stream() //
				.findFirst().map(it -> ResolvableType.forMethodParameter(it.getMethod(), it.getParameterIndex()))
				.orElseThrow(() -> new RuntimeException("Didn't find a parameter annotated with @RequestBody!"));

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(resolvableType);

		assertThat(metadata.stream()).hasSize(2);
		assertThat(metadata.stream().map(PropertyMetadata::getName)).contains("name", "role");
	}

	@Test
	void objectWithIgnorableAttributes() {

		EmployeeWithCustomizedReaders employee = new EmployeeWithCustomizedReaders("Frodo", "Baggins", "ring bearer",
				"password", "fbaggins", "ignore this one");

		Map<String, Object> properties = PropertyUtils.extractPropertyValues(employee);

		assertThat(properties).hasSize(6);
		assertThat(properties.keySet()).containsExactlyInAnyOrder("firstName", "lastName", "role", "username", "fullName",
				"usernameAndLastName");
		assertThat(properties.entrySet()).containsExactlyInAnyOrder(new SimpleEntry<>("firstName", "Frodo"),
				new SimpleEntry<>("lastName", "Baggins"), new SimpleEntry<>("role", "ring bearer"),
				new SimpleEntry<>("username", "fbaggins"), new SimpleEntry<>("fullName", "Frodo Baggins"),
				new SimpleEntry<>("usernameAndLastName", "fbaggins+++Baggins"));
	}

	@Test
	void objectWithNullReturningGetter() {

		EmployeeWithNullReturningGetter employee = new EmployeeWithNullReturningGetter("Frodo");

		Map<String, Object> properties = PropertyUtils.extractPropertyValues(employee);

		assertThat(properties).hasSize(2);
		assertThat(properties.keySet()).containsExactlyInAnyOrder("name", "father");
		assertThat(properties.entrySet()).containsExactlyInAnyOrder(new SimpleEntry<>("name", "Frodo"),
				new SimpleEntry<>("father", null));
	}

	@Test
	void considersAccessorAvailablility() {

		PayloadMetadata metadata = PropertyUtils.getExposedProperties(MethodExposurePayload.class);

		assertThat(metadata.getPropertyMetadata("readWrite")) //
				.map(PropertyMetadata::isReadOnly) //
				.hasValue(false);

		assertThat(metadata.getPropertyMetadata("readOnly")) //
				.map(PropertyMetadata::isReadOnly) //
				.hasValue(true);
	}

	@Test
	void considersJsr303Annotations() {

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(Jsr303SamplePayload.class);

		assertThat(metadata.getPropertyMetadata("nonNull")).hasValueSatisfying(it -> {
			assertThat(it.isRequired()).isTrue();
		});

		assertThat(metadata.getPropertyMetadata("pattern")).hasValueSatisfying(it -> {
			assertThat(it.getPattern()).hasValue("\\w");
		});

		assertThat(metadata.getPropertyMetadata("annotated")).hasValueSatisfying(it -> {
			assertThat(it.getPattern()).hasValue("regex");
		});
	}

	@Test // #1121
	void considersPropertyWithoutReader() throws Exception {

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(WithoutReaderMethod.class);

		assertThat(metadata.getPropertyMetadata("firstname")).isPresent();
	}

	@Data
	@AllArgsConstructor
	@JsonIgnoreProperties({ "ignoreThisProperty" })
	static class EmployeeWithCustomizedReaders {

		private String firstName;
		private String lastName;
		private String role;
		@JsonIgnore private String password;
		@JsonIgnore(false) private String username;
		private String ignoreThisProperty;

		public String getFullName() {
			return this.firstName + " " + this.lastName;
		}

		@JsonIgnore
		public String getEncodedPassword() {
			return "{bcrypt}" + this.password;
		}

		@JsonIgnore(false)
		public String getUsernameAndLastName() {
			return this.username + "+++" + this.lastName;
		}
	}

	@Data
	static class EmployeeWithNullReturningGetter {

		private String name;
		private String father;

		EmployeeWithNullReturningGetter(String name) {
			this.name = name;
		}
	}

	@Value
	static class Jsr303SamplePayload {

		@NotNull String nonNull;
		@Pattern(regexp = "\\w") String pattern;
		TypeAnnotated annotated;
	}

	@Pattern(regexp = "regex")
	static class TypeAnnotated {}

	static class MethodExposurePayload {

		@Getter @Setter String readWrite;
		@Getter String readOnly;
	}

	@RestController
	static class TestController {

		@GetMapping("/")
		public Employee newEmployee(@RequestBody EntityModel<Employee> employee) {
			return employee.getContent();
		}
	}

	static class WithoutReaderMethod {

		private String firstname;

		public void setFirstname(String firstname) {
			this.firstname = firstname;
		}
	}
}
