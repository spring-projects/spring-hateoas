/*
 * Copyright 2017-2019 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.mvc.WebMvcLinkBuilder.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Test cases for {@link ControllerLinkBuilder} that are NOT inside an existing Spring MVC request
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public class WebMvcLinkBuilderOutsideSpringMvcUnitTest {

	/**
	 * Clear out any existing request attributes left behind by other tests
	 */
	@Before
	public void setUp() {
		RequestContextHolder.setRequestAttributes(null);
	}

	/**
	 * @see #408
	 */
	@Test
	public void requestingLinkOutsideWebRequest() {

		Link link = linkTo(
				methodOn(WebMvcLinkBuilderUnitTest.PersonsAddressesController.class, 15).getAddressesForCountry("DE"))
						.withSelfRel();

		assertThat(link).isEqualTo(new Link("/people/15/addresses/DE").withSelfRel());
	}
}
