/*
 * Copyright 2017-2024 the original author or authors.
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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.InputType;
import org.springframework.hateoas.mediatype.html.HtmlInputType;
import org.springframework.hateoas.server.core.MethodParameters;
import org.springframework.hateoas.support.Employee;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
		EntityModel<Employee> employeeResource = EntityModel.of(employee);

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

		assertThat(getProperty(metadata, "readWrite")) //
				.map(PropertyMetadata::isReadOnly) //
				.hasValue(false);

		assertThat(getProperty(metadata, "readOnly")) //
				.map(PropertyMetadata::isReadOnly) //
				.hasValue(true);
	}

	@Test
	void considersBasicJsr303Annotations() {

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(Jsr303SamplePayload.class);

		assertThat(getProperty(metadata, "nonNull")).hasValueSatisfying(it -> {
			assertThat(it.isRequired()).isTrue();
		});

		assertThat(getProperty(metadata, "pattern")).hasValueSatisfying(it -> {
			assertThat(it.getPattern()).hasValue("\\w");
		});

		assertThat(getProperty(metadata, "annotated")).hasValueSatisfying(it -> {
			assertThat(it.getPattern()).hasValue("regex");
		});
	}

	@Test // #1121
	void considersPropertyWithoutReader() throws Exception {

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(WithoutReaderMethod.class);

		assertThat(getProperty(metadata, "firstname")).isPresent();
	}

	@TestFactory
	Stream<DynamicTest> exposesInputTypeForProperties() {

		Stream<InputTypes> source = Stream.of( //
				InputTypes.of("firstname", HtmlInputType.TEXT), //
				InputTypes.of("comment", HtmlInputType.TEXTAREA), //
				InputTypes.of("email", HtmlInputType.EMAIL), //
				InputTypes.of("uri", HtmlInputType.URL), //
				InputTypes.of("url", HtmlInputType.URL), //
				InputTypes.of("stringUrl", HtmlInputType.URL), //
				InputTypes.of("email", HtmlInputType.EMAIL), //
				InputTypes.of("ranged", HtmlInputType.RANGE),
				InputTypes.of("sized", HtmlInputType.RANGE));

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(InputTypeSample.class);

		return DynamicTest.stream(source, InputTypes::toString, it -> it.verify(metadata));
	}

	@Test // #1563
	void considersJacksonRenamedProperty() {

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(JacksonCustomizations.class);

		assertThat(getProperty(metadata, "renamed")).isPresent();
	}

	@Test // #1402
	void detectesPropertiesWithRecordStyleAccessorsCorrectly() {

		assertThatNoException()
				.isThrownBy(() -> PropertyUtils.getExposedProperties(TypeWithRecordStyleAccessors.class));
	}

	@Test // #1753
	void considersJsr303NotBlankAnnotation() {

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(Jsr303SamplePayload.class);

		assertThat(getProperty(metadata, "nonBlank")).hasValueSatisfying(it -> {
			assertThat(it.isRequired()).isTrue();
			assertThat(it.getPattern()).hasValue(PropertyUtils.NOT_BLANK_REGEX);
		});

		assertThat(getProperty(metadata, "nonBlankPattern")).hasValueSatisfying(it -> {
			assertThat(it.isRequired()).isTrue();
			assertThat(it.getPattern()).hasValue("\\w");
		});
	}

	@Test // #1920
	void exposesMinAndMaxFromJsr303AtSizeAnnotation() {

		InputPayloadMetadata metadata = PropertyUtils.getExposedProperties(Jsr303SamplePayload.class);
		Optional<PropertyMetadata> property = metadata.stream().filter(it -> it.getName().equals("sized")).findFirst();

		assertThat(property).hasValueSatisfying(it -> {
			assertThat(it.getMin()).isEqualTo(41);
			assertThat(it.getMax()).isEqualTo(4711);
		});
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
		@NotBlank String nonBlank;
		@Pattern(regexp = "\\w") String pattern;
		@NotBlank @Pattern(regexp = "\\w") String nonBlankPattern;
		TypeAnnotated annotated;
		@Size(min = 41, max = 4711) int sized;
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

	@Data
	static class InputTypeSample {

		String firstname;

		@InputType(HtmlInputType.TEXTAREA_VALUE) String comment;

		@Email String email;

		URI uri;

		java.net.URL url;

		@URL String stringUrl;

		@Range int ranged;

		@Size int sized;
	}

	@Value
	static class JacksonCustomizations {
		@JsonProperty("renamed") String property;
	}

	// Test fixtures

	@Value(staticConstructor = "of")
	static class InputTypes {

		String property;
		HtmlInputType type;

		public void verify(InputPayloadMetadata metadata) {

			assertThat(PropertyUtilsTest.getProperty(metadata, property))
					.map(PropertyMetadata::getInputType)
					.hasValue(type.toString());
		}

		@Override
		public String toString() {
			return String.format("Expecting input type %s for %s.", type, property);
		}
	}

	private static Optional<PropertyMetadata> getProperty(PayloadMetadata metadata, String name) {
		return metadata.stream().filter(it -> it.hasName(name)).findFirst();
	}

	// #1402
	static class TypeWithRecordStyleAccessors {

		private Boolean isActive;

		public Boolean isActive() {
			return isActive;
		}

		public void setActive(Boolean active) {
			isActive = active;
		}
	}
}
