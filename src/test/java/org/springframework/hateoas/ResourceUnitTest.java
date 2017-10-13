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

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import org.junit.Test;

/**
 * Unit tests for {@link Resource}.
 * 
 * @author Oliver Gierke
 */
public class ResourceUnitTest {

	@Test
	public void equalsForSelfReference() {

		Resource<String> resource = new Resource<String>("foo");
		assertThat(resource).isEqualTo(resource);
	}

	@Test
	public void equalsWithEqualContent() {

		Resource<String> left = new Resource<String>("foo");
		Resource<String> right = new Resource<String>("foo");

		assertThat(left).isEqualTo(right);
		assertThat(right).isEqualTo(left);
	}

	@Test
	public void notEqualForDifferentContent() {

		Resource<String> left = new Resource<String>("foo");
		Resource<String> right = new Resource<String>("bar");

		assertThat(left).isNotEqualTo(right);
		assertThat(right).isNotEqualTo(left);
	}

	@Test
	public void notEqualForDifferentLinks() {

		Resource<String> left = new Resource<String>("foo");
		Resource<String> right = new Resource<String>("foo");
		right.add(new Link("localhost"));

		assertThat(left).isNotEqualTo(right);
		assertThat(right).isNotEqualTo(left);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsCollectionContent() {
		new Resource<Object>(Collections.emptyList());
	}
}
