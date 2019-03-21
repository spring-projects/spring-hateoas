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

import org.junit.Test;

/**
 * @author Greg Turnquist
 */
public class IanaLinkRelationUnitTest {

	/**
	 * @see #778
	 */
	@Test
	public void extractingValueOfIanaLinkRelationShouldWork() {
		assertThat(IanaLinkRelation.ABOUT.value()).isEqualTo("about");
	}

	/**
	 * @see #778
	 */
	@Test
	public void testingForOfficialIanaLinkRelation() {

		assertThat(IanaLinkRelation.isIanaRel((String) null)).isFalse();
		assertThat(IanaLinkRelation.isIanaRel((LinkRelation) null)).isFalse();
		assertThat(IanaLinkRelation.isIanaRel("")).isFalse();
		assertThat(IanaLinkRelation.isIanaRel("foo-bar")).isFalse();

		assertThat(IanaLinkRelation.isIanaRel("about")).isTrue();
		assertThat(IanaLinkRelation.isIanaRel("ABOUT")).isTrue();
	}

	/**
	 * @see #778
	 */
	@Test
	public void parsingIanaLinkRelationsShouldWork() {

		assertThat(IanaLinkRelation.parse("about")).isEqualTo(IanaLinkRelation.ABOUT);
		assertThat(IanaLinkRelation.parse("ABOUT")).isEqualTo(IanaLinkRelation.ABOUT);

		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelation.parse(null));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelation.parse(""));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelation.parse("faulty"));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelation.parse("FAULTY"));
	}

	@Test
	public void testIanaLinkRelationShouldPass() {
		assertThat(IanaLinkRelation.isIanaRel(IanaLinkRelation.ABOUT)).isTrue();
	}

	@Test
	public void comparingNonIanaLinkRelationsToIanaLinkRelationsDontGuaranteeAMatch() {

		assertThat(IanaLinkRelation.isIanaRel(new CustomLinkRelation("about"))).isTrue();
		assertThat(IanaLinkRelation.isIanaRel(new CustomLinkRelation("ABOUT"))).isTrue();
		assertThat(IanaLinkRelation.isIanaRel(new CustomLinkRelation("something-new"))).isFalse();
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
