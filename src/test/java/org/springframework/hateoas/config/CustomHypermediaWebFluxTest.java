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
package org.springframework.hateoas.config;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.*;
import static org.springframework.hateoas.support.CustomHypermediaType.*;
import static org.springframework.hateoas.support.MappingUtils.*;

import reactor.core.publisher.Mono;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.CustomHypermediaType;
import org.springframework.hateoas.support.Employee;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Greg Turnquist
 */
class CustomHypermediaWebFluxTest {

	WebTestClient testClient;

	@BeforeEach
	void setUp() {

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(TestConfig.class);
		ctx.refresh();

		WebClientConfigurer webClientConfigurer = ctx.getBean(WebClientConfigurer.class);

		this.testClient = WebTestClient.bindToApplicationContext(ctx).build() //
				.mutate() //
				.exchangeStrategies(it -> it.codecs(webClientConfigurer.configurer)) //
				.build();
	}

	@Test // #833
	void getUsingCustomMediaType() throws IOException {

		this.testClient.get().uri("http://localhost/employees/1") //
				.accept(FRODO_MEDIATYPE) //
				.exchange() //
				.expectStatus().isOk() //
				.expectHeader().contentType(FRODO_MEDIATYPE.toString()) //
				.expectBody(String.class) //
				.isEqualTo(read(new ClassPathResource("webflux-frodo.json", getClass())));
	}

	@Configuration
	@EnableWebFlux
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class TestConfig {

		@Bean
		CustomHypermediaType customHypermediaType() {
			return new CustomHypermediaType();
		}

		@Bean
		EmployeeController employeeController() {
			return new EmployeeController();
		}
	}

	@RestController
	static class EmployeeController {

		@GetMapping("/employees/1")
		public Mono<EntityModel<Employee>> findOne() {

			return linkTo(methodOn(EmployeeController.class).findOne()).withSelfRel() //
					.toMono() //
					.map(link -> new EntityModel<>(new Employee("Frodo Baggins", "ring bearer"), link)); //
		}
	}
}
