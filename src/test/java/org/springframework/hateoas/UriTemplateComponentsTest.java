/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.springframework.hateoas;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.hateoas.UriTemplateComponents;

public class UriTemplateComponentsTest {

	@Test
	public void testToStringWithQueryContinuation() throws Exception {
		UriTemplateComponents partialUriTemplateComponents = new UriTemplateComponents("http://example.com",
				"?foo=bar", "baz,gnarf", "#fragmentIdentifier", Arrays.asList("baz", "gnarf"));
		Assert.assertEquals("http://example.com?foo=bar{&baz,gnarf}#fragmentIdentifier",
				partialUriTemplateComponents.toString());
	}

	@Test
	public void testToStringWithQueryVariables() throws Exception {
		UriTemplateComponents partialUriTemplateComponents = new UriTemplateComponents("http://example.com",
				"", "baz,gnarf", "#fragmentIdentifier", Arrays.asList("baz", "gnarf"));
		Assert.assertEquals("http://example.com{?baz,gnarf}#fragmentIdentifier", partialUriTemplateComponents.toString());
	}
}
