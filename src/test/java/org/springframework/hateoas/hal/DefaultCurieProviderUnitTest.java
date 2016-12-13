/*
 * Copyright 2013-2015 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.UriTemplate;

/**
 * Unit tests for {@link DefaultCurieProvider}.
 *
 * @author Oliver Gierke
 */
public class DefaultCurieProviderUnitTest {

	private static final UriTemplate URI_TEMPLATE = new UriTemplate("http://localhost:8080/rels/{rel}");

	CurieProvider provider = new DefaultCurieProvider("acme", URI_TEMPLATE);

	@Test(expected = IllegalArgumentException.class)
	public void preventsNullCurieName() {
		new DefaultCurieProvider(null, URI_TEMPLATE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void preventsEmptyCurieName() {
		new DefaultCurieProvider("", URI_TEMPLATE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void preventsUriTemplateWithoutVariable() {
		new DefaultCurieProvider("acme", new UriTemplate("http://localhost:8080/rels"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void preventsUriTemplateWithMoreThanOneVariable() {
		new DefaultCurieProvider("acme", new UriTemplate("http://localhost:8080/rels/{rel}/{another}"));
	}

	@Test
	public void doesNotPrefixIanaRels() {
		assertThat(provider.getNamespacedRelFrom(new Link("http://amazon.com")), is("self"));
	}

	@Test
	public void prefixesNormalRels() {
		assertThat(provider.getNamespacedRelFrom(new Link("http://amazon.com", "book")), is("acme:book"));
	}

	@Test
	public void doesNotPrefixQualifiedRels() {
		assertThat(provider.getNamespacedRelFrom(new Link("http://amazon.com", "custom:rel")), is("custom:rel"));
	}

	/**
	 * @see #229
	 */
	@Test
	public void doesNotPrefixIanaRelsForRelAsString() {
		assertThat(provider.getNamespacedRelFor("self"), is("self"));
	}

	/**
	 * @see #229
	 */
	@Test
	public void prefixesNormalRelsForRelAsString() {
		assertThat(provider.getNamespacedRelFor("book"), is("acme:book"));
	}

	/**
	 * @see #229
	 */
	@Test
	public void doesNotPrefixQualifiedRelsForRelAsString() {
		assertThat(provider.getNamespacedRelFor("custom:rel"), is("custom:rel"));
	}

	/**
	 * @see #363
	 */
	@Test
	public void configuresMultipleCuriesWithoutDefaultCorrectly() {

		DefaultCurieProvider provider = new DefaultCurieProvider(getCuries());

		assertThat(provider.getCurieInformation(new Links()), hasSize(2));
		assertThat(provider.getNamespacedRelFor("some"), is("some"));
	}

	/**
	 * @see #363
	 */
	@Test
	public void configuresMultipleCuriesWithDefaultCorrectly() {

		DefaultCurieProvider provider = new DefaultCurieProvider(getCuries(), "foo", null);

		assertThat(provider.getCurieInformation(new Links()), hasSize(2));
		assertThat(provider.getNamespacedRelFor("some"), is("foo:some"));
	}

	private static Map<String, UriTemplate> getCuries() {

		Map<String, UriTemplate> curies = new HashMap<String, UriTemplate>(2);
		curies.put("foo", new UriTemplate("/foo/{rel}"));
		curies.put("bar", new UriTemplate("/bar/{rel}"));

		return curies;
	}
}