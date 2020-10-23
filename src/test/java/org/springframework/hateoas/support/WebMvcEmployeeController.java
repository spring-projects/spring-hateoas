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
package org.springframework.hateoas.support;

import static org.springframework.hateoas.MediaTypes.*;
import static org.springframework.hateoas.mediatype.PropertyUtils.*;
import static org.springframework.hateoas.mediatype.alps.Alps.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Descriptor;
import org.springframework.hateoas.mediatype.alps.Ext;
import org.springframework.hateoas.mediatype.alps.Format;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Greg Turnquist
 */
@RestController
public class WebMvcEmployeeController {

	private static Map<Integer, Employee> EMPLOYEES;

	public static void reset() {

		EMPLOYEES = new TreeMap<>();

		EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
		EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
	}

	@GetMapping("/employees")
	public CollectionModel<EntityModel<Employee>> all() {

		// Generate an "Affordance" based on this method (the "self" link)
		WebMvcEmployeeController controller = methodOn(WebMvcEmployeeController.class);

		Link selfLink = linkTo(controller.all()).withSelfRel() //
				.andAffordance(afford(controller.newEmployee(null))) //
				.andAffordance(afford(controller.search(null, null)));

		// Return the collection of employee resources along with the composite affordance
		return IntStream.range(0, EMPLOYEES.size()) //
				.mapToObj(this::findOne) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), it -> CollectionModel.of(it, selfLink)));
	}

	@GetMapping("/employees/search")
	public CollectionModel<EntityModel<Employee>> search(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "role", required = false) String role) {

		// Create a list of Resource<Employee>'s to return
		List<EntityModel<Employee>> employees = new ArrayList<>();

		// Fetch each Resource<Employee> using the controller's findOne method.
		for (int i = 0; i < EMPLOYEES.size(); i++) {

			EntityModel<Employee> employeeResource = findOne(i);

			boolean nameMatches = Optional.ofNullable(name) //
					.map(s -> employeeResource.getContent().getName().contains(s)) //
					.orElse(true);

			boolean roleMatches = Optional.ofNullable(role) //
					.map(s -> employeeResource.getContent().getRole().contains(s)) //
					.orElse(true);

			if (nameMatches && roleMatches) {
				employees.add(employeeResource);
			}
		}

		// Generate an "Affordance" based on this method (the "self" link)
		WebMvcEmployeeController controller = methodOn(WebMvcEmployeeController.class);

		Link selfLink = linkTo(controller.all()) //
				.withSelfRel() //
				.andAffordance(afford(controller.newEmployee(null))) //
				.andAffordance(afford(controller.search(null, null)));

		// Return the collection of employee resources along with the composite affordance
		return CollectionModel.of(employees, selfLink);
	}

	@GetMapping("/employees/{id}")
	public EntityModel<Employee> findOne(@PathVariable Integer id) {

		// Start the affordance with the "self" link, i.e. this method.
		WebMvcEmployeeController controller = methodOn(WebMvcEmployeeController.class);

		Link findOneLink = linkTo(controller.findOne(id)).withSelfRel();

		// Define final link as means to find entire collection.
		Link employeesLink = linkTo(controller.all()).withRel("employees");

		// Return the affordance + a link back to the entire collection resource.
		return EntityModel.of(EMPLOYEES.get(id), //
				findOneLink //
						.andAffordance(afford(controller.updateEmployee(null, id))) // //
						.andAffordance(afford(controller.partiallyUpdateEmployee(null, id))), //
				employeesLink);
	}

	@PostMapping("/employees")
	public ResponseEntity<?> newEmployee(@RequestBody EntityModel<Employee> employee) {

		int newEmployeeId = EMPLOYEES.size();

		EMPLOYEES.put(newEmployeeId, employee.getContent());

		Link link = linkTo(methodOn(getClass()).findOne(newEmployeeId)).withSelfRel().expand();

		return ResponseEntity.created(link.toUri()).build();
	}

	@PutMapping("/employees/{id}")
	public ResponseEntity<?> updateEmployee(@RequestBody EntityModel<Employee> employee, @PathVariable Integer id) {

		EMPLOYEES.put(id, employee.getContent());

		Link link = linkTo(methodOn(getClass()).findOne(id)).withSelfRel().expand();

		return ResponseEntity.noContent() //
				.location(link.toUri()) //
				.build();
	}

	@PatchMapping("/employees/{id}")
	public ResponseEntity<?> partiallyUpdateEmployee(@RequestBody EntityModel<Employee> employee,
			@PathVariable Integer id) {

		Employee newEmployee = EMPLOYEES.get(id);

		if (employee.getContent().getName() != null) {
			newEmployee = newEmployee.withName(employee.getContent().getName());
		}

		if (employee.getContent().getRole() != null) {
			newEmployee = newEmployee.withRole(employee.getContent().getRole());
		}

		EMPLOYEES.put(id, newEmployee);

		return ResponseEntity //
				.noContent() //
				.location(findOne(id) //
						.getRequiredLink(IanaLinkRelations.SELF) //
						.toUri()) //
				.build();
	}

	// tag::alps-profile[]
	@GetMapping(value = "/profile", produces = ALPS_JSON_VALUE)
	Alps profile() {

		return Alps.alps() //
				.doc(doc() //
						.href("https://example.org/samples/full/doc.html") //
						.value("value goes here") //
						.format(Format.TEXT) //
						.build()) //
				.descriptor(getExposedProperties(Employee.class).stream() //
						.map(property -> Descriptor.builder() //
								.id("class field [" + property.getName() + "]") //
								.name(property.getName()) //
								.type(Type.SEMANTIC) //
								.ext(Ext.builder() //
										.id("ext [" + property.getName() + "]") //
										.href("https://example.org/samples/ext/" + property.getName()) //
										.value("value goes here") //
										.build()) //
								.rt("rt for [" + property.getName() + "]") //
								.descriptor(Collections.singletonList(Descriptor.builder().id("embedded").build())) //
								.build()) //
						.collect(Collectors.toList()))
				.build();
	}
	// end::alps-profile[]

	@GetMapping("/employees/problem")
	public ResponseEntity<?> problem() {

		return ResponseEntity.badRequest().body(Problem.create() //
				.withType(URI.create("http://example.com/problem")) //
				.withTitle("Employee-based problem") //
				.withStatus(HttpStatus.BAD_REQUEST) //
				.withDetail("This is a test case"));
	}
}
