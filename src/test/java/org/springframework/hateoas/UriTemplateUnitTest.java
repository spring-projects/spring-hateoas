/*
 * Copyright 2014-2024 the original author or authors.
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
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.hateoas.TemplateVariable.VariableType;

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

		assertVariables(template, new TemplateVariable("bar", VariableType.PATH_SEGMENT));
	}

	@Test // #137
	void discoversPathVariable() {

		UriTemplate template = UriTemplate.of("/foo/{bar}");

		assertVariables(template, new TemplateVariable("bar", VariableType.SIMPLE));
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
				new TemplateVariable("foobar", VariableType.REQUEST_PARAM_CONTINUED));
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
		List<TemplateVariable> toAdd = Arrays.asList(new TemplateVariable("bar", VariableType.REQUEST_PARAM_CONTINUED));

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
	void compositveValuesAreRecognised() {

		UriTemplate template = UriTemplate.of("/foo{&bar,foobar*}");

		TemplateVariable templateVariable = template.getVariables().get(1);

		assertThat(templateVariable.isComposite()).isTrue();
		assertThat(templateVariable.getType()).isEqualTo(VariableType.REQUEST_PARAM_CONTINUED);
	}

	@Test // #483
	@SuppressWarnings("serial")
	void expandsCompositeValueAsAssociativeArray() {

		of("/foo{?bar,foobar*}", "/foo?bar=barExpanded&city=Clarksville&state=TN") //
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

		of("/foo{?bar,foobar*}", "/foo?bar=barExpanded&foobar=foo1&foobar=foo2") //
				.param("bar", "barExpanded") //
				.param("foobar", Arrays.asList("foo1", "foo2")) //
				.verify();
	}

	@Test // #483
	void handlesCompositeValueAsSingleValue() {

		of("/foo{?bar,foobar*}", "/foo?bar=barExpanded&foobar=singleValue") //
				.param("bar", "barExpanded") //
				.param("foobar", "singleValue") //
				.verify();
	}

	@Test // #1127
	void escapesBaseUriProperly() {

		of("https://example.org/foo and bar/{baz}", "https://example.org/foo%20and%20bar/xyzzy") //
				.param("baz", "xyzzy") //
				.verify();

		of("/foo?foo=bar{&baz}", "/foo?foo=bar&baz=xyz").param("baz", "xyz").verify();
		of("?foo=bar{&baz}", "?foo=bar&baz=xyz").param("baz", "xyz").verify();
	}

	@TestFactory // #593
	public Stream<DynamicTest> uriTemplateExpansionsShouldWork() {
		return DynamicTest.stream(getEncodingFixtures(), EncodingFixture::toString, EncodingFixture::verify);
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

	@Test
	void expandsCompositePaths() {

		URI uri = UriTemplate.of("/foo{/bar*}") //
				.expand(Collections.singletonMap("bar", Arrays.asList("first", "second")));

		assertThat(uri).isEqualTo(URI.create("/foo/first/second"));
	}

	@Test // #1696
	void adaptsRequestParamVariableToContinuationIfBaseUriContainsParameter() {

		UriTemplate template = UriTemplate.of("/path/{bar}/foo.zip?type=foo")
				.with(new TemplateVariable("foobar", VariableType.REQUEST_PARAM));

		assertThat(template.toString()).isEqualTo("/path/{bar}/foo.zip?type=foo{&foobar}");
	}

	@Test // #1727
	void supportsVariableInHostName() {

		assertThatCode(() -> UriTemplate.of("https://{somehost}/somepath"))
				.doesNotThrowAnyException();
	}

	@Test // #1800
	void supportsDotsInVariableName() {
		assertThat(UriTemplate.of("/path/{foo.bar}").getVariableNames()).contains("foo.bar");
	}

	@Test // #2036
	void addsPathSegmentAtTheRightPositionWithinTheUri() {

		var template = UriTemplate.of("/api?foo=bar#baz");

		assertThat(template.with("p", VariableType.REQUEST_PARAM).toString())
				.isEqualTo("/api?foo=bar{&p}#baz");

		assertThat(template.with("p", VariableType.PATH_SEGMENT).toString())
				.isEqualTo("/api{/p}?foo=bar#baz");

		assertThat(template.with("p", VariableType.PATH_STYLE_PARAMETER).toString())
				.isEqualTo("/api{;p}?foo=bar#baz");
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

	@TestFactory
	Stream<DynamicTest> rfcExamples() {
		return DynamicTest.stream(foo(), EncodingFixture::toShortString, EncodingFixture::verify);
	}

	private static Stream<EncodingFixture> foo() {

		Map<String, Object> values = new HashMap<>();
		values.put("count", Arrays.asList("one", "two", "three"));
		values.put("dom", Arrays.asList("example", "com"));
		values.put("dub", "me/too");
		values.put("hello", "Hello World!");
		values.put("half", "50%");
		values.put("var", "value");
		values.put("who", "fred");
		values.put("base", "http://example.com/home/");
		values.put("path", "/foo/bar");
		values.put("list", Arrays.asList("red", "green", "blue"));
		values.put("keys", new LinkedHashMap<String, String>() {
			{
				put("semi", ";");
				put("dot", ".");
				put("comma", ",");
			}
		});
		values.put("v", 6);
		values.put("x", 1024);
		values.put("y", 768);
		values.put("empty", "");
		values.put("empty_keys", Collections.emptyList());
		values.put("undef", null);

		return Stream.of( //
				// 3.2.1
				of("{count}", "one,two,three"),
				of("{count*}", "one,two,three"),
				of("{/count}", "/one,two,three"),
				of("{/count*}", "/one/two/three"),
				of("{;count}", ";count=one,two,three"),
				of("{;count*}", ";count=one;count=two;count=three"),
				of("{?count}", "?count=one,two,three"),
				of("{?count*}", "?count=one&count=two&count=three"),
				of("?bar={&count*}", "?bar=&count=one&count=two&count=three"), //

				// 3.2.2
				of("{var}", "value"),
				of("{hello}", "Hello%20World%21"),
				of("{half}", "50%25"),
				of("O{empty}X", "OX"),
				of("O{undef}X", "OX"),
				of("{x,y}", "1024,768"),
				of("{x,hello,y}", "1024,Hello%20World%21,768"),
				of("?{x,empty}", "?1024,"),
				of("?{x,undef}", "?1024"),
				of("?{undef,y}", "?768"),
				of("{var:3}", "val"),
				of("{var:30}", "value"),
				of("{list}", "red,green,blue"),
				of("{list*}", "red,green,blue"),
				of("{keys}", "semi,%3B,dot,.,comma,%2C"),
				of("{keys*}", "semi=%3B,dot=.,comma=%2C"), //

				// 3.2.3
				of("{+var}", "value"),
				of("{+hello}", "Hello%20World!"),
				of("{+half}", "50%25"),
				of("{base}index", "http%3A%2F%2Fexample.com%2Fhome%2Findex"),
				of("{+base}index", "http://example.com/home/index"),
				of("O{+empty}X", "OX"),
				of("O{+undef}X", "OX"),
				of("{+path}/here", "/foo/bar/here"),
				of("here?ref={+path}", "here?ref=/foo/bar"),
				of("up{+path}{var}/here", "up/foo/barvalue/here"),
				of("{+x,hello,y}", "1024,Hello%20World!,768"),
				of("{+path,x}/here", "/foo/bar,1024/here"),
				of("{+path:6}/here", "/foo/b/here"),
				of("{+list}", "red,green,blue"),
				of("{+list*}", "red,green,blue"),
				of("{+keys}", "semi,;,dot,.,comma,,"),
				of("{+keys*}", "semi=;,dot=.,comma=,"), //

				// 3.2.4
				of("{#var}", "#value"),
				of("{#hello}", "#Hello%20World!"),
				of("{#half}", "#50%25"),
				of("foo{#empty}", "foo#"),
				of("foo{#undef}", "foo"),
				of("{#x,hello,y}", "#1024,Hello%20World!,768"),
				of("{#path,x}/here", "#/foo/bar,1024/here"),
				of("{#path:6}/here", "#/foo/b/here"),
				of("{#list}", "#red,green,blue"),
				of("{#list*}", "#red,green,blue"),
				of("{#keys}", "#semi,;,dot,.,comma,,"),
				of("{#keys*}", "#semi=;,dot=.,comma=,"), //

				// 3.2.5
				of("{.who}", ".fred"),
				of("{.who,who}", ".fred.fred"),
				of("{.half,who}", ".50%25.fred"),
				of("www{.dom*}", "www.example.com"),
				of("X{.var}", "X.value"),
				of("X{.empty}", "X."),
				of("X{.undef}", "X"),
				of("X{.var:3}", "X.val"),
				of("X{.list}", "X.red,green,blue"),
				of("X{.list*}", "X.red.green.blue"),
				of("X{.keys}", "X.semi,%3B,dot,.,comma,%2C"),
				of("X{.keys*}", "X.semi=%3B.dot=..comma=%2C"),
				of("X{.empty_keys}", "X"),
				of("X{.empty_keys*}", "X"),

				// 3.2.6 - Path segments
				of("{/who}", "/fred"),
				of("{/who,who}", "/fred/fred"),
				of("{/half,who}", "/50%25/fred"),
				of("{/who,dub}", "/fred/me%2Ftoo"),
				of("{/var}", "/value"),
				of("{/var,empty}", "/value/"),
				of("{/var,undef}", "/value"),
				of("{/var,x}/here", "/value/1024/here"),
				of("{/var:1,var}", "/v/value"),
				of("{/list}", "/red,green,blue"),
				of("{/list*}", "/red/green/blue"),
				of("{/list*,path:4}", "/red/green/blue/%2Ffoo"),
				of("{/keys}", "/semi,%3B,dot,.,comma,%2C"),
				of("{/keys*}", "/semi=%3B/dot=./comma=%2C"),

				// 3.2.7 -
				of("{;who}", ";who=fred"),
				of("{;half}", ";half=50%25"),
				of("{;empty}", ";empty"),
				of("{;v,empty,who}", ";v=6;empty;who=fred"),
				of("{;v,bar,who}", ";v=6;who=fred"),
				of("{;x,y}", ";x=1024;y=768"),
				of("{;x,y,empty}", ";x=1024;y=768;empty"),
				of("{;x,y,undef}", ";x=1024;y=768"),
				of("{;hello:5}", ";hello=Hello"),
				of("{;list}", ";list=red,green,blue"),
				of("{;list*}", ";list=red;list=green;list=blue"),
				of("{;keys}", ";keys=semi,%3B,dot,.,comma,%2C"),
				of("{;keys*}", ";semi=%3B;dot=.;comma=%2C"),

				// 3.2.8 - Form-Style Query Expansion
				of("{?who}", "?who=fred"),
				of("{?half}", "?half=50%25"),
				of("{?x,y}", "?x=1024&y=768"),
				of("{?x,y,empty}", "?x=1024&y=768&empty="),
				of("{?x,y,undef}", "?x=1024&y=768"),
				of("{?var:3}", "?var=val"),
				of("{?list}", "?list=red,green,blue"),
				of("{?list*}", "?list=red&list=green&list=blue"),
				of("{?keys}", "?keys=semi,%3B,dot,.,comma,%2C"),
				of("{?keys*}", "?semi=%3B&dot=.&comma=%2C"),

				//
				of("?foo={&who}", "?foo=&who=fred"),
				of("?foo={&half}", "?foo=&half=50%25"),
				of("?fixed=yes{&x}", "?fixed=yes&x=1024"),
				of("?foo={&x,y,empty}", "?foo=&x=1024&y=768&empty="),
				of("?foo={&x,y,undef}", "?foo=&x=1024&y=768"),
				of("?foo={&var:3}", "?foo=&var=val"),
				of("?foo={&list}", "?foo=&list=red,green,blue"),
				of("?foo={&list*}", "?foo=&list=red&list=green&list=blue"),
				of("?foo={&keys}", "?foo=&keys=semi,%3B,dot,.,comma,%2C"),
				of("?foo={&keys*}", "?foo=&semi=%3B&dot=.&comma=%2C")

		//
		) //

				.map(EncodingFixture::skipVarArgsVerification)
				.map(it -> it.params(values));
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

			return params(newParameters);
		}

		public EncodingFixture params(Map<String, Object> parameters) {
			return new EncodingFixture(template, uri, parameters, varArgsVerification);
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

		public String toShortString() {
			return String.format("Expanding %s to %s", template, uri);
		}

		@Override
		public String toString() {
			return String.format("Expanding %s using parameters %s results in %s.", template, parameters, uri);
		}
	}
}
