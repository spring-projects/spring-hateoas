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
package org.springframework.hateoas.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.hateoas.RelProvider;

/**
 * Unit tests for {@link EvoInflectorRelProvider}.
 * 
 * @author Oliver Gierke
 */
public class EvoInflectorRelProviderUnitTest {

	RelProvider provider = new EvoInflectorRelProvider();

	@Test
	public void buildsCollectionRelCorrectly() {
		assertRels(City.class, "city", "cities");
		assertRels(Person.class, "person", "persons");
	}

	private void assertRels(Class<?> type, String singleRel, String collectionRel) {
		assertThat(provider.getSingleResourceRelFor(type), is(singleRel));
		assertThat(provider.getCollectionResourceRelFor(type), is(collectionRel));
	}

	static class Person {

	}

	static class City {

	}
}
