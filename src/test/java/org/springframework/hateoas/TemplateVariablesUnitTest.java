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
import static org.springframework.hateoas.TemplateVariable.VariableType.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.TemplateVariable.VariableType;

/**
 * Unit tests for {@link TemplateVariables}.
 * 
 * @author Oliver Gierke
 */
class TemplateVariablesUnitTest {

	/**
	 * @see #137
	 */
	@Test
	void rendersNoTempalteVariablesAsEmptyString() {
		assertThat(TemplateVariables.NONE.toString()).isEqualTo("");
	}

	/**
	 * @see #137
	 */
	@Test
	void rendersSingleVariableCorrectly() {

		TemplateVariables variables = new TemplateVariables(new TemplateVariable("foo", SEGMENT));
		assertThat(variables.toString()).isEqualTo("{/foo}");
	}

	/**
	 * @see #137
	 */
	@Test
	void combinesMultipleVariablesOfTheSameType() {

		TemplateVariable first = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString()).isEqualTo("{?foo,bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	void combinesMultipleVariablesOfTheDifferentType() {

		TemplateVariable first = new TemplateVariable("foo", SEGMENT);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString()).isEqualTo("{/foo}{?bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	void concatsVariables() {

		TemplateVariables variables = new TemplateVariables(new TemplateVariable("foo", SEGMENT));
		variables = variables.concat(new TemplateVariable("bar", REQUEST_PARAM));

		assertThat(variables.toString()).isEqualTo("{/foo}{?bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	void combinesContinuedParamWithParam() {

		TemplateVariable first = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM_CONTINUED);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString()).isEqualTo("{?foo,bar}");
	}

	/**
	 * @see #137
	 */
	@Test
	void combinesContinuedParameterWithParameter() {

		TemplateVariable first = new TemplateVariable("foo", REQUEST_PARAM_CONTINUED);
		TemplateVariable second = new TemplateVariable("bar", REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString()).isEqualTo("{&foo,bar}");
	}

	/**
	 * @see #198
	 */
	@Test
	void dropsDuplicateTemplateVariable() {

		TemplateVariable variable = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariables variables = new TemplateVariables(variable);

		List<TemplateVariable> result = variables.concat(variable).asList();

		assertThat(result).hasSize(1);
		assertThat(result).contains(variable);
	}

	/**
	 * @see #217
	 */
	@Test
	void considersRequestParameterVariablesEquivalent() {

		TemplateVariable parameter = new TemplateVariable("foo", REQUEST_PARAM);
		TemplateVariable continued = new TemplateVariable("foo", REQUEST_PARAM_CONTINUED);
		TemplateVariable fragment = new TemplateVariable("foo", FRAGMENT);

		assertThat(parameter.isEquivalent(continued)).isTrue();
		assertThat(continued.isEquivalent(parameter)).isTrue();
		assertThat(fragment.isEquivalent(continued)).isFalse();
	}

	/**
	 * @see #217
	 */
	@Test
	void considersFragementVariable() {

		assertThat(new TemplateVariable("foo", VariableType.FRAGMENT).isFragment()).isTrue();
		assertThat(new TemplateVariable("foo", VariableType.REQUEST_PARAM).isFragment()).isFalse();
	}

	/**
	 * @see #217
	 */
	@Test
	void doesNotAddEquivalentVariable() {

		TemplateVariable parameter = new TemplateVariable("foo", VariableType.REQUEST_PARAM);
		TemplateVariable parameterContinued = new TemplateVariable("foo", VariableType.REQUEST_PARAM_CONTINUED);

		List<TemplateVariable> result = new TemplateVariables(parameter).concat(parameterContinued).asList();

		assertThat(result).hasSize(1);
		assertThat(result).contains(parameter);
	}

	/**
	 * @see #228
	 */
	@Test
	void variableRejectsEmptyName() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new TemplateVariable("", PATH_VARIABLE);
		});
	}

	/**
	 * @see #228
	 */
	@Test
	void variableRejectsNullName() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new TemplateVariable(null, PATH_VARIABLE);
		});
	}

	/**
	 * @see #228
	 */
	@Test
	void variableRejectsNullType() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new TemplateVariable("foo", null);
		});
	}

	/**
	 * @see #228
	 */
	@Test
	void variableRejectsNullDescription() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			new TemplateVariable("foo", PATH_VARIABLE, null);
		});
	}
}
