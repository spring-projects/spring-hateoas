/*
 * Copyright 2014-2017 the original author or authors.
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

import static org.springframework.hateoas.TemplateVariable.VariableType.*;

import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.util.Assert;

/**
 * Wrapper type for a collection of {@link TemplateVariable}.
 *
 * @author Oliver Gierke
 */
@EqualsAndHashCode
public final class TemplateVariables implements Iterable<TemplateVariable>, Serializable {

	public static final TemplateVariables NONE = new TemplateVariables();
	private static final long serialVersionUID = -7736592281223783079L;

	private final List<TemplateVariable> variables;

	/**
	 * Creates a new {@link TemplateVariables} for the given {@link TemplateVariable}s.
	 *
	 * @param variables must not be {@literal null}.
	 */
	public TemplateVariables(TemplateVariable... variables) {
		this(Arrays.asList(variables));
	}

	/**
	 * Creates a new {@link TemplateVariables} for the given {@link TemplateVariable}s.
	 *
	 * @param variables must not be {@literal null}.
	 */
	public TemplateVariables(List<TemplateVariable> variables) {

		Assert.notNull(variables, "Template variables must not be null!");
		Assert.noNullElements(variables.toArray(), "Variables must not contain null values!");

		this.variables = Collections.unmodifiableList(variables);
	}

	/**
	 * Concatenates the given {@link TemplateVariable}s to the current one.
	 *
	 * @param variables must not be {@literal null}.
	 * @return
	 */
	public TemplateVariables concat(TemplateVariable... variables) {
		return concat(Arrays.asList(variables));
	}

	/**
	 * Concatenates the given {@link TemplateVariable}s to the current one.
	 *
	 * @param variables must not be {@literal null}.
	 * @return
	 */
	public TemplateVariables concat(Collection<TemplateVariable> variables) {

		List<TemplateVariable> result = new ArrayList<>(this.variables.size() + variables.size());
		result.addAll(this.variables);

		List<TemplateVariable> filtered = variables.stream() //
				.filter(variable -> !containsEquivalentFor(variable)).collect(Collectors.toList());

		result.addAll(filtered);

		return new TemplateVariables(result);
	}

	/**
	 * Concatenates the given {@link TemplateVariables} to the current one.
	 *
	 * @param variables must not be {@literal null}.
	 * @return
	 */
	public TemplateVariables concat(TemplateVariables variables) {
		return concat(variables.variables);
	}

	/**
	 * Returns the contained {@link TemplateVariable}s as {@link List}.
	 *
	 * @return
	 */
	public List<TemplateVariable> asList() {
		return this.variables;
	}

	private boolean containsEquivalentFor(TemplateVariable candidate) {

		return this.variables.stream() //
				.anyMatch(variable -> variable.isEquivalent(candidate));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<TemplateVariable> iterator() {
		return this.variables.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Returns the string representation of the template but forcing a continued style of expressing request parameters.
	 *
	 * @param appended
	 * @return
	 */
	String toString(boolean appended) {

		if (variables.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		TemplateVariable previous = null;

		for (TemplateVariable variable : variables) {

			VariableType type = variable.getType();
			type = appended && type.equals(REQUEST_PARAM) ? REQUEST_PARAM_CONTINUED : type;

			if (previous == null) {
				builder.append("{").append(type.toString());
			} else if (!previous.isCombinable(variable)) {
				builder.append("}{").append(type.toString());
			} else {
				builder.append(",");
			}

			previous = variable;
			builder.append(variable.getName());
		}

		return builder.append("}").toString();
	}
}
