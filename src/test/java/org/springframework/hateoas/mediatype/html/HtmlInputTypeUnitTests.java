/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.html;

import static org.assertj.core.api.Assertions.*;

import lombok.Value;

import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Unit tests for {@link HtmlInputType}.
 *
 * @author Oliver Drotbohm
 */
public class HtmlInputTypeUnitTests {

	@Test // #1445
	void looksUpConstantsByString() {

		assertThat(HtmlInputType.of("text")).isEqualTo(HtmlInputType.TEXT);
		assertThat(HtmlInputType.of("text_value")).isEqualTo(HtmlInputType.TEXT);
		assertThat(HtmlInputType.of("foobar")).isNull();
	}

	@TestFactory // #1445
	Stream<DynamicTest> derivesInputTypesFromType() {

		Stream<$> numbers = HtmlInputType.NUMERIC_TYPES.stream() //
				.map(it -> $.of(it, HtmlInputType.NUMBER));

		Stream<$> others = Stream.of( //
				$.of(LocalDate.class, HtmlInputType.DATE), //
				$.of(LocalDateTime.class, HtmlInputType.DATETIME_LOCAL), //
				$.of(String.class, HtmlInputType.TEXT), //
				$.of(LocalTime.class, HtmlInputType.TIME), //
				$.of(URL.class, HtmlInputType.URL), //
				$.of(URI.class, HtmlInputType.URL) //
		);

		return DynamicTest.stream(Stream.concat(numbers, others), $::toString, $::verify);
	}

	@Value(staticConstructor = "of")
	static class $ {

		Class<?> type;
		HtmlInputType expected;

		public void verify() {
			assertThat(HtmlInputType.from(type)).isEqualTo(expected);
		}

		@Override
		public String toString() {
			return String.format("Derives %s from %s.", expected, type);
		}
	}
}
