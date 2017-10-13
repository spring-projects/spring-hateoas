/*
 * Copyright 2012-2013 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.LinkBuilderFactory;
import org.springframework.hateoas.TestUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link ControllerEntityLinks}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class ControllerEntityLinksUnitTest extends TestUtils {

	@Mock LinkBuilderFactory<LinkBuilder> linkBuilderFactory;

	@Test
	public void rejectsUnannotatedController() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new ControllerEntityLinks(Arrays.asList(InvalidController.class), linkBuilderFactory)) //
				.withMessageContaining(InvalidController.class.getName());
	}

	@Test
	public void rejectsNullControllerList() {
		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new ControllerEntityLinks(null, linkBuilderFactory));
	}

	@Test
	public void rejectsNullLinkBuilderFactory() {

		assertThatExceptionOfType(IllegalArgumentException.class) //
				.isThrownBy(() -> new ControllerEntityLinks(Arrays.asList(SampleController.class), null));
	}

	@Test
	public void registersControllerForEntity() {

		when(linkBuilderFactory.linkTo(SampleController.class, new Object[0])).thenReturn(linkTo(SampleController.class));
		EntityLinks links = new ControllerEntityLinks(Arrays.asList(SampleController.class), linkBuilderFactory);

		assertThat(links.supports(Person.class)).isTrue();
		assertThat(links.linkFor(Person.class)).isNotNull();
	}

	/**
	 * @see #43
	 */
	@Test
	public void returnsLinkBuilderForParameterizedController() {

		when(linkBuilderFactory.linkTo(eq(ControllerWithParameters.class), (Object[]) any())) //
				.thenReturn(linkTo(ControllerWithParameters.class, "1"));

		ControllerEntityLinks links = new ControllerEntityLinks(Arrays.asList(ControllerWithParameters.class),
				linkBuilderFactory);
		LinkBuilder builder = links.linkFor(Order.class, "1");

		assertThat(builder.withSelfRel().getHref()).endsWith("/person/1");
	}

	@Test
	public void rejectsUnmanagedEntity() {

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

	@Controller
	@ExposesResourceFor(Person.class)
	@RequestMapping("/person")
	static class SampleController {}

	@Controller
	@ExposesResourceFor(Order.class)
	@RequestMapping("/person/{id}")
	static class ControllerWithParameters {}

	static class InvalidController {}

	static class Person {}

	static class Order {}
}
