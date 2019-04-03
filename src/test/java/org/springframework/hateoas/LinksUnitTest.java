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
package org.springframework.hateoas;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * Unit test for {@link Links}.
 * 
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class LinksUnitTest {

	static final String FIRST = "</something>;rel=\"foo\"";
	static final String SECOND = "</somethingElse>;rel=\"bar\"";
	static final String WITH_COMMA = "<http://localhost:8080/test?page=0&filter=foo,bar>;rel=\"foo\"";

	static final String LINKS = StringUtils.collectionToCommaDelimitedString(Arrays.asList(FIRST, SECOND));

	static final String THIRD = "</something>;rel=\"foo\";hreflang=\"en\"";
	static final String FOURTH = "</somethingElse>;rel=\"bar\";hreflang=\"de\"";

	static final String LINKS2 = StringUtils.collectionToCommaDelimitedString(Arrays.asList(THIRD, FOURTH));

	static final Links reference = new Links(new Link("/something", "foo"), new Link("/somethingElse", "bar"));
	static final Links reference2 = new Links(new Link("/something", "foo").withHreflang("en"), new Link("/somethingElse", "bar").withHreflang("de"));
	
	@Test
	public void parsesLinkHeaderLinks() {

		assertThat(Links.valueOf(LINKS), is(reference));
		assertThat(Links.valueOf(LINKS2), is(reference2));
		assertThat(reference.toString(), is(LINKS));
		assertThat(reference2.toString(), is(LINKS2));
	}

	@Test
	public void skipsEmptyLinkElements() {
		assertThat(Links.valueOf(LINKS + ",,,"), is(reference));
		assertThat(Links.valueOf(LINKS2 + ",,,"), is(reference2));
	}

	@Test
	public void returnsNullForNullOrEmptySource() {

		assertThat(Links.valueOf(null), is(Links.NO_LINKS));
		assertThat(Links.valueOf(""), is(Links.NO_LINKS));
	}

	/**
	 * @see #54
	 * @see #100
	 */
	@Test
	public void getSingleLinkByRel() {
		assertThat(reference.getLink("bar"), is(new Link("/somethingElse", "bar")));
		assertThat(reference2.getLink("bar"), is(new Link("/somethingElse", "bar").withHreflang("de")));
	}

	/**
	 * @see #440
	 */
	@Test
	public void parsesLinkWithComma() {

		Link withComma = new Link("http://localhost:8080/test?page=0&filter=foo,bar", "foo");

		assertThat(Links.valueOf(WITH_COMMA).getLink("foo"), is(withComma));

		Links twoWithCommaInFirst = Links.valueOf(WITH_COMMA.concat(",").concat(SECOND));

		assertThat(twoWithCommaInFirst.getLink("foo"), is(withComma));
		assertThat(twoWithCommaInFirst.getLink("bar"), is(new Link("/somethingElse", "bar")));
	}
}
