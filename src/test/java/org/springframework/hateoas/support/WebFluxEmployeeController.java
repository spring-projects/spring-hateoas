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
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;
import static reactor.function.TupleUtils.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.mediatype.alps.Alps;
import org.springframework.hateoas.mediatype.alps.Descriptor;
import org.springframework.hateoas.mediatype.alps.Ext;
import org.springframework.hateoas.mediatype.alps.Format;
import org.springframework.hateoas.mediatype.alps.Type;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
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
 * Sample controller using {@link WebFluxLinkBuilder} to create {@link Affordance}s.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
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
	public Mono<CollectionModel<EntityModel<Employee>>> all() {

		WebFluxEmployeeController controller = methodOn(WebFluxEmployeeController.class);

		return Flux.fromIterable(EMPLOYEES.keySet()) //
				.flatMap(this::findOne) //
				.collectList() //
				.flatMap(resources -> linkTo(controller.all()).withSelfRel() //
						.andAffordance(controller.newEmployee(null)) //
						.andAffordance(controller.search(null, null)) //
						.toMono() //
						.map(selfLink -> new CollectionModel<>(resources, selfLink)));
	}

	@GetMapping("/employees/search")
	public Mono<CollectionModel<EntityModel<Employee>>> search( //
			@RequestParam Optional<String> name, //
			@RequestParam Optional<String> role) {

		WebFluxEmployeeController controller = methodOn(WebFluxEmployeeController.class);

		return Flux.fromIterable(EMPLOYEES.keySet()) //
				.flatMap(this::findOne) //
				.filter(resource -> {

					boolean nameMatches = name //
							.map(s -> resource.getContent().getName().contains(s)) //
							.orElse(true);

					boolean roleMatches = name.map(s -> resource.getContent().getRole().contains(s)) //
							.orElse(true);

					return nameMatches && roleMatches;
				}).collectList().flatMap(resources -> linkTo(controller.all()) //
						.withSelfRel() //
						.andAffordance(controller.newEmployee(null)) //
						.andAffordance(controller.search(null, null)) //
						.toMono() //
						.map(selfLink -> new CollectionModel<>(resources, selfLink)));
	}

	@GetMapping("/employees/{id}")
	public Mono<EntityModel<Employee>> findOne(@PathVariable Integer id) {

		WebFluxEmployeeController controller = methodOn(WebFluxEmployeeController.class);

		Mono<Link> selfLink = linkTo(controller.findOne(id)).withSelfRel() //
				.andAffordance(controller.updateEmployee(null, id)) //
				.andAffordance(controller.partiallyUpdateEmployee(null, id)) //
				.toMono();

		Mono<Link> employeesLink = linkTo(controller.all()).withRel("employees") //
				.toMono();

		return selfLink.zipWith(employeesLink) //
				.map(function((left, right) -> Links.of(left, right))) //
				.map(links -> new EntityModel<>(EMPLOYEES.get(id), links));
	}

	@PostMapping("/employees")
	public Mono<ResponseEntity<?>> newEmployee(@RequestBody Mono<EntityModel<Employee>> employee) {

		return employee //
				.flatMap(resource -> {

					int newEmployeeId = EMPLOYEES.size();
					EMPLOYEES.put(newEmployeeId, resource.getContent());
					return findOne(newEmployeeId);
				}) //
				.map(findOne -> ResponseEntity.created(findOne //
						.getRequiredLink(IanaLinkRelations.SELF) //
						.toUri()) //
						.build());
	}

	@PutMapping("/employees/{id}")
	public Mono<ResponseEntity<?>> updateEmployee(@RequestBody Mono<EntityModel<Employee>> employee,
			@PathVariable Integer id) {

		return employee.flatMap(resource -> {

			EMPLOYEES.put(id, resource.getContent());
			return findOne(id);
		}).map(findOne -> ResponseEntity.noContent() //
				.location(findOne.getRequiredLink(IanaLinkRelations.SELF).toUri()).build());
	}

	@PatchMapping("/employees/{id}")
	public Mono<ResponseEntity<?>> partiallyUpdateEmployee( //
			@RequestBody Mono<EntityModel<Employee>> employee, @PathVariable Integer id) {

		return employee //
				.flatMap(resource -> {

					Employee newEmployee = EMPLOYEES.get(id);

					if (resource.getContent().getName() != null) {
						newEmployee = newEmployee.withName(resource.getContent().getName());
					}

					if (resource.getContent().getRole() != null) {
						newEmployee = newEmployee.withRole(resource.getContent().getRole());
					}

					EMPLOYEES.put(id, newEmployee);

					return findOne(id);

				}).map(findOne -> ResponseEntity.noContent() //
						.location(findOne.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
						.build() //
				);
	}

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
}
