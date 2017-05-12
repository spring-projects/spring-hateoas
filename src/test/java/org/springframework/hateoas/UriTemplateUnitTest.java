/*
 * Copyright 2014-2015 the original author or authors.
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.hateoas.TemplateVariable.VariableType;

/**
 * Unit tests for {@link UriTemplate}.
 * 
 * @author Oliver Gierke
 */
public class UriTemplateUnitTest {

	/**
	 * @see #137
	 */
	@Test
	public void discoversTemplate() {

		assertThat(UriTemplate.isTemplate("/foo{?bar}"), is(true));
		assertThat(UriTemplate.isTemplate("/foo"), is(false));
		assertThat(UriTemplate.isTemplate(null), is(false));
		assertThat(UriTemplate.isTemplate(""), is(false));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversRequestParam() {

		UriTemplate template = new UriTemplate("/foo{?bar}");

		assertVariables(template, TemplateVariable.of("bar", VariableType.REQUEST_PARAM));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversRequestParamCntinued() {

		UriTemplate template = new UriTemplate("/foo?bar{&foobar}");

		assertVariables(template, TemplateVariable.of("foobar", VariableType.REQUEST_PARAM_CONTINUED));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversOptionalPathVariable() {

		UriTemplate template = new UriTemplate("/foo{/bar}");

		assertVariables(template, TemplateVariable.of("bar", VariableType.SEGMENT));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversPathVariable() {

		UriTemplate template = new UriTemplate("/foo/{bar}");

		assertVariables(template, TemplateVariable.of("bar", VariableType.PATH_VARIABLE));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversFragment() {

		UriTemplate template = new UriTemplate("/foo{#bar}");

		assertVariables(template, TemplateVariable.of("bar", VariableType.FRAGMENT));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversMultipleRequestParam() {

		UriTemplate template = new UriTemplate("/foo{?bar,foobar}");

		assertVariables(template, TemplateVariable.of("bar", VariableType.REQUEST_PARAM),
				TemplateVariable.of("foobar", VariableType.REQUEST_PARAM));
	}

	/**
	 * @see #137
	 */
	@Test
	public void expandsRequestParameter() {

		UriTemplate template = new UriTemplate("/foo{?bar}");

		URI uri = template.expand(Collections.singletonMap("bar", "myBar")).toUri();
		assertThat(uri.toString(), is("/foo?bar=myBar"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void expandsMultipleRequestParameters() {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("bar", "myBar");
		parameters.put("fooBar", "myFooBar");

		UriTemplate template = new UriTemplate("/foo{?bar,fooBar}");

		URI uri = template.expand(parameters).toUri();
		assertThat(uri.toString(), is("/foo?bar=myBar&fooBar=myFooBar"));
	}

	/**
	 * @see #137
	 */
	@Test(expected = IllegalStateException.class)
	public void rejectsMissingRequiredPathVariable() {

		UriTemplate template = new UriTemplate("/foo/{bar}");
		template.toUri();
	}

	/**
	 * @see #137
	 */
	@Test
	public void expandsMultipleVariablesViaArray() {

		UriTemplate template = new UriTemplate("/foo{/bar}{?firstname,lastname}{#anchor}");
		UriTemplate expanded = template.expand("path", "Dave", "Matthews", "discography");

		assertThat(expanded.toString(), is("/foo/path?firstname=Dave&lastname=Matthews#discography"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void expandsTemplateWithoutVariablesCorrectly() {
		assertThat(new UriTemplate("/foo").expand().toString(), is("/foo"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void correctlyExpandsFullUri() {
		assertThat(new UriTemplate("http://localhost:8080/foo{?bar}").toUri().toString(),
				is("http://localhost:8080/foo"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void rendersUriTempalteWithPathVariable() {

		UriTemplate template = new UriTemplate("/{foo}/bar{?page}");
		assertThat(template.toString(), is("/{foo}/bar{?page}"));
	}

	/**
	 * #@see 137
	 */
	@Test
	public void addsTemplateVariables() {

		UriTemplate source = new UriTemplate("/{foo}/bar{?page}");
		List<TemplateVariable> toAdd = Arrays.asList(TemplateVariable.of("bar", VariableType.REQUEST_PARAM));

		List<TemplateVariable> expected = new ArrayList<TemplateVariable>();
		expected.addAll(source.getVariables());
		expected.addAll(toAdd);

		assertVariables(source.with(TemplateVariables.of(toAdd)), expected);
	}

	/**
	 * @see #217
	 */
	@Test
	public void doesNotAddVariablesForAlreadyExistingRequestParameters() {

		UriTemplate template = new UriTemplate("/?page=2");
		UriTemplate result = template
				.with(TemplateVariables.of(TemplateVariable.of("page", VariableType.REQUEST_PARAM)));
		assertThat(result.getVariableNames(), is(empty()));

		result = template.with(TemplateVariables.of(TemplateVariable.of("page", VariableType.REQUEST_PARAM_CONTINUED)));
		assertThat(result.getVariableNames(), is(empty()));
	}

	/**
	 * @see #217
	 */
	@Test
	public void doesNotAddVariablesForAlreadyExistingFragment() {

		UriTemplate template = new UriTemplate("/#fragment");
		UriTemplate result = template
				.with(TemplateVariables.of(TemplateVariable.of("fragment", VariableType.FRAGMENT)));
		assertThat(result.getVariableNames(), is(empty()));
	}

	/**
	 * @see #271
	 */
	@Test
	public void expandASimplePathVariable() {

		UriTemplate template = new UriTemplate("/foo/{id}");
		assertThat(template.expand(2).toString(), is("/foo/2"));
	}

	/**
	 * @see #273
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsEmptyBaseUri() {
		new UriTemplate(null, TemplateVariables.NONE);
	}

	/**
	 * @see #281
	 */
	@Test
	public void allowsAddingTemplateVariable() {

		UriTemplate template = new UriTemplate("/").with("q", VariableType.REQUEST_PARAM);

		assertThat(template.toString(), is("/{?q}"));
	}

	@Test
	public void testToStringWithQueryVariablesContainingDot() throws Exception {

		UriTemplate partialUriTemplateComponents = new UriTemplate(
				"http://localhost/events/query{?foo1,foo2,bar.baz,bars.empty,offset,size,strings.empty}");

		assertThat(partialUriTemplateComponents.getVariableNames(),
				contains("foo1", "foo2", "bar.baz", "bars.empty", "offset", "size", "strings.empty"));
	}

	@Test
	public void testExpandAllComponents() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events{/city}{?eventName,location}{#section}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description"));
	}

	@Test
	public void testExpandQueryWithTwoVariables() throws Exception {

		UriTemplate template = new UriTemplate("http://example.com/events/Wiesbaden{?eventName,location}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof"));
	}

	@Test
	public void testExpandQueryWithOneVariable() throws Exception {

		UriTemplate template = new UriTemplate("http://example.com/events/Wiesbaden{?eventName}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(), is("http://example.com/events/Wiesbaden?eventName=Revo+Tour"));
	}

	@Test
	public void testExpandLevelOnePathSegment() throws Exception {

		UriTemplate template = new UriTemplate("http://example.com/events/{city}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");

		assertThat(template.expand(val).toString(), is("http://example.com/events/Wiesbaden"));
	}

	@Test
	public void testExpandLevelOnePathSegmentWithRegex() throws Exception {

		UriTemplate template = new UriTemplate("http://example.com/events/{city:+}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");

		assertThat(template.expand(val).toString(), is("http://example.com/events/Wiesbaden"));
	}

	@Test
	public void testExpandLevelOnePathSegmentWithPrefix() throws Exception {

		UriTemplate template = new UriTemplate("http://example.com/events/v{version}/Wiesbaden");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("version", "1.2.0");

		assertThat(template.expand(val).toString(), is("http://example.com/events/v1.2.0/Wiesbaden"));
	}

	@Test
	public void testExpandLevelOneQueryWithOneVariable() throws Exception {

		UriTemplate template = new UriTemplate("http://example.com/events/Wiesbaden?eventName={eventName}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(), is("http://example.com/events/Wiesbaden?eventName=Revo+Tour"));
	}

	@Test
	public void testExpandLevelOneQueryWithTwoVariables() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events/Wiesbaden?eventName={eventName}&location={location}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof"));
	}

	@Test
	public void testExpandDoesNotChangeUrlWithoutVariables() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description"));
	}

	@Test
	public void testExpandWithFixedQuery() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events{/city}?eventName=Revo+Tour&location=Schlachthof{#section}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#description"));
	}

	@Test
	public void testExpandWithFixedFragmentIdentifier() throws Exception {
		final UriTemplate template = new UriTemplate(
				"http://example.com/events{/city}{?eventName," + "location}#price");
		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");
		val.put("section", "description");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof#price"));
	}

	@Test
	public void testExpandAllComponentsButFragmentIdentifier() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events{/city}{?eventName,location}{#section}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("eventName", "Revo Tour");
		val.put("location", "Schlachthof");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden?eventName=Revo+Tour&location=Schlachthof{#section}"));
	}

	@Test
	public void testExpandOneOfTwoQueryVariables() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events{/city}/concerts{?eventName,location}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("location", "Schlachthof");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events{/city}/concerts?location=Schlachthof{&eventName}"));
	}

	@Test
	public void testExpandSegmentVariable() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example" + ".com/events/{city}/concerts{?eventName,location}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("city", "Wiesbaden");
		val.put("location", "Schlachthof");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events/Wiesbaden/concerts?location=Schlachthof{&eventName}"));
	}

	@Test
	public void testExpandQueryContinuationTemplate() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example" + ".com/events{/city}/concerts?eventName=Revo+Tour{&location}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("location", "Schlachthof");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events{/city}/concerts?eventName=Revo+Tour&location=Schlachthof"));
	}

	@Test
	public void testExpandQueryContinuationTemplateAfterFixedQueryContinuation() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events{/city}/concerts?eventName=Revo+Tour&foo=bar{&location}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("location", "Schlachthof");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events{/city}/concerts?eventName=Revo+Tour&foo=bar&location=Schlachthof"));
	}

	@Test
	public void testExpandQueryContinuationTemplatesAfterFixedQueryContinuation() throws Exception {

		UriTemplate template = new UriTemplate(
				"http://example.com/events{/city}/concerts?eventName=Revo+Tour&foo=bar{&location,baz}");

		Map<String, Object> val = new HashMap<String, Object>();
		val.put("baz", "Gnarf");
		val.put("location", "Schlachthof");

		assertThat(template.expand(val).toString(),
				is("http://example.com/events{/city}/concerts?eventName=Revo+Tour&foo=bar&location=Schlachthof&baz=Gnarf"));
	}

	private static void assertVariables(UriTemplate template, TemplateVariable... variables) {
		assertVariables(template, Arrays.asList(variables));
	}

	private static void assertVariables(UriTemplate template, Collection<TemplateVariable> variables) {

		assertThat(template.getVariableNames(), hasSize(variables.size()));
		assertThat(template.getVariables(), hasSize(variables.size()));

		for (TemplateVariable variable : variables) {

			assertThat(template, hasItem(variable));
			assertThat(template.getVariableNames(), hasItems(variable.getName()));
		}
	}
}
