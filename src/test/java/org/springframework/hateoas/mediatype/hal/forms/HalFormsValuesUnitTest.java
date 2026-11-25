/*
 * Copyright 2026 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HalFormsValues}.
 *
 * @author Réda Housni Alaoui
 */
class HalFormsValuesUnitTest {

	@Test
	void replacesValueOfSameProperty() {
		HalFormsValues values = HalFormsValues.none().and("number", "1234").and("number", "5678");

		assertThat(values.getValue("number")).isEqualTo("5678");
	}

	@Test
	void removesValueSetToNull() {
		HalFormsValues values = HalFormsValues.none().and("number", "1234").and("number", null);

		assertThat(values.getValue("number")).isNull();
	}

	@Test
	void rejectsBlankProperty() {
		HalFormsValues values = HalFormsValues.none();

		assertThatIllegalArgumentException().isThrownBy(() -> values.and(" ", "1234"))
				.withMessage("Property must not be null or empty!");
	}

	@Test
	void rendersValuesInToString() {
		assertThat(HalFormsValues.none().and("number", "1234")).hasToString("HalFormsValues{number=1234}");
	}
}
