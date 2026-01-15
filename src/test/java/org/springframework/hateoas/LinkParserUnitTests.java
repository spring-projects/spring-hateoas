/*
 * Copyright 2024-2026 the original author or authors.
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
import static org.springframework.hateoas.LinkParser.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LinkParser}.
 *
 * @author Viliam Durina
 * @author Oliver Drotbohm
 */
class LinkParserUnitTests {

	@Test // GH-2099
	void parsingUnexpectedData() {

		// two URLs without a comma - the second URL is an unexpected text
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLinks("<url1>;rel=\"foo\"<url2>;rel= \"bar\""))
				.withMessage("Unexpected data at the end of Link header at index 16");
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLinks("<url1>; rel=\"foo\" <url2>;rel= \"bar\""))
				.withMessage("Unexpected data at the end of Link header at index 18");
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLinks("<url1> ; rel= \"foo\" <url2>;rel= \"bar\""))
				.withMessage("Unexpected data at the end of Link header at index 20");

		// unexpected text after a quoted string
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLinks("<url1>;rel=\"foo\"#"))
				.withMessage("Unexpected data at the end of Link header at index 16");
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLinks("<url1>;rel=\"foo\" foo bar"))
				.withMessage("Unexpected data at the end of Link header at index 17");

		// if the value isn't quoted, it can't be unexpected; all is part of the value
		assertThat(parseLink("<url1>;rel=foo#")) //
				.isEqualTo(Link.of("url1", "foo#"));

		// extra text after a comma - looks like a legit value for rel, but comma is special and starts a new link
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLinks("<url1>;rel=foo,bar"))
				.withMessage("Unexpected data at the end of Link header at index 15");

		// a trailing comma is ignored
		assertThat(parseLink("<url1>;rel=foo,")) //
				.isEqualTo(Link.of("url1", "foo"));

		// a trailing semicolon is also ignored
		assertThat(parseLink("<url1>;rel=foo;")) //
				.isEqualTo(Link.of("url1", "foo"));

		// unexpected text at the beginning
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLinks("foo bar <url>;rel=\"next\"")) //
				.withMessage("Unexpected data at the end of Link header at index 0");
	}

	@Test // GH-2099
	void parsingMissingData() {

		// missing trailing bracket
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLink("<https://example.com/;rel=next"))
				.withMessage("Missing closing '>' at index 30");

		// missing end quote
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLink("<https://example.com/>;rel=\"next"))
				.withMessage("Missing final quote at index 32");

		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLink("<https://example.com/>;rel='next"))
				.withMessage("Missing final quote at index 32");
	}

	@Test // GH-2099
	void parsingGreedyCapture() {

		// no greedy capture until `>`
		assertThat(parseLink("<url>;title=foo>;rel=\"next\"")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo>"));

		// no greedy capture until `;`
		assertThat(parseLink("<url>;title=\"foo;bar\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo;bar"));

		// no greedy capture until `,`
		assertThat(parseLink("<url>;title=\"foo,bar\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo,bar"));
	}

	@Test // GH-2099
	void parsingQuotedText() {

		// unquoting of double quotes
		assertThat(parseLink("<url>;title=\"\\\"bar\\\"\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("\"bar\""));

		// unquoting of single quotes
		assertThat(parseLink("<url>;title='\\'bar\\'';rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("'bar'"));

		// single quote is literal in double-quoted string
		assertThat(parseLink("<url>;title=\"'bar'\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("'bar'"));

		// double quote is literal in single-quoted string
		assertThat(parseLink("<url>;title='\"bar\"';rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("\"bar\""));

		// backslash unquoting
		assertThat(parseLink("<url>;title=\"foo\\\\bar\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo\\bar"));
		assertThat(parseLink("<url>;title='foo\\\\bar';rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo\\bar"));

		// unquoting of unnecessarily quoted text
		assertThat(parseLink("<url>;title=\"\\f\\o\\o\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo"));
		assertThat(parseLink("<url>;title='\\f\\o\\o';rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo"));

		// no java-style special characters
		assertThat(parseLink("<url>;title=\"\\r\\n\\t\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("rnt"));
		assertThat(parseLink("<url>;title='\\r\\n\\t';rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("rnt"));

		// quote within a token value - the quote, if it's not the first character, is literal
		assertThat(parseLink("<url>;title=foo\"bar\";rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo\"bar\""));
		assertThat(parseLink("<url>;title=foo'bar';rel=next")) //
				.isEqualTo(Link.of("url", "next").withTitle("foo'bar'"));
	}

	@Test // GH-2099
	void parsingEmptyString() {

		// at the end
		Link expected = Link.of("url", "next").withTitle("");

		// value missing
		assertThat(parseLink("<url>;rel=next;title")).isEqualTo(expected);
		// empty token-style value
		assertThat(parseLink("<url>;rel=next;title=")).isEqualTo(expected);
		// empty double-quoted string
		assertThat(parseLink("<url>;rel=next;title=\"\"")).isEqualTo(expected);
		// empty single-quoted string
		assertThat(parseLink("<url>;rel=next;title=''")).isEqualTo(expected);

		// not at the end
		expected = Link.of("url", "next").withTitle("").withName("a");

		assertThat(parseLink("<url>;rel=next;title;name=a")).isEqualTo(expected);
		assertThat(parseLink("<url>;rel=next;title=;name=a")).isEqualTo(expected);
		assertThat(parseLink("<url>;rel=next;title=\"\";name=a")).isEqualTo(expected);
		assertThat(parseLink("<url>;rel=next;title='';name=a")).isEqualTo(expected);
	}

	@Test // GH-2099
	void parsingMultipleRels() {

		assertThat(parseLinks("<url>;rel=next last")) //
				.containsExactly(Link.of("url", "next"), Link.of("url", "last"));

		assertThat(parseLinks("<url>;rel=\"next last\"")) //
				.containsExactly(Link.of("url", "next"), Link.of("url", "last"));

		assertThat(parseLinks("</prev>;rel=prev first,</next>;rel=next last")) //
				.containsExactly( //
						Link.of("/prev", "prev"), //
						Link.of("/prev", "first"), //
						Link.of("/next", "next"), //
						Link.of("/next", "last"));
	}

	@Test // GH-2099
	void parsingSpecialChars() {

		// within the href, `,` and `;` aren't special
		assertThat(parseLink("<http://example.com/?param=foo,bar;baz>;rel=next")) //
				.isEqualTo(Link.of("http://example.com/?param=foo,bar;baz", "next"));
	}

	@Test // GH-2099
	void parsingWhitespaceOtherThanSpace() {

		var source = "\n\r\t <url1>\n\r\t ;\n\r\t rel\n\r\t =\r\n\t next \r\n\t , \r\n\t ,"
				+ " \r\n\t <url2>\r\n\t ;\r\n\t rel \r\n\t = \r\n\t \"foo\"\r\n\t ; title=\"\r\n\t bar\r\n\t \"\r\n\t ";

		assertThat(parseLinks(source))
				.containsExactly(Link.of("url1", "next"), Link.of("url2", "foo").withTitle("\r\n\t bar\r\n\t "));
	}

	@Test // GH-2099
	void parsingEmptyRel() {

		// rel is empty string
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLink("<url>;rel=''"))
				.withMessage("Missing 'rel' attribute at index 12");

		// rel is a single space - if we split by whitespace, there's no value
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLink("<url>;rel=' '"))
				.withMessage("Missing 'rel' attribute at index 13");
	}

	@Test // GH-2099
	void directLinkParsing() {

		// leading whitespace
		assertThat(parseLink("  <url>;rel=next")).isEqualTo(Link.of("url", "next"));

		// unexpected data at the beginning
		assertThatIllegalArgumentException()
				.isThrownBy(() -> parseLink("foo <url>;rel=next"))
				.withMessage("Expecting '<' at index 0");
	}
}
