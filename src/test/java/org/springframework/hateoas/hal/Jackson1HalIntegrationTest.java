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
package org.springframework.hateoas.hal;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.AbstractMarshallingIntegrationTests;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

/**
 * Integration tests for Jackson 1 based HAL integration.
 * 
 * @author Alexander Baetz
 * @author Oliver Gierke
 */
public class Jackson1HalIntegrationTest extends AbstractMarshallingIntegrationTests {

	static final String SINGLE_LINK_REFERENCE = "{\"_links\":{\"self\":{\"rel\":\"self\",\"href\":\"localhost\"}}}";
	static final String LIST_LINK_REFERENCE = "{\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";
	static final String SINGLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"test\":{}},\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";
	static final String LIST_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"test\":[{},{}]},\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";

	@Before
	public void setUpModule() {
		mapper.registerModule(new Jackson1HalModule());
	}

	/**
	 * @see #29
	 */
	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport), is(SINGLE_LINK_REFERENCE));
	}

	/**
	 * @see #29
	 */
	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport), is(LIST_LINK_REFERENCE));
	}
}
