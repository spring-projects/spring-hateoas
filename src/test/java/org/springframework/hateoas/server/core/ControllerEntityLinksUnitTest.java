/*
 * Copyright 2012-2021 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import lombok.Value;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.server.EntityLinks;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.LinkBuilderFactory;
import org.springframework.hateoas.server.TypedEntityLinks;
import org.springframework.hateoas.server.TypedEntityLinks.ExtendedTypedEntityLinks;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link ControllerEntityLinks}.
 *
 * @author Oliver Gierke
 */
@ExtendWith(MockitoExtension.class)
class ControllerEntityLinksUnitTest extends TestUtils {

	@Mock LinkBuilderFactory<LinkBuilder> linkBuilderFactory;

	@Test
	void rejectsUnannotatedController() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new ControllerEntityLinks(singletonList(InvalidController.class), linkBuilderFactory)) //
				.withMessageContaining(InvalidController.class.getName());
	}

	@Test
	void rejectsNullControllerList() {
		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new ControllerEntityLinks(null, linkBuilderFactory));
	}

	@Test
	void rejectsNullLinkBuilderFactory() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new ControllerEntityLinks(singletonList(SampleController.class), null));
	}

	@Test
	void registersControllerForEntity() {

		when(linkBuilderFactory.linkTo(SampleController.class, new Object[0])).thenReturn(linkTo(SampleController.class));
		EntityLinks links = new ControllerEntityLinks(singletonList(SampleController.class), linkBuilderFactory);

		assertThat(links.supports(Person.class)).isTrue();
		assertThat(links.linkFor(Person.class)).isNotNull();
	}

	@Test // #43
	void returnsLinkBuilderForParameterizedController() {

		when(linkBuilderFactory.linkTo(eq(ControllerWithParameters.class), (Object[]) any())) //
				.thenReturn(linkTo(ControllerWithParameters.class, "1"));

		ControllerEntityLinks links = new ControllerEntityLinks(singletonList(ControllerWithParameters.class),
				linkBuilderFactory);
		LinkBuilder builder = links.linkFor(Order.class, "1");

		assertThat(builder.withSelfRel().getHref()).endsWith("/person/1");
	}

	@Test
	void rejectsUnmanagedEntity() {

		EntityLinks links = new ControllerEntityLinks(Arrays.asList(SampleController.class, ControllerWithParameters.class),
				linkBuilderFactory);

		assertThat(links.supports(Person.class)).isTrue();
		assertThat(links.supports(Order.class)).isTrue();
		assertThat(links.supports(SampleController.class)).isFalse();

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> links.linkFor(SampleController.class)) //
				.withMessageContaining(SampleController.class.getName()) //
				.withMessageContaining(ExposesResourceFor.class.getName());
	}

	@Test // #843
	void returnsItemResourceLinkForExtractorFunction() {

		when(linkBuilderFactory.linkTo(SampleController.class, new Object[0])).thenReturn(linkTo(SampleController.class));

		ControllerEntityLinks entityLinks = new ControllerEntityLinks(singleton(SampleController.class),
				linkBuilderFactory);

		assertThat(entityLinks.linkToItemResource(new Person(42L), Person::getId).getHref()).endsWith("/person/42");
	}

	@Test // #843
	void returnsTypeEntityLinks() {

		when(linkBuilderFactory.linkTo(SampleController.class, new Object[0])).thenReturn(linkTo(SampleController.class));

		TypedEntityLinks<Person> entityLinks = new ControllerEntityLinks(singleton(SampleController.class),
				linkBuilderFactory).forType(Person::getId);

		Person person = new Person(42L);

		assertThat(entityLinks.linkForItemResource(person).withSelfRel().getHref()).endsWith("/person/42");
		assertThat(entityLinks.linkToItemResource(person).getHref()).endsWith("/person/42");
	}

	@Test // #843
	void returnsExtendedTypedEntityLinks() {

		when(linkBuilderFactory.linkTo(SampleController.class, new Object[0])).thenReturn(linkTo(SampleController.class));

		ExtendedTypedEntityLinks<Person> entityLinks = new ControllerEntityLinks(singleton(SampleController.class),
				linkBuilderFactory).forType(Person.class, Person::getId);

		assertThat(entityLinks.linkToCollectionResource().getHref()).endsWith("/person");
	}

	@Controller
	@ExposesResourceFor(Person.class)
	@RequestMapping("/person")
	static class SampleController {}

	@Controller
	@ExposesResourceFor(Order.class)
	@RequestMapping("/person/{id}")
	static class ControllerWithParameters {}

	static class InvalidController {}

	@Value
	static class Person {
		Long id;
	}

	static class Order {}
}
