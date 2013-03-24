/*
 * Copyright 2012 the original author or authors.
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
package org.springframework.hateoas.config;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import javax.ws.rs.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.core.DelegatingEntityLinks;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Integration test for {@link EnableEntityLinks} annotation.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class EnableEntityLinksIntegrationTest {

	@Configuration
	@EnableEntityLinks
	static class Config {

		@Bean
		public SampleController controller() {
			return new SampleController();
		}

		@Bean
		public SampleResource resource() {
			return new SampleResource();
		}
	}

	@Autowired
	DelegatingEntityLinks builder;

	@Test
	public void initializesDelegatingEntityLinks() {

		assertThat(builder, is(notNullValue()));
		assertThat(builder.supports(Person.class), is(true));
		assertThat(builder.supports(Address.class), is(true));
		assertThat(builder.supports(Object.class), is(false));
	}

	@Controller
	@ExposesResourceFor(Person.class)
	@RequestMapping("/person")
	static class SampleController {

	}

	@Path("/address")
	@ExposesResourceFor(Address.class)
	static class SampleResource {

	}

	static class Person {

	}

	static class Address {

	}

}
