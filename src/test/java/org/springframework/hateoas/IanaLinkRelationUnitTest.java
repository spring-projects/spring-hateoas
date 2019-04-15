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

import org.junit.jupiter.api.Test;

/**
 * @author Greg Turnquist
 */
class IanaLinkRelationUnitTest {

	/**
	 * @see #778
	 */
	@Test
	void extractingValueOfIanaLinkRelationShouldWork() {
		assertThat(IanaLinkRelations.ABOUT.value()).isEqualTo("about");
	}

	/**
	 * @see #778
	 */
	@Test
	void testingForOfficialIanaLinkRelation() {

		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelations.isIanaRel((String) null));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelations.isIanaRel((LinkRelation) null));
		assertThat(IanaLinkRelations.isIanaRel("")).isFalse();
		assertThat(IanaLinkRelations.isIanaRel("foo-bar")).isFalse();

		assertThat(IanaLinkRelations.isIanaRel("about")).isTrue();
		assertThat(IanaLinkRelations.isIanaRel("ABOUT")).isTrue();
	}

	/**
	 * @see #778
	 */
	@Test
	void parsingIanaLinkRelationsShouldWork() {

		assertThat(IanaLinkRelations.parse("about")).isEqualTo(IanaLinkRelations.ABOUT);
		assertThat(IanaLinkRelations.parse("ABOUT")).isEqualTo(IanaLinkRelations.ABOUT);

		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelations.parse(null));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelations.parse(""));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelations.parse("faulty"));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelations.parse("FAULTY"));
	}

	@Test
	void testIanaLinkRelationShouldPass() {
		assertThat(IanaLinkRelations.isIanaRel(IanaLinkRelations.ABOUT)).isTrue();
	}

	@Test
	void comparingNonIanaLinkRelationsToIanaLinkRelationsDontGuaranteeAMatch() {

		assertThat(IanaLinkRelations.isIanaRel(new CustomLinkRelation("about"))).isTrue();
		assertThat(IanaLinkRelations.isIanaRel(new CustomLinkRelation("ABOUT"))).isTrue();
		assertThat(IanaLinkRelations.isIanaRel(new CustomLinkRelation("something-new"))).isFalse();
	}

	/**
	 * Custom implementation of the {@link LinkRelation} interface.
	 */
	@Value
	static class CustomLinkRelation implements LinkRelation {

		private String value;

		@Override
		public String value() {
			return this.value;
		}
	}


}
