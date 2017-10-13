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

import static org.assertj.core.api.Assertions.*;

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

		assertThat(UriTemplate.isTemplate("/foo{?bar}")).isTrue();
		assertThat(UriTemplate.isTemplate("/foo")).isFalse();
		assertThat(UriTemplate.isTemplate(null)).isFalse();
		assertThat(UriTemplate.isTemplate("")).isFalse();
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversRequestParam() {

		UriTemplate template = new UriTemplate("/foo{?bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversRequestParamCntinued() {

		UriTemplate template = new UriTemplate("/foo?bar{&foobar}");

		assertVariables(template, new TemplateVariable("foobar", VariableType.REQUEST_PARAM_CONTINUED));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversOptionalPathVariable() {

		UriTemplate template = new UriTemplate("/foo{/bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.SEGMENT));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversPathVariable() {

		UriTemplate template = new UriTemplate("/foo/{bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.PATH_VARIABLE));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversFragment() {

		UriTemplate template = new UriTemplate("/foo{#bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.FRAGMENT));
	}

	/**
	 * @see #137
	 */
	@Test
	public void discoversMultipleRequestParam() {

		UriTemplate template = new UriTemplate("/foo{?bar,foobar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM),
				new TemplateVariable("foobar", VariableType.REQUEST_PARAM));
	}

	/**
	 * @see #137
	 */
	@Test
	public void expandsRequestParameter() {

		UriTemplate template = new UriTemplate("/foo{?bar}");

		URI uri = template.expand(Collections.singletonMap("bar", "myBar"));
		assertThat(uri.toString()).isEqualTo("/foo?bar=myBar");
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

		URI uri = template.expand(parameters);
		assertThat(uri.toString()).isEqualTo("/foo?bar=myBar&fooBar=myFooBar");
	}

	/**
	 * @see #137
	 */
	@Test(expected = IllegalArgumentException.class)
	public void rejectsMissingRequiredPathVariable() {

		UriTemplate template = new UriTemplate("/foo/{bar}");
		template.expand(Collections.<String, Object> emptyMap());
	}

	/**
	 * @see #137
	 */
	@Test
	public void expandsMultipleVariablesViaArray() {

		UriTemplate template = new UriTemplate("/foo{/bar}{?firstname,lastname}{#anchor}");
		URI uri = template.expand("path", "Dave", "Matthews", "discography");
		assertThat(uri.toString()).isEqualTo("/foo/path?firstname=Dave&lastname=Matthews#discography");
	}

	/**
	 * @see #137
	 */
	@Test
	public void expandsTemplateWithoutVariablesCorrectly() {
		assertThat(new UriTemplate("/foo").expand().toString()).isEqualTo("/foo");
	}

	/**
	 * @see #137
	 */
	@Test
	public void correctlyExpandsFullUri() {
		assertThat(new UriTemplate("http://localhost:8080/foo{?bar}").expand().toString())
				.isEqualTo("http://localhost:8080/foo");
	}

	/**
	 * @see #137
	 */
	@Test
	public void rendersUriTempalteWithPathVariable() {

		UriTemplate template = new UriTemplate("/{foo}/bar{?page}");
		assertThat(template.toString()).isEqualTo("/{foo}/bar{?page}");
	}

	/**
	 * #@see 137
	 */
	@Test
	public void addsTemplateVariables() {

		UriTemplate source = new UriTemplate("/{foo}/bar{?page}");
		List<TemplateVariable> toAdd = Arrays.asList(new TemplateVariable("bar", VariableType.REQUEST_PARAM));

		List<TemplateVariable> expected = new ArrayList<TemplateVariable>();
		expected.addAll(source.getVariables());
		expected.addAll(toAdd);

		assertVariables(source.with(new TemplateVariables(toAdd)), expected);
	}

	/**
	 * @see #217
	 */
	@Test
	public void doesNotAddVariablesForAlreadyExistingRequestParameters() {

		UriTemplate template = new UriTemplate("/?page=2");
		UriTemplate result = template.with(new TemplateVariables(new TemplateVariable("page", VariableType.REQUEST_PARAM)));
		assertThat(result.getVariableNames()).isEmpty();

		result = template.with(new TemplateVariables(new TemplateVariable("page", VariableType.REQUEST_PARAM_CONTINUED)));
		assertThat(result.getVariableNames()).isEmpty();
	}

	/**
	 * @see #217
	 */
	@Test
	public void doesNotAddVariablesForAlreadyExistingFragment() {

		UriTemplate template = new UriTemplate("/#fragment");
		UriTemplate result = template.with(new TemplateVariables(new TemplateVariable("fragment", VariableType.FRAGMENT)));
		assertThat(result.getVariableNames()).isEmpty();
	}

	/**
	 * @see #271
	 */
	@Test
	public void expandASimplePathVariable() {

		UriTemplate template = new UriTemplate("/foo/{id}");
		assertThat(template.expand(2).toString()).isEqualTo("/foo/2");
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

		assertThat(template.toString()).isEqualTo("/{?q}");
	}

	private static void assertVariables(UriTemplate template, TemplateVariable... variables) {
		assertVariables(template, Arrays.asList(variables));
	}

	private static void assertVariables(UriTemplate template, Collection<TemplateVariable> variables) {

		assertThat(template.getVariableNames()).hasSize(variables.size());
		assertThat(template.getVariables()).hasSize(variables.size());

		for (TemplateVariable variable : variables) {

			assertThat(template).contains(variable);
			assertThat(template.getVariableNames()).contains(variable.getName());
		}
	}
}
