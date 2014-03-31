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
package org.springframework.hateoas;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Integration tests for {@link ResourceSupport}.
 * 
 * @author Oliver Gierke
 */
public class ResourceSupportIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final String REFERENCE = "{\"links\":[{\"rel\":\"self\",\"href\":\"localhost\"}]}";

	/**
	 * @see #7
	 */
	@Test
	public void doesNotRenderId() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport), is(REFERENCE));
	}

	/**
	 * @see #14
	 */
	@Test
	public void readResourceSupportCorrectly() throws Exception {

		ResourceSupport result = read(REFERENCE, ResourceSupport.class);

		assertThat(result.getLinks(), hasSize(1));
		assertThat(result.getLinks(), hasItem(new Link("localhost")));
	}
}
