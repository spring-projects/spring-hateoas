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
package org.springframework.hateoas.support;

import static org.assertj.core.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Greg Turnquist
 */
public class PropertyUtilsTest {

	@Test
	public void simpleObject() {

		Employee employee = new Employee("Frodo Baggins", "ring bearer");

		Map<String, Object> properties = PropertyUtils.findProperties(employee);

		assertThat(properties).hasSize(2);
		assertThat(properties.keySet()).contains("name", "role");
		assertThat(properties.get("name")).isEqualTo("Frodo Baggins");
		assertThat(properties.get("role")).isEqualTo("ring bearer");
	}

	@Test
	public void simpleObjectWrappedAsResource() {

		Employee employee = new Employee("Frodo Baggins", "ring bearer");
		Resource<Employee> employeeResource = new Resource<>(employee);

		Map<String, Object> properties = PropertyUtils.findProperties(employeeResource);

		assertThat(properties).hasSize(2);
		assertThat(properties.keySet()).contains("name", "role");
		assertThat(properties.get("name")).isEqualTo("Frodo Baggins");
		assertThat(properties.get("role")).isEqualTo("ring bearer");
	}

	@Test
	public void resourceWrappedSpringMvcParameter() throws NoSuchMethodException {

		Method method = ReflectionUtils.findMethod(TestController.class, "newEmployee", Resource.class);
		MethodParameters parameters = new MethodParameters(method);

		ResolvableType resolvableType = parameters.getParametersWith(RequestBody.class).stream()
			.findFirst()
			.map(methodParameter -> ResolvableType.forMethodParameter(methodParameter.getMethod(), methodParameter.getParameterIndex()))
			.orElseThrow(() -> new RuntimeException("Didn't find a parameter annotated with @RequestBody!"));

		List<String> properties = PropertyUtils.findProperties(resolvableType);

		assertThat(properties).hasSize(2);
		assertThat(properties).contains("name", "role");
	}

	@Test
	public void objectWithIgnorableAttributes() {

		EmployeeWithCustomizedReaders employee = new EmployeeWithCustomizedReaders("Frodo", "Baggins", "ring bearer", "password", "fbaggins");

		Map<String, Object> properties = PropertyUtils.findProperties(employee);

		assertThat(properties).hasSize(6);
		assertThat(properties.keySet()).containsExactlyInAnyOrder("firstName", "lastName", "role", "username", "fullName", "usernameAndLastName");
		assertThat(properties.entrySet()).containsExactlyInAnyOrder(
			new SimpleEntry<>("firstName", "Frodo"),
			new SimpleEntry<>("lastName", "Baggins"),
			new SimpleEntry<>("role", "ring bearer"),
			new SimpleEntry<>("username", "fbaggins"),
			new SimpleEntry<>("fullName", "Frodo Baggins"),
			new SimpleEntry<>("usernameAndLastName", "fbaggins+++Baggins"));
	}

	@Data
	@AllArgsConstructor
	static class EmployeeWithCustomizedReaders {

		private String firstName;
		private String lastName;
		private String role;
		@JsonIgnore private String password;
		@JsonIgnore(false) private String username;

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

	@RestController
	static class TestController {
		
		@GetMapping("/")
		public Employee newEmployee(@RequestBody Resource<Employee> employee) {
			return employee.getContent();
		}
	}


}
