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
package org.springframework.hateoas.server.core;

import java.util.List;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A {@link LinkBuilderSupport} extension that can keep a list of {@link TemplateVariables} around.
 *
 * @author Oliver Gierke
 */
public abstract class TemplateVariableAwareLinkBuilderSupport<T extends TemplateVariableAwareLinkBuilderSupport<T>>
		extends LinkBuilderSupport<T> {

	private final TemplateVariables variables;

	protected TemplateVariableAwareLinkBuilderSupport(UriComponentsBuilder builder, TemplateVariables variables,
			List<Affordance> affordances) {

		super(builder, affordances);

		this.variables = variables;
	}

	protected TemplateVariableAwareLinkBuilderSupport(UriComponents components, TemplateVariables variables,
			List<Affordance> affordances) {

		super(components, affordances);

		this.variables = variables;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.LinkBuilderSupport#createNewInstance(org.springframework.web.util.UriComponentsBuilder, java.util.List)
	 */
	@Override
	protected final T createNewInstance(UriComponentsBuilder builder, List<Affordance> affordances) {
		return createNewInstance(builder, affordances, variables);
	}

	protected abstract T createNewInstance(UriComponentsBuilder builder, List<Affordance> affordances,
			TemplateVariables variables);

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.core.LinkBuilderSupport#toString()
	 */
	@Override
	public String toString() {

		String result = super.toString();

		if (variables == TemplateVariables.NONE) {
			return result;
		}

		if (!result.contains("#")) {
			return result.concat(variables.toString());
		}

		String[] parts = result.split("#");
		return parts[0].concat(variables.toString()).concat("#").concat(parts[0]);
	}
}
