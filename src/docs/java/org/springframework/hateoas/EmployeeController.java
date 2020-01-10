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
package org.springframework.hateoas;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.hateoas.support.Employee;
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
public class EmployeeController {

	private static Map<Integer, Employee> EMPLOYEES;

	public static void reset() {

		EMPLOYEES = new TreeMap<>();

		EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
		EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
	}

	@GetMapping("/employees")
	public CollectionModel<EntityModel<Employee>> all() {

		Class<EmployeeController> controllerClass = EmployeeController.class;

		// Generate an "Affordance" based on this method (the "self" link)
		Link selfLink = linkTo(methodOn(controllerClass).all()).withSelfRel() // <1>
				.andAffordance(afford(methodOn(controllerClass).newEmployee(null))); // <2>

		// Return the collection of employee resources along with the composite affordance
		return IntStream.range(0, EMPLOYEES.size()) //
				.mapToObj(this::findOne) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), //
						it -> new CollectionModel<>(it, selfLink)));
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
		Link selfLink = linkTo(methodOn(EmployeeController.class).all()) //
				.withSelfRel() //
				.andAffordance(afford(methodOn(EmployeeController.class).newEmployee(null))) //
				.andAffordance(afford(methodOn(EmployeeController.class).search(null, null)));

		// Return the collection of employee resources along with the composite affordance
		return new CollectionModel<>(employees, selfLink);
	}

	// tag::get[]
	@GetMapping("/employees/{id}")
	public EntityModel<Employee> findOne(@PathVariable Integer id) {

		Class<EmployeeController> controllerClass = EmployeeController.class;

		// Start the affordance with the "self" link, i.e. this method.
		Link findOneLink = linkTo(methodOn(controllerClass).findOne(id)).withSelfRel(); // <1>

		// Return the affordance + a link back to the entire collection resource.
		return new EntityModel<>(EMPLOYEES.get(id), //
				findOneLink //
						.andAffordance(afford(methodOn(controllerClass).updateEmployee(null, id))) // <2>
						.andAffordance(afford(methodOn(controllerClass).partiallyUpdateEmployee(null, id)))); // <3>
	}
	// end::get[]

	@PostMapping("/employees")
	public ResponseEntity<?> newEmployee(@RequestBody EntityModel<Employee> employee) {

		int newEmployeeId = EMPLOYEES.size();
		EMPLOYEES.put(newEmployeeId, employee.getContent());

		Link link = linkTo(methodOn(getClass()).findOne(newEmployeeId)).withSelfRel().expand();

		return ResponseEntity.created(link.toUri()).build();
	}
	// end::new[]

	// tag::put[]
	@PutMapping("/employees/{id}")
	public ResponseEntity<?> updateEmployee( //
			@RequestBody EntityModel<Employee> employee, @PathVariable Integer id)
	// end::put[]
	{

		EMPLOYEES.put(id, employee.getContent());

		Link link = linkTo(methodOn(getClass()).findOne(id)).withSelfRel().expand();

		return ResponseEntity.noContent() //
				.location(link.toUri()) //
				.build();
	}

	// tag::patch[]
	@PatchMapping("/employees/{id}")
	public ResponseEntity<?> partiallyUpdateEmployee( //
			@RequestBody EntityModel<Employee> employee, @PathVariable Integer id)
	// end::patch[]
	{

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
				.location(findOne(id).getRequiredLink(IanaLinkRelations.SELF).toUri()) //
				.build();
	}
}
