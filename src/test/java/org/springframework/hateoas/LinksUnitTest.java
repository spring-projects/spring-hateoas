/*
 * Copyright 2013-2014 the original author or authors.
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

import java.util.Arrays;

import org.junit.Test;
import org.springframework.util.StringUtils;

/**
 * Unit test for {@link Links}.
 * 
 * @author Oliver Gierke
 */
public class LinksUnitTest {

	static final String FIRST = "</something>;rel=\"foo\"";
	static final String SECOND = "</somethingElse>;rel=\"bar\"";

	static final String LINKS = StringUtils.collectionToCommaDelimitedString(Arrays.asList(FIRST, SECOND));

	static final Links reference = new Links(new Link("/something", "foo"), new Link("/somethingElse", "bar"));

	@Test
	public void parsesLinkHeaderLinks() {

		assertThat(Links.valueOf(LINKS), is(reference));
		assertThat(reference.toString(), is(LINKS));
	}

	@Test
	public void skipsEmptyLinkElements() {
		assertThat(Links.valueOf(LINKS + ",,,"), is(reference));
	}

	@Test
	public void returnsNullForNullOrEmptySource() {

		assertThat(Links.valueOf(null), is(Links.NO_LINKS));
		assertThat(Links.valueOf(""), is(Links.NO_LINKS));
	}

	@Test
	public void getSingleLinkByRel() {
		assertThat(reference.getLink("bar"), is(new Link("/somethingElse", "bar")));
	}
}
