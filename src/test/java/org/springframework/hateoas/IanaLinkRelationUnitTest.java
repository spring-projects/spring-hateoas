/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.Value;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

/**
 * @author Greg Turnquist
 * @author Vedran Pavic
 */
class IanaLinkRelationUnitTest {

	/**
	 * @see #778
	 */
	@Test
	void extractingValueOfIanaLinkRelationShouldWork() {
		assertThat(IanaLinkRelations.ABOUT.value()).isEqualTo(IanaLinkRelations.ABOUT_VALUE);
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
	 * @see #1216
	 */
	@Test
	void testAllIanaLinkRelationsHaveStringConstant() {

		Set<String> linkRelations = Arrays.stream(IanaLinkRelations.class.getDeclaredFields()) //
				.filter(ReflectionUtils::isPublicStaticFinal) //
				.filter(field -> LinkRelation.class.equals(field.getType())) //
				.map(it -> ReflectionUtils.getField(it, null)) //
				.filter(Objects::nonNull) //
				.map(LinkRelation.class::cast) //
				.map(LinkRelation::value) //
				.collect(Collectors.toSet());

		Set<String> stringConstants = Arrays.stream(IanaLinkRelations.class.getDeclaredFields()) //
				.filter(ReflectionUtils::isPublicStaticFinal) //
				.filter(field -> String.class.equals(field.getType())) //
				.map(it -> ReflectionUtils.getField(it, null)) //
				.filter(Objects::nonNull) //
				.map(String.class::cast) //
				.collect(Collectors.toSet());

		assertEquals(linkRelations, stringConstants);
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
