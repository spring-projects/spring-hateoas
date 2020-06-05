/*
 * Copyright 2014-2021 the original author or authors.
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
import static org.springframework.hateoas.TemplateVariable.*;
import static org.springframework.hateoas.UriTemplateUnitTest.EncodingFixture.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.hateoas.TemplateVariable.*;

/**
 * Unit tests for {@link UriTemplate}.
 *
 * @author Oliver Gierke
 * @author JamesE Richardson
 */
class UriTemplateUnitTest {

	@Test // #137
	@SuppressWarnings("null")
	void discoversTemplate() {

		assertThat(UriTemplate.isTemplate("/foo{?bar}")).isTrue();
		assertThat(UriTemplate.isTemplate("/foo")).isFalse();
		assertThat(UriTemplate.isTemplate(null)).isFalse();
		assertThat(UriTemplate.isTemplate("")).isFalse();
	}

	@Test // #137
	void discoversRequestParam() {

		UriTemplate template = UriTemplate.of("/foo{?bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM));
	}

	@Test // #137
	void discoversRequestParamCntinued() {

		UriTemplate template = UriTemplate.of("/foo?bar{&foobar}");

		assertVariables(template, new TemplateVariable("foobar", VariableType.REQUEST_PARAM_CONTINUED));
	}

	@Test // #137
	void discoversOptionalPathVariable() {

		UriTemplate template = UriTemplate.of("/foo{/bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.SEGMENT));
	}

	@Test // #137
	void discoversPathVariable() {

		UriTemplate template = UriTemplate.of("/foo/{bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.PATH_VARIABLE));
	}

	@Test // #137
	void discoversFragment() {

		UriTemplate template = UriTemplate.of("/foo{#bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.FRAGMENT));
	}

	@Test // #137
	void discoversMultipleRequestParam() {

		UriTemplate template = UriTemplate.of("/foo{?bar,foobar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM),
				new TemplateVariable("foobar", VariableType.REQUEST_PARAM));
	}

	@Test // #137
	void expandsRequestParameter() {

		UriTemplate template = UriTemplate.of("/foo{?bar}");

		URI uri = template.expand(Collections.singletonMap("bar", "myBar"));
		assertThat(uri.toString()).isEqualTo("/foo?bar=myBar");
	}

	@Test // #137
	void expandsMultipleRequestParameters() {

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("bar", "myBar");
		parameters.put("fooBar", "myFooBar");

		UriTemplate template = UriTemplate.of("/foo{?bar,fooBar}");

		URI uri = template.expand(parameters);
		assertThat(uri.toString()).isEqualTo("/foo?bar=myBar&fooBar=myFooBar");
	}

	@Test // #137
	void rejectsMissingRequiredPathVariable() {

		UriTemplate template = UriTemplate.of("/foo/{bar}");

		assertThatIllegalArgumentException().isThrownBy(() -> {
			template.expand(Collections.emptyMap());
		});
	}

	@Test // #137
	void expandsMultipleVariablesViaArray() {

		UriTemplate template = UriTemplate.of("/foo{/bar}{?firstname,lastname}{#anchor}");
		URI uri = template.expand("path", "Dave", "Matthews", "discography");
		assertThat(uri.toString()).isEqualTo("/foo/path?firstname=Dave&lastname=Matthews#discography");
	}

	@Test // #137
	void expandsTemplateWithoutVariablesCorrectly() {
		assertThat(UriTemplate.of("/foo").expand().toString()).isEqualTo("/foo");
	}

	@Test // #137
	void correctlyExpandsFullUri() {
		assertThat(UriTemplate.of("http://localhost:8080/foo{?bar}").expand().toString())
				.isEqualTo("http://localhost:8080/foo");
	}

	@Test // #137
	void rendersUriTempalteWithPathVariable() {

		UriTemplate template = UriTemplate.of("/{foo}/bar{?page}");
		assertThat(template.toString()).isEqualTo("/{foo}/bar{?page}");
	}

	@Test // #137
	void addsTemplateVariables() {

		UriTemplate source = UriTemplate.of("/{foo}/bar{?page}");
		List<TemplateVariable> toAdd = Arrays.asList(new TemplateVariable("bar", VariableType.REQUEST_PARAM));

		List<TemplateVariable> expected = new ArrayList<>();
		expected.addAll(source.getVariables());
		expected.addAll(toAdd);

		assertVariables(source.with(new TemplateVariables(toAdd)), expected);
	}

	@Test // #217
	void doesNotAddVariablesForAlreadyExistingRequestParameters() {

		UriTemplate template = UriTemplate.of("/?page=2");
		UriTemplate result = template.with(new TemplateVariables(new TemplateVariable("page", VariableType.REQUEST_PARAM)));
		assertThat(result.getVariableNames()).isEmpty();

		result = template.with(new TemplateVariables(new TemplateVariable("page", VariableType.REQUEST_PARAM_CONTINUED)));
		assertThat(result.getVariableNames()).isEmpty();
	}

	@Test // #217
	void doesNotAddVariablesForAlreadyExistingFragment() {

		UriTemplate template = UriTemplate.of("/#fragment");
		UriTemplate result = template.with(new TemplateVariables(new TemplateVariable("fragment", VariableType.FRAGMENT)));
		assertThat(result.getVariableNames()).isEmpty();
	}

	@Test // #273
	@SuppressWarnings("null")
	void rejectsEmptyBaseUri() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			UriTemplate.of(null, TemplateVariables.NONE);
		});
	}

	@Test // #281
	void allowsAddingTemplateVariable() {

		UriTemplate template = UriTemplate.of("/").with("q", VariableType.REQUEST_PARAM);

		assertThat(template.toString()).isEqualTo("/{?q}");
	}

	@Test // #483
	void compositveValuesAreRecognisedAsVariableType() {

		UriTemplate template = UriTemplate.of("/foo{&bar,foobar*}");

		assertVariables(template, new TemplateVariable("bar", VariableType.REQUEST_PARAM_CONTINUED),
				new TemplateVariable("foobar", VariableType.COMPOSITE_PARAM));
	}

	@Test // #483
	@SuppressWarnings("serial")
	void expandsCompositeValueAsAssociativeArray() {

		of("/foo{&bar,foobar*}", "/foo?bar=barExpanded&city=Clarksville&state=TN") //
				.param("bar", "barExpanded") //
				.param("foobar", new HashMap<String, String>() {
					{
						put("city", "Clarksville");
						put("state", "TN");
					}
				}) //
				.verify();
	}

	@Test // #483
	void expandsCompositeValueAsList() {

		of("/foo{&bar,foobar*}", "/foo?bar=barExpanded&foobar=foo1&foobar=foo2") //
				.param("bar", "barExpanded") //
				.param("foobar", Arrays.asList("foo1", "foo2")) //
				.verify();
	}

	@Test // #483
	void handlesCompositeValueAsSingleValue() {

		of("/foo{&bar,foobar*}", "/foo?bar=barExpanded&foobar=singleValue") //
				.param("bar", "barExpanded") //
				.param("foobar", "singleValue") //
				.verify();
	}

	@Test // #1127
	void escapesBaseUriProperly() {
		of("https://example.org/foo and bar/{baz}", "https://example.org/foo%20and%20bar/xyzzy") //
				.param("baz", "xyzzy") //
				.verify();
	}

	@ParameterizedTest // #593
	@MethodSource("getEncodingFixtures")
	public void uriTemplateExpansionsShouldWork(EncodingFixture fixture) {
		fixture.verify();
	}

	@Test // #593
	void deserializesProperly() throws IOException, ClassNotFoundException {

		UriTemplate template = UriTemplate.of("/{foo}");

		try (ByteArrayOutputStream output = new ByteArrayOutputStream();
				ObjectOutputStream stream = new ObjectOutputStream(output)) {

			stream.writeObject(template);

			try (InputStream input = new ByteArrayInputStream(output.toByteArray());
					ObjectInputStream object = new ObjectInputStream(input)) {

				Object result = object.readObject();

				assertThat(result).isInstanceOfSatisfying(UriTemplate.class, it -> {
					assertThat(it.expand("bar")).hasToString("/bar");
				});
			}
		}
	}

	@Test // #1165
	void expandsTemplateWithAddedVariable() {

		UriTemplate template = UriTemplate.of("/foo") //
				.with(new TemplateVariable("bar", VariableType.REQUEST_PARAM));

		assertThat(template.expand("value").toString()).isEqualTo("/foo?bar=value");
	}

	@Test // #1172
	void useHelperMethodsToBuildUriTemplates() {

		assertThat(UriTemplate.of("/foo") //
				.with(pathVariable("var")) //
				.getVariableNames()) //
						.containsExactly("var");

		assertThat(UriTemplate.of("/foo") //
				.with(requestParameter("var")) //
				.with(requestParameterContinued("var2")) //
				.toString()) //
						.isEqualTo("/foo{?var,var2}");

		assertThat(UriTemplate.of("/foo") //
				.with(requestParameter("var")) //
				.with(requestParameter("var2")) //
				.toString()) //
						.isEqualTo("/foo{?var,var2}");

		assertThat(UriTemplate.of("/foo") //
				.with(requestParameterContinued("var2")) //
				.toString()).isEqualTo("/foo{&var2}");

		assertThat(UriTemplate.of("/foo") //
				.with(segment("var")) //
				.toString()) //
						.isEqualTo("/foo{/var}");

		assertThat(UriTemplate.of("/foo") //
				.with(fragment("var")) //
				.toString()) //
						.isEqualTo("/foo{#var}");

		assertThat(UriTemplate.of("/foo") //
				.with(compositeParameter("var")) //
				.toString()) //
						.isEqualTo("/foo{*var}");
	}

	@Test // #227
	void variableParameterIsTemplated() {

		assertThat(Link.of("http://localhost/api/rest/v1/userGroups/50/functions/{?id*}").isTemplated()).isTrue();
		assertThat(UriTemplate.isTemplate("http://localhost/api/rest/v1/userGroups/50/functions/{?id*}")).isTrue();
	}

	@Test // #475
	void variablesWithPercentEncodingShouldWork() {

		assertThat(UriTemplate.of("http://localhost/foo/bar/{%24filter}").expand("value"))
				.isEqualTo(URI.create("http://localhost/foo/bar/value"));
	}

	@Test // #475
	void variablesWithUnderscoresShouldWork() {

		assertThat(UriTemplate.of("http://localhost/foo/bar/{_filter}").expand("value"))
				.isEqualTo(URI.create("http://localhost/foo/bar/value"));
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

	private static Stream<EncodingFixture> getEncodingFixtures() {

		return Stream.of(//
				of("/foo/bar/{?x}", "/foo/bar/?x=1").param("x", 1), //
				of("/foo/bar/{?x,y}", "/foo/bar/?x=1&y=2").param("x", 1).param("y", 2),
				of("/foo/bar{?x}{&y}", "/foo/bar?x=1&y=2").param("x", 1).param("y", 2),
				of("/foo/bar?x=1{&y}", "/foo/bar?x=1&y=2").param("y", 2), //
				of("/foo/bar?x=1{&y,z}", "/foo/bar?x=1&y=2&z=3").param("y", 2).param("z", 3L),
				of("/foo{/x}", "/foo/1").param("x", 1), //
				of("/foo{/x,y}", "/foo/1/2").param("x", 1).param("y", "2"),
				of("/foo{/x}{/y}", "/foo/1/2").param("x", 1).param("y", "2"),
				of("/foo{/x}{/y}{?z}", "/foo/1/2?z=3").param("x", 1).param("y", "2").param("z", 3L),
				of("/foo/{x}", "/foo/1").param("x", 1), //
				of("/foo/{x}/bar", "/foo/1/bar").param("x", 1), //
				of("/services/foo/{x}/bar/{y}/gaz", "/services/foo/1/bar/2/gaz").param("x", 1).param("y", "2"),
				of("/foo/{x}/bar/{y}/bar{?z}", "/foo/1/bar/2/bar?z=3").param("x", 1).param("y", "2").param("z", 3),
				of("/foo/{x}/bar/{y}/bar{?z}", "/foo/1/bar/2/bar").param("x", 1).param("y", "2"),
				of("/foo/{x}/bar/{y}/bar{?z}", "/foo/1/bar/2/bar").param("x", 1).param("y", "2"),
				of("/foo/bar{?x,y,z}", "/foo/bar?x=1").param("x", 1), //
				of("/foo/bar{?x,y,z}", "/foo/bar?x=1&y=2").param("x", 1).param("y", "2"),
				of("/foo/bar{?x,y,z}", "/foo/bar?x=1&z=3").param("x", 1).param("z", 3L).skipVarArgsVerification(),
				of("/foo/{x}/bar{/y}{?z}", "/foo/1/bar/2?z=3").param("x", 1).param("y", "2").param("z", 3L),
				of("/foo/{x}/bar{/y}{?z}", "/foo/1/bar?z=3").param("x", 1).param("z", 3L).skipVarArgsVerification(),
				of("/foo/{x}/bar{?y}{#z}", "/foo/1/bar?y=2").param("x", 1).param("y", "2"),
				of("/foo/{x}/bar{?y}{#z}", "/foo/1/bar?y=2#3").param("x", 1).param("y", "2").param("z", 3L),
				of("/foo/{x}/bar{?y}{#z}", "/foo/1/bar#3").param("x", 1).param("z", 3L).skipVarArgsVerification(),
				of("/foo/b%20ar{?x}", "/foo/b%20ar?x=1").param("x", 1), //
				of("/foo/b\"ar{?x}", "/foo/b%22ar?x=1").param("x", 1), //
				of("/foo/b%22ar{?x}", "/foo/b%22ar?x=1").param("x", 1));
	}

	static class EncodingFixture {

		private final String template;
		private final URI uri;
		private final Map<String, Object> parameters;
		private final boolean varArgsVerification;

		private EncodingFixture(String template, URI uri, Map<String, Object> parameters, boolean varArgsVerification) {

			this.template = template;
			this.uri = uri;
			this.parameters = parameters;
			this.varArgsVerification = varArgsVerification;
		}

		public static EncodingFixture of(String template, String uri) {
			return new EncodingFixture(template, URI.create(uri), new LinkedHashMap<>(), true);
		}

		public EncodingFixture param(String key, Object value) {

			Map<String, Object> newParameters = new LinkedHashMap<>(parameters);
			newParameters.put(key, value);

			return new EncodingFixture(template, uri, newParameters, varArgsVerification);
		}

		public EncodingFixture skipVarArgsVerification() {
			return new EncodingFixture(template, uri, parameters, false);
		}

		public void verify() {

			UriTemplate uriTemplate = UriTemplate.of(template);

			assertThat(uriTemplate.expand(parameters)).isEqualTo(uri);

			if (varArgsVerification) {
				assertThat(uriTemplate.expand(parameters.values().toArray())).isEqualTo(uri);
			}
		}

		@Override
		public String toString() {
			return String.format("Expanding %s using parameters %s results in %s.", template, parameters, uri);
		}
	}
}
