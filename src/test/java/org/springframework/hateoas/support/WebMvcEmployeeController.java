/*
 * Copyright 2019 the original author or authors.
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

import static org.springframework.hateoas.mvc.WebMvcLinkBuilder.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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
	public Resources<Resource<Employee>> all() {

		// Generate an "Affordance" based on this method (the "self" link)
		Link selfLink = linkTo(methodOn(WebMvcEmployeeController.class).all()).withSelfRel() //
				.andAffordance(afford(methodOn(WebMvcEmployeeController.class).newEmployee(null))) //
				.andAffordance(afford(methodOn(WebMvcEmployeeController.class).search(null, null)));

		// Return the collection of employee resources along with the composite affordance
		return IntStream.range(0, EMPLOYEES.size()) //
				.mapToObj(this::findOne) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), it -> new Resources<>(it, selfLink)));
	}

	@GetMapping("/employees/search")
	public Resources<Resource<Employee>> search(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "role", required = false) String role) {

		// Create a list of Resource<Employee>'s to return
		List<Resource<Employee>> employees = new ArrayList<>();

		// Fetch each Resource<Employee> using the controller's findOne method.
		for (int i = 0; i < EMPLOYEES.size(); i++) {

			Resource<Employee> employeeResource = findOne(i);

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
		Link selfLink = linkTo(methodOn(WebMvcEmployeeController.class).all()) //
				.withSelfRel() //
				.andAffordance(afford(methodOn(WebMvcEmployeeController.class).newEmployee(null))) //
				.andAffordance(afford(methodOn(WebMvcEmployeeController.class).search(null, null)));

		// Return the collection of employee resources along with the composite affordance
		return new Resources<>(employees, selfLink);
	}

	@GetMapping("/employees/{id}")
	public Resource<Employee> findOne(@PathVariable Integer id) {

		// Start the affordance with the "self" link, i.e. this method.
		Link findOneLink = linkTo(methodOn(WebMvcEmployeeController.class).findOne(id)).withSelfRel();

		// Define final link as means to find entire collection.
		Link employeesLink = linkTo(methodOn(WebMvcEmployeeController.class).all()).withRel("employees");

		// Return the affordance + a link back to the entire collection resource.
		return new Resource<>(EMPLOYEES.get(id), //
				findOneLink //
						.andAffordance(afford(methodOn(WebMvcEmployeeController.class).updateEmployee(null, id))) // //
						.andAffordance(afford(methodOn(WebMvcEmployeeController.class).partiallyUpdateEmployee(null, id))), //
				employeesLink);
	}

	@PostMapping("/employees")
	public ResponseEntity<?> newEmployee(@RequestBody Resource<Employee> employee) {

		int newEmployeeId = EMPLOYEES.size();

		EMPLOYEES.put(newEmployeeId, employee.getContent());

		Link link = linkTo(methodOn(getClass()).findOne(newEmployeeId)).withSelfRel().expand();

		return ResponseEntity.created(URI.create(link.getHref())).build();
	}

	@PutMapping("/employees/{id}")
	public ResponseEntity<?> updateEmployee(@RequestBody Resource<Employee> employee, @PathVariable Integer id) {

		EMPLOYEES.put(id, employee.getContent());

		Link link = linkTo(methodOn(getClass()).findOne(id)).withSelfRel().expand();

		return ResponseEntity.noContent() //
				.location(URI.create(link.getHref())) //
				.build();
	}

	@PatchMapping("/employees/{id}")
	public ResponseEntity<?> partiallyUpdateEmployee(@RequestBody Resource<Employee> employee, @PathVariable Integer id) {

		Employee oldEmployee = EMPLOYEES.get(id);
		Employee newEmployee = oldEmployee;

		if (employee.getContent().getName() != null) {
			newEmployee = newEmployee.withName(employee.getContent().getName());
		}

		if (employee.getContent().getRole() != null) {
			newEmployee = newEmployee.withRole(employee.getContent().getRole());
		}

		EMPLOYEES.put(id, newEmployee);

		try {
			return ResponseEntity //
					.noContent() //
					.location( //
							new URI(findOne(id) //
									.getLink(IanaLinkRelations.SELF) //
									.map(link -> link.expand().getHref()) //
									.orElse("") //
							) //
					).build();
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
}
