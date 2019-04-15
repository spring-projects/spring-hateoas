/*
 * Copyright 2013-2017 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider.Curie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Unit tests for {@link DefaultCurieProvider}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class DefaultCurieProviderUnitTest {

	private static final UriTemplate URI_TEMPLATE = UriTemplate.of("http://localhost:8080/rels/{rel}");

	CurieProvider provider = new DefaultCurieProvider("acme", URI_TEMPLATE);

	@SuppressWarnings("null")
	@Test
	void preventsNullCurieName() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new DefaultCurieProvider(null, URI_TEMPLATE);
		});
	}

	@Test
	void preventsEmptyCurieName() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new DefaultCurieProvider("", URI_TEMPLATE);
		});
	}

	@SuppressWarnings("null")
	@Test
	void preventsNullUriTemplateName() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new DefaultCurieProvider("acme", null);
		});
	}

	@Test
	void preventsUriTemplateWithoutVariable() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new DefaultCurieProvider("acme", UriTemplate.of("http://localhost:8080/rels"));
		});
	}

	@Test
	void preventsUriTemplateWithMoreThanOneVariable() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new DefaultCurieProvider("acme", UriTemplate.of("http://localhost:8080/rels/{rel}/{another}"));
		});
	}

	@Test
	void doesNotPrefixIanaRels() {

		assertThat(provider.getNamespacedRelFrom(new Link("https://amazon.com"))) //
				.isEqualTo(HalLinkRelation.of(IanaLinkRelations.SELF));
	}

	@Test
	void prefixesNormalRels() {

		assertThat(provider.getNamespacedRelFrom(new Link("https://amazon.com", "book"))) //
				.isEqualTo(HalLinkRelation.curied("acme", "book"));
	}

	@Test
	void doesNotPrefixQualifiedRels() {

		assertThat(provider.getNamespacedRelFrom(new Link("https://amazon.com", "custom:rel")))
				.isEqualTo(HalLinkRelation.curied("custom", "rel"));
	}

	/**
	 * @see #100
	 */
	@Test
	void prefixesNormalRelsThatHaveExtraRFC5988Attributes() {

		Link link = new Link("https://amazon.com", "custom:rel") //
				.withHreflang("en") //
				.withTitle("the title") //
				.withMedia("the media") //
				.withType("the type") //
				.withDeprecation("https://example.com/custom/deprecated");

		assertThat(provider.getNamespacedRelFrom(link)) //
				.isEqualTo(HalLinkRelation.curied("custom", "rel"));
	}

	/**
	 * @see #229
	 */
	@Test
	void doesNotPrefixIanaRelsForRelAsString() {

		assertThat(provider.getNamespacedRelFor(IanaLinkRelations.SELF)) //
				.isEqualTo(HalLinkRelation.uncuried("self"));
	}

	/**
	 * @see #229
	 */
	@Test
	void prefixesNormalRelsForRelAsString() {

		assertThat(provider.getNamespacedRelFor(LinkRelation.of("book"))) //
				.isEqualTo(HalLinkRelation.curied("acme", "book"));
	}

	/**
	 * @see #229
	 */
	@Test
	void doesNotPrefixQualifiedRelsForRelAsString() {

		assertThat(provider.getNamespacedRelFor(HalLinkRelation.curied("custom", "rel")))
				.isEqualTo(HalLinkRelation.curied("custom", "rel"));
	}

	/**
	 * @see #363
	 */
	@Test
	void configuresMultipleCuriesWithoutDefaultCorrectly() {

		DefaultCurieProvider provider = new DefaultCurieProvider(getCuries());

		assertThat(provider.getCurieInformation(Links.of())).hasSize(2);
		assertThat(provider.getNamespacedRelFor(LinkRelation.of("some"))).isEqualTo(HalLinkRelation.uncuried("some"));
	}

	/**
	 * @see #363
	 */
	@Test
	void configuresMultipleCuriesWithDefaultCorrectly() {

		DefaultCurieProvider provider = new DefaultCurieProvider(getCuries(), "foo");

		assertThat(provider.getCurieInformation(Links.of())).hasSize(2);
		assertThat(provider.getNamespacedRelFor(LinkRelation.of("some"))) //
				.isEqualTo(HalLinkRelation.curied("foo", "some"));
	}

	/**
	 * #421
	 */
	@Test
	void expandsNonAbsoluteUriWithApplicationUri() {

		DefaultCurieProvider provider = new DefaultCurieProvider("name", UriTemplate.of("/docs/{rel}"));

		MockHttpServletRequest request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);

		Links links = Links.of(new Link("http://localhost", "name:foo"));

		Collection<? extends Object> curies = provider.getCurieInformation(links);
		assertThat(curies).hasSize(1);

		Object curie = curies.iterator().next();
		assertThat(curie).isInstanceOfSatisfying(Curie.class,
				it -> assertThat(it.getHref()).startsWith("http://localhost"));
	}

	private static Map<String, UriTemplate> getCuries() {

		Map<String, UriTemplate> curies = new HashMap<>(2);
		curies.put("foo", UriTemplate.of("/foo/{rel}"));
		curies.put("bar", UriTemplate.of("/bar/{rel}"));

		return curies;
	}
}
