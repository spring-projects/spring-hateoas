/*
 * Copyright 2014 the original author or authors.
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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.hateoas.TemplateVariable.VariableType;

/**
 * Unit tests for {@link TemplateVariables}.
 * 
 * @author Oliver Gierke
 */
public class TemplateVariablesUnitTest {

	/**
	 * @see #137
	 */
	@Test
	public void rendersNoTempalteVariablesAsEmptyString() {
		assertThat(TemplateVariables.NONE.toString(), is(""));
	}

	/**
	 * @see #137
	 */
	@Test
	public void rendersSingleVariableCorrectly() {

		TemplateVariables variables = new TemplateVariables(new TemplateVariable("foo", VariableType.SEGMENT));
		assertThat(variables.toString(), is("{/foo}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesMultipleVariablesOfTheSameType() {

		TemplateVariable first = new TemplateVariable("foo", VariableType.REQUEST_PARAM);
		TemplateVariable second = new TemplateVariable("bar", VariableType.REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{?foo,bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesMultipleVariablesOfTheDifferentType() {

		TemplateVariable first = new TemplateVariable("foo", VariableType.SEGMENT);
		TemplateVariable second = new TemplateVariable("bar", VariableType.REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{/foo}{?bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void concatsVariables() {

		TemplateVariables first = new TemplateVariables(new TemplateVariable("foo", VariableType.SEGMENT));
		TemplateVariables second = new TemplateVariables(new TemplateVariable("bar", VariableType.REQUEST_PARAM));

		TemplateVariables variables = first.concat(second);

		assertThat(variables.toString(), is("{/foo}{?bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesContinuedParamWithParam() {

		TemplateVariable first = new TemplateVariable("foo", VariableType.REQUEST_PARAM);
		TemplateVariable second = new TemplateVariable("bar", VariableType.REQUEST_PARAM_CONTINUED);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{?foo,bar}"));
	}

	/**
	 * @see #137
	 */
	@Test
	public void combinesContinuedParameterWithParameter() {

		TemplateVariable first = new TemplateVariable("foo", VariableType.REQUEST_PARAM_CONTINUED);
		TemplateVariable second = new TemplateVariable("bar", VariableType.REQUEST_PARAM);

		TemplateVariables variables = new TemplateVariables(first, second);

		assertThat(variables.toString(), is("{&foo,bar}"));
	}

}
