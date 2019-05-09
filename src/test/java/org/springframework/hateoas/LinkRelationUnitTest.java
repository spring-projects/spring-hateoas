/*
 * Copyright 2019 the original author or authors.
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

import lombok.Value;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * @author Greg Turnquist
 */
class LinkRelationUnitTest {

	@Test
	void customLinkRelationShouldWork() {

		LinkRelation linkRelation = new CustomLinkRelation("customer");
		assertThat(linkRelation.value()).isEqualTo("customer");
	}

	@Test
	void customEnumBasedLinkRelationShouldWork() {

		assertThat(MyOwnLinkRelation.FOO.value()).isEqualTo("foo");
		assertThat(MyOwnLinkRelation.BAR.value()).isEqualTo("bar");

		Set<LinkRelation> myOwnLinkRelations = Arrays.stream(MyOwnLinkRelation.values()).collect(Collectors.toSet());

		assertThat(myOwnLinkRelations).containsExactlyInAnyOrder(MyOwnLinkRelation.FOO, MyOwnLinkRelation.BAR);
	}

	@Test // #825
	void verifyRFC8288CaseInsensitiveMatching() {

		assertThat(LinkRelation.of("self")).isEqualTo(LinkRelation.of("SELF"));
		assertThat(LinkRelation.of("SeLf")).isEqualTo(IanaLinkRelations.SELF);

		assertThat(LinkRelation.of("self").isSameAs(LinkRelation.of("SELF"))).isTrue();
		assertThat(IanaLinkRelations.SELF.isSameAs(LinkRelation.of("seLF"))).isTrue();
	}

	/**
	 * Custom implementation of the {@link LinkRelation} interface.
	 */
	@Value
	static class CustomLinkRelation implements LinkRelation {

		private String value;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.LinkRelation#value()
		 */
		@Override
		public String value() {
			return value;
		}
	}

	/**
	 * Custom enum-based set of {@link LinkRelation}s.
	 */
	enum MyOwnLinkRelation implements LinkRelation {

		FOO("foo"), BAR("bar");

		private final String value;

		MyOwnLinkRelation(String value) {
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.LinkRelation#value()
		 */
		@Override
		public String value() {
			return value;
		}
	}

}
