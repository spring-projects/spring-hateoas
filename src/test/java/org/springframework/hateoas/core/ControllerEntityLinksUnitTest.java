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
package org.springframework.hateoas.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.LinkBuilderFactory;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Unit tests for {@link ControllerEntityLinks}.
 * 
 * @author Oliver Gierke
 */
@RunWith(MockitoJUnitRunner.class)
public class ControllerEntityLinksUnitTest extends TestUtils {

	@Mock
	LinkBuilderFactory<LinkBuilder> linkBuilderFactory;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	@SuppressWarnings("unchecked")
	public void rejectsUnannotatedController() {

		thrown.expectMessage(InvalidController.class.getName());
		new ControllerEntityLinks(Arrays.asList(InvalidController.class), linkBuilderFactory);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullControllerList() {

		new ControllerEntityLinks(null, linkBuilderFactory);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void rejectsNullLinkBuilderFactory() {

		thrown.expectMessage(InvalidController.class.getName());
		new ControllerEntityLinks(Arrays.asList(InvalidController.class), linkBuilderFactory);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void registersControllerForEntity() {

		when(linkBuilderFactory.linkTo(SampleController.class)).thenReturn(
				ControllerLinkBuilder.linkTo(SampleController.class));
		EntityLinks links = new ControllerEntityLinks(Arrays.asList(SampleController.class), linkBuilderFactory);

		assertThat(links.supports(Person.class), is(true));
		assertThat(links.linkFor(Person.class), is(notNullValue()));
	}

	@Controller
	@ExposesResourceFor(Person.class)
	@RequestMapping("/person")
	static class SampleController {

	}

	static class InvalidController {

	}

	static class Person {

	}
}
