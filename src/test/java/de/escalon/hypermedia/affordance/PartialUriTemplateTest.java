/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.affordance;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class PartialUriTemplateTest {


	@Test
	public void testToStringWithQueryVariablesContainingDot() throws Exception {
		PartialUriTemplate partialUriTemplateComponents = new PartialUriTemplate
				("http://localhost/events/query{?foo1,foo2,bar.baz,bars.empty,offset,size,strings.empty}");
		assertThat(partialUriTemplateComponents.getVariableNames(), contains("foo1", "foo2", "bar.baz",
				"bars.empty", "offset", "size", "strings.empty"));
	}

	@Test
	public void testExpandAllComponents() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events{/city}{?eventName," +
				"location}{#section}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description",
				expanded.toString());
	}

	@Test
	public void testExpandQueryWithTwoVariables() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events/Wiesbaden{?eventName," +
				"location}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof",
				expanded.toString());
	}

	@Test
	public void testExpandQueryWithOneVariable() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events/Wiesbaden{?eventName}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour",
				expanded.toString());
	}

	@Test
	public void testExpandLevelOnePathSegment() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events/{city}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden",
				expanded.toString());
	}

	@Test
	public void testExpandLevelOnePathSegmentWithRegex() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events/{city:+}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden",
				expanded.toString());
	}

	@Test
	public void testExpandLevelOnePathSegmentWithPrefix() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events/v{version}/Wiesbaden");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("version", "1.2.0");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/v1.2.0/Wiesbaden",
				expanded.toString());
	}

	@Test
	public void testExpandLevelOneQueryWithOneVariable() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events/Wiesbaden?eventName={eventName}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour",
				expanded.toString());
	}

	@Test
	public void testExpandLevelOneQueryWithTwoVariables() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events/Wiesbaden?eventName={eventName}&location={location}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof",
				expanded.toString());
	}


	@Test
	public void testExpandDoesNotChangeUrlWithoutVariables() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description",
				expanded.toString());
	}


	@Test
	public void testExpandWithFixedQuery() throws Exception {
		final PartialUriTemplate template =
				new PartialUriTemplate("http://example" +
						".com/events{/city}?eventName=Revo+Tour&location=Schlachthof{#section}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description",
				expanded.toString());
	}


	@Test
	public void testExpandWithFixedFragmentIdentifier() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events{/city}{?eventName," +
				"location}#price");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#price",
				expanded.toString());
	}


	@Test
	public void testExpandAllComponentsButFragmentIdentifier() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example.com/events{/city}{?eventName," +
				"location}{#section}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof{#section}",
				expanded.toString());
	}

	@Test
	public void testExpandOneOfTwoQueryVariables() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events{/city}/concerts{?eventName,location}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("location", "Schlachthof");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events{/city}/concerts?location=Schlachthof{&eventName}", expanded
				.toString());
	}

	@Test
	public void testExpandSegmentVariable() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events/{city}/concerts{?eventName,location}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("location", "Schlachthof");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events/Wiesbaden/concerts?location=Schlachthof{&eventName}", expanded
				.toString());
	}

	@Test
	public void testExpandQueryContinuationTemplate() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events{/city}/concerts?eventName=Revo+Tour{&location}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("location", "Schlachthof");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example.com/events{/city}/concerts?eventName=Revo+Tour&location=Schlachthof",
				expanded.toString());
	}

	@Test
	public void testExpandQueryContinuationTemplateAfterFixedQueryContinuation() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events{/city}/concerts?eventName=Revo+Tour&foo=bar{&location}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("location", "Schlachthof");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example" +
				".com/events{/city}/concerts?eventName=Revo+Tour&foo=bar&location=Schlachthof", expanded.toString());
	}

	@Test
	public void testExpandQueryContinuationTemplatesAfterFixedQueryContinuation() throws Exception {
		final PartialUriTemplate template = new PartialUriTemplate("http://example" +
				".com/events{/city}/concerts?eventName=Revo+Tour&foo=bar{&location,baz}");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("baz", "Gnarf");
		val.put("location", "Schlachthof");
		final PartialUriTemplateComponents expanded = template.expand(val);
		Assert.assertEquals("http://example" +
				".com/events{/city}/concerts?eventName=Revo+Tour&foo=bar&location=Schlachthof&baz=Gnarf", expanded
				.toString());
	}
}