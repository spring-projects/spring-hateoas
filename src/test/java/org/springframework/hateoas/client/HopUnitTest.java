/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.hateoas.client;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link Hop}.
 * 
 * @author Oliver Gierke
 * @soundtrack Dave Matthews Band - The Stone (Before These Crowded Streets)
 */
public class HopUnitTest {

	/**
	 * @see #346
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullRelationName() {
		Hop.rel(null);
	}

	/**
	 * @see #346
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsEmptyRelationName() {
		Hop.rel("");
	}

	/**
	 * @see #346
	 */
	@Test
	public void hasNoParametersByDefault() {
		assertThat(Hop.rel("rel").getParameters().entrySet(), is(empty()));
	}

	/**
	 * @see #346
	 */
	@Test
	public void addsParameterForSingluarWither() {

		Hop hop = Hop.rel("rel").withParameter("key", "value");

		assertThat(hop.getParameters(), hasEntry("key", (Object) "value"));
		assertThat(hop.getParameters().entrySet(), hasSize(1));
	}

	/**
	 * @see #346
	 */
	@Test
	public void replacesParametersForWither() {

		Hop hop = Hop.rel("rel").withParameter("key", "value")
				.withParameters(Collections.<String, Object> singletonMap("foo", "bar"));

		assertThat(hop.getParameters().entrySet(), hasSize(1));
		assertThat(hop.getParameters(), hasEntry("foo", (Object) "bar"));
	}

	/**
	 * @see #346
	 */
	@Test
	public void mergesGlobalParameters() {

		Hop hop = Hop.rel("rel").withParameter("key", "value");

		Map<String, Object> result = hop.getMergedParameters(Collections.<String, Object> singletonMap("foo", "bar"));

		assertThat(result.entrySet(), hasSize(2));
		assertThat(result, allOf(hasEntry("key", (Object) "value"), hasEntry("foo", (Object) "bar")));
	}

	/**
	 * @see #346
	 */
	@Test
	public void localParameterOverridesGlobalOnMerging() {

		Hop hop = Hop.rel("rel").withParameter("key", "value");

		Map<String, Object> result = hop.getMergedParameters(Collections.singletonMap("key", (Object) "global"));

		assertThat(result.entrySet(), hasSize(1));
		assertThat(result, hasEntry("key", (Object) "value"));
	}
}
