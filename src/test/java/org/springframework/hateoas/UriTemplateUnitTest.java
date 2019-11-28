/*
 * Copyright 2014-2015 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.TemplateVariable.VariableType;

/**
 * Unit tests for {@link UriTemplate}.
 *
 * @author Oliver Gierke
 * @author JamesE Richardson
 */
class UriTemplateUnitTest {

	/**
	 * @see #137
	 */
	@Test
	void discoversTemplate() {

		assertThat(UriTemplate.isTemplate("/foo{?bar}")).isTrue();
		assertThat(UriTemplate.isTemplate("/foo")).isFalse();
		assertThat(UriTemplate.isTemplate(null)).isFalse();
		assertThat(UriTemplate.isTemplate("")).isFalse();
	}

	/**
	 * @see #137
	 */
	@Test
	void discoversRequestParam() {

		UriTemplate template = UriTemplate.of("/foo{?bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM));
	}

	/**
	 * @see #137
	 */
	@Test
	void discoversRequestParamCntinued() {

		UriTemplate template = UriTemplate.of("/foo?bar{&foobar}");

		assertVariables(template, new TemplateVariable("foobar", VariableType.REQUEST_PARAM_CONTINUED));
	}

	/**
	 * @see #137
	 */
	@Test
	void discoversOptionalPathVariable() {

		UriTemplate template = UriTemplate.of("/foo{/bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.SEGMENT));
	}

	/**
	 * @see #137
	 */
	@Test
	void discoversPathVariable() {

		UriTemplate template = UriTemplate.of("/foo/{bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.PATH_VARIABLE));
	}

	/**
	 * @see #137
	 */
	@Test
	void discoversFragment() {

		UriTemplate template = UriTemplate.of("/foo{#bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.FRAGMENT));
	}

	/**
	 * @see #137
	 */
	@Test
	void discoversMultipleRequestParam() {

		UriTemplate template = UriTemplate.of("/foo{?bar,foobar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM),
				new TemplateVariable("foobar", VariableType.REQUEST_PARAM));
	}

	/**
	 * @see #137
	 */
	@Test
	void expandsRequestParameter() {

		UriTemplate template = UriTemplate.of("/foo{?bar}");

		URI uri = template.expand(Collections.singletonMap("bar", "myBar"));
		assertThat(uri.toString()).isEqualTo("/foo?bar=myBar");
	}

	/**
	 * @see #137
	 */
	@Test
	void expandsMultipleRequestParameters() {

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("bar", "myBar");
		parameters.put("fooBar", "myFooBar");

		UriTemplate template = UriTemplate.of("/foo{?bar,fooBar}");

		URI uri = template.expand(parameters);
		assertThat(uri.toString()).isEqualTo("/foo?bar=myBar&fooBar=myFooBar");
	}

	/**
	 * @see #137
	 */
	@Test
	void rejectsMissingRequiredPathVariable() {

		UriTemplate template = UriTemplate.of("/foo/{bar}");

		assertThatIllegalArgumentException().isThrownBy(() -> {
			template.expand(Collections.emptyMap());
		});
	}

	/**
	 * @see #137
	 */
	@Test
	void expandsMultipleVariablesViaArray() {

		UriTemplate template = UriTemplate.of("/foo{/bar}{?firstname,lastname}{#anchor}");
		URI uri = template.expand("path", "Dave", "Matthews", "discography");
		assertThat(uri.toString()).isEqualTo("/foo/path?firstname=Dave&lastname=Matthews#discography");
	}

	/**
	 * @see #137
	 */
	@Test
	void expandsTemplateWithoutVariablesCorrectly() {
		assertThat(UriTemplate.of("/foo").expand().toString()).isEqualTo("/foo");
	}

	/**
	 * @see #137
	 */
	@Test
	void correctlyExpandsFullUri() {
		assertThat(UriTemplate.of("http://localhost:8080/foo{?bar}").expand().toString())
				.isEqualTo("http://localhost:8080/foo");
	}

	/**
	 * @see #137
	 */
	@Test
	void rendersUriTempalteWithPathVariable() {

		UriTemplate template = UriTemplate.of("/{foo}/bar{?page}");
		assertThat(template.toString()).isEqualTo("/{foo}/bar{?page}");
	}

	/**
	 * #@see 137
	 */
	@Test
	void addsTemplateVariables() {

		UriTemplate source = UriTemplate.of("/{foo}/bar{?page}");
		List<TemplateVariable> toAdd = Arrays.asList(new TemplateVariable("bar", VariableType.REQUEST_PARAM));

		List<TemplateVariable> expected = new ArrayList<>();
		expected.addAll(source.getVariables());
		expected.addAll(toAdd);

		assertVariables(source.with(new TemplateVariables(toAdd)), expected);
	}

	/**
	 * @see #217
	 */
	@Test
	void doesNotAddVariablesForAlreadyExistingRequestParameters() {

		UriTemplate template = UriTemplate.of("/?page=2");
		UriTemplate result = template.with(new TemplateVariables(new TemplateVariable("page", VariableType.REQUEST_PARAM)));
		assertThat(result.getVariableNames()).isEmpty();

		result = template.with(new TemplateVariables(new TemplateVariable("page", VariableType.REQUEST_PARAM_CONTINUED)));
		assertThat(result.getVariableNames()).isEmpty();
	}

	/**
	 * @see #217
	 */
	@Test
	void doesNotAddVariablesForAlreadyExistingFragment() {

		UriTemplate template = UriTemplate.of("/#fragment");
		UriTemplate result = template.with(new TemplateVariables(new TemplateVariable("fragment", VariableType.FRAGMENT)));
		assertThat(result.getVariableNames()).isEmpty();
	}

	/**
	 * @see #271
	 */
	@Test
	void expandASimplePathVariable() {

		UriTemplate template = UriTemplate.of("/foo/{id}");
		assertThat(template.expand(2).toString()).isEqualTo("/foo/2");
	}

	/**
	 * @see #273
	 */
	@Test
	void rejectsEmptyBaseUri() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new UriTemplate(null, TemplateVariables.NONE);
		});
	}

	/**
	 * @see #281
	 */
	@Test
	void allowsAddingTemplateVariable() {

		UriTemplate template = UriTemplate.of("/").with("q", VariableType.REQUEST_PARAM);

		assertThat(template.toString()).isEqualTo("/{?q}");
	}

	/**
	 * @see #483
	 */
	@Test
	void compositveValuesAreRecognisedAsVariableType() {

		UriTemplate template = UriTemplate.of("/foo{&bar,foobar*}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM_CONTINUED),
				new TemplateVariable("foobar", VariableType.COMPOSITE_PARAM));
	}

	/**
	 * @see #483
	 */
	@Test
	@SuppressWarnings("serial")
	void expandsCompositeValueAsAssociativeArray() {

		UriTemplate template = UriTemplate.of("/foo{&bar,foobar*}");

		String expandedTemplate = template.expand(new HashMap<String, Object>() {
			{
				put("bar", "barExpanded");
				put("foobar", new HashMap<String, String>() {
					{
						put("city", "Clarksville");
						put("state", "TN");
					}
				});
			}
		}).toString();

		assertThat(expandedTemplate).isEqualTo("/foo?bar=barExpanded&city=Clarksville&state=TN");
	}

	/**
	 * @see #483
	 */
	@Test
	@SuppressWarnings("serial")
	void expandsCompositeValueAsList() {

		UriTemplate template = UriTemplate.of("/foo{&bar,foobar*}");

		String expandedTemplate = template.expand(new HashMap<String, Object>() {
			{
				put("bar", "barExpanded");
				put("foobar", Arrays.asList("foo1", "foo2"));
			}
		}).toString();

		assertThat(expandedTemplate).isEqualTo("/foo?bar=barExpanded&foobar=foo1&foobar=foo2");
	}

	/**
	 * @see #483
	 */
	@Test
	@SuppressWarnings("serial")
	void handlesCompositeValueAsSingleValue() {

		UriTemplate template = UriTemplate.of("/foo{&bar,foobar*}");

		String expandedTemplate = template.expand(new HashMap<String, Object>() {
			{
				put("bar", "barExpanded");
				put("foobar", "singleValue");
			}
		}).toString();

		assertThat(expandedTemplate).isEqualTo("/foo?bar=barExpanded&foobar=singleValue");
	}

	@Test // #1127
	void escapesBaseUriProperly() {

		assertThat(UriTemplate.of("https://example.org/foo and bar/{baz}").expand("xyzzy"))
				.hasToString("https://example.org/foo%20and%20bar/xyzzy");
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
