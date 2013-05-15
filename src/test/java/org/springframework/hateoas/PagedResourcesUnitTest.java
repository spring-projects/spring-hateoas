/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.hateoas;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.PagedResources.PageMetadata;

/**
 * Unit tests for {@link PagedResources}.
 * 
 * @author Oliver Gierke
 */
public class PagedResourcesUnitTest {

	static final PageMetadata metadata = new PagedResources.PageMetadata(10, 1, 200);

	PagedResources<Object> resources;

	@Before
	public void setUp() {
		resources = new PagedResources<Object>(Collections.emptyList(), metadata);
	}

	@Test
	public void discoversNextLink() {

		resources.add(new Link("foo", Link.REL_NEXT));

		assertThat(resources.getNextLink(), is(notNullValue()));
	}

	@Test
	public void discoversPreviousLink() {

		resources.add(new Link("custom", Link.REL_PREVIOUS));

		assertThat(resources.getPreviousLink(), is(notNullValue()));
	}
}
