/*
 * Copyright 2019 the original author or authors.
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

import org.junit.Test;

/**
 * @author Greg Turnquist
 */
public class IanaLinkRelationTest {

	@Test
	public void extractingValueOfLinkRelationShouldWork() {
		assertThat(IanaLinkRelation.ABOUT.value()).isEqualTo("about");
	}

	@Test
	public void testingForIana() {

		assertThat(IanaLinkRelation.isIanaRel(null)).isFalse();
		assertThat(IanaLinkRelation.isIanaRelIgnoreCase(null)).isFalse();

		assertThat(IanaLinkRelation.isIanaRel("")).isFalse();
		assertThat(IanaLinkRelation.isIanaRelIgnoreCase("")).isFalse();

		assertThat(IanaLinkRelation.isIanaRel("about")).isTrue();
		assertThat(IanaLinkRelation.isIanaRelIgnoreCase("about")).isTrue();

		assertThat(IanaLinkRelation.isIanaRel("ABOUT")).isFalse();
		assertThat(IanaLinkRelation.isIanaRelIgnoreCase("ABOUT")).isTrue();

		assertThat(IanaLinkRelation.isIanaRel("INTERVALOVERLAPS")).isFalse();
		assertThat(IanaLinkRelation.isIanaRelIgnoreCase("INTERVALOVERLAPS")).isTrue();
	}

	@Test
	public void parsingLinkRelationsShouldWork() {

		assertThat(IanaLinkRelation.parse("about")).isEqualTo(IanaLinkRelation.ABOUT);
		assertThat(IanaLinkRelation.parseIgnoringCase("ABOUT")).isEqualTo(IanaLinkRelation.ABOUT);

		assertThat(IanaLinkRelation.parseIgnoringCase("INTERVALOVERLAPS")).isEqualTo(IanaLinkRelation.INTERVAL_OVERLAPS);

		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelation.parse("faulty"));
		assertThatIllegalArgumentException().isThrownBy(() -> IanaLinkRelation.parseIgnoringCase("FAULTY"));
	}
}
