/*
 * Copyright 2019 the original author or authors.
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

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.hateoas.reactive.ReactiveLinkBuilder.linkTo;
import static reactor.function.TupleUtils.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.reactive.ReactiveLinkBuilder;
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
public class WebFluxEmployeeController {

	private static Map<Integer, Employee> EMPLOYEES;

	public static void reset() {

		EMPLOYEES = new TreeMap<>();

		EMPLOYEES.put(0, new Employee("Frodo Baggins", "ring bearer"));
		EMPLOYEES.put(1, new Employee("Bilbo Baggins", "burglar"));
	}

	@GetMapping("/employees")
	public Mono<Resources<Resource<Employee>>> all() {

		return Flux.fromIterable(EMPLOYEES.keySet()).flatMap(id -> findOne(id)).collectList().flatMap(
				resources -> linkTo(methodOn(WebFluxEmployeeController.class).all()).map(ReactiveLinkBuilder::withSelfRel)
						.map(link -> link.andAffordance(afford(methodOn(WebFluxEmployeeController.class).newEmployee(null)))
								.andAffordance(afford(methodOn(WebFluxEmployeeController.class).search(null, null))))
						.map(selfLink -> new Resources<>(resources, selfLink)));
	}

	@GetMapping("/employees/search")
	public Mono<Resources<Resource<Employee>>> search(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "role", required = false) String role) {

		return Flux.fromIterable(EMPLOYEES.keySet()).flatMap(id -> findOne(id)).filter(resource -> {

			boolean nameMatches = Optional.ofNullable(name).map(s -> resource.getContent().getName().contains(s))
					.orElse(true);

			boolean roleMatches = Optional.ofNullable(role).map(s -> resource.getContent().getRole().contains(s))
					.orElse(true);

			return nameMatches && roleMatches;
		}).collectList().flatMap(
				resources -> linkTo(methodOn(WebFluxEmployeeController.class).all()).map(ReactiveLinkBuilder::withSelfRel)
						.map(link -> link.andAffordance(afford(methodOn(WebFluxEmployeeController.class).newEmployee(null)))
								.andAffordance(afford(methodOn(WebFluxEmployeeController.class).search(null, null))))
						.map(selfLink -> new Resources<>(resources, selfLink)));
	}

	@GetMapping("/employees/{id}")
	public Mono<Resource<Employee>> findOne(@PathVariable Integer id) {

		return linkTo(methodOn(WebFluxEmployeeController.class).findOne(id)) //
				.map(ReactiveLinkBuilder::withSelfRel) //
				.zipWith(linkTo(methodOn(WebFluxEmployeeController.class).all()) //
						.map(reactiveLinkBuilder -> reactiveLinkBuilder.withRel("employees")))
				.map(function((selfLink, employeesLink) -> new Resource<>(EMPLOYEES.get(id),
						selfLink.andAffordance(afford(methodOn(WebFluxEmployeeController.class).updateEmployee(null, id))) //
								.andAffordance(afford(methodOn(WebFluxEmployeeController.class).partiallyUpdateEmployee(null, id))),
						employeesLink)));
	}

	@PostMapping("/employees")
	public Mono<ResponseEntity<?>> newEmployee(@RequestBody Mono<Resource<Employee>> employee) {

		return employee.flatMap(resource -> {

			int newEmployeeId = EMPLOYEES.size();
			EMPLOYEES.put(newEmployeeId, resource.getContent());
			return findOne(newEmployeeId);

		}).map(findOne -> {

			return ResponseEntity.created(URI.create(findOne //
					.getLink(IanaLinkRelations.SELF) //
					.map(link -> link.expand().getHref()) //
					.orElse(""))) //
					.build();
		});
	}

	@PutMapping("/employees/{id}")
	public Mono<ResponseEntity<?>> updateEmployee(@RequestBody Mono<Resource<Employee>> employee,
			@PathVariable Integer id) {

		return employee.flatMap(resource -> {
			EMPLOYEES.put(id, resource.getContent());
			return findOne(id);
		}).map(findOne -> {

			return ResponseEntity.noContent() //
					.location(URI.create(findOne //
							.getLink(IanaLinkRelations.SELF) //
							.map(link -> link.expand().getHref()) //
							.orElse(""))) //
					.build();
		});
	}

	@PatchMapping("/employees/{id}")
	public Mono<ResponseEntity<?>> partiallyUpdateEmployee( //
			@RequestBody Mono<Resource<Employee>> employee, @PathVariable Integer id) {

		return employee.flatMap(resource -> {

			Employee oldEmployee = EMPLOYEES.get(id);
			Employee newEmployee = oldEmployee;

			if (resource.getContent().getName() != null) {
				newEmployee = newEmployee.withName(resource.getContent().getName());
			}

			if (resource.getContent().getRole() != null) {
				newEmployee = newEmployee.withRole(resource.getContent().getRole());
			}

			EMPLOYEES.put(id, newEmployee);

			return findOne(id);

		}).map(findOne -> {

			return ResponseEntity.noContent() //
					.location(URI.create(findOne //
							.getLink(IanaLinkRelations.SELF) //
							.map(link -> link.expand().getHref()) //
							.orElse(""))) //
					.build();
		});
	}
}
