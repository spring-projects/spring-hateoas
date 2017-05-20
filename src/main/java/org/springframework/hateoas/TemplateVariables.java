/*
 * Copyright 2014-2016 the original author or authors.
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

import static org.springframework.hateoas.TemplateVariable.VariableType.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.EqualsAndHashCode;

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
	private TemplateVariables(TemplateVariable... variables) {
		this(Arrays.asList(variables));
	}

	/**
	 * Creates a new {@link TemplateVariables} for the given {@link TemplateVariable}s.
	 * 
	 * @param variables must not be {@literal null}.
	 */
	private TemplateVariables(List<TemplateVariable> variables) {

		Assert.notNull(variables, "Template variables must not be null!");
		this.variables = Collections.unmodifiableList(variables);
	}

	/**
	 * Creates a new {@link TemplateVariables} for the given {@link TemplateVariable}s.
	 * 
	 * @param variables must not be {@literal null}.
	 */
	public static TemplateVariables of(TemplateVariable... variables) {
		return new TemplateVariables(variables);
	}

	/**
	 * Creates a new {@link TemplateVariables} for the given {@link TemplateVariable}s.
	 * 
	 * @param variables must not be {@literal null}.
	 */
	public static TemplateVariables of(List<TemplateVariable> variables) {
		return new TemplateVariables(variables);
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

		List<TemplateVariable> result = new ArrayList<TemplateVariable>(this.variables.size() + variables.size());
		result.addAll(this.variables);

		for (TemplateVariable variable : variables) {
			if (!containsEquivalentFor(variable)) {
				result.add(variable);
			}
		}

		return TemplateVariables.of(result);
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

	public TemplateVariables getRequiredVariables() {

		if (isEmpty()) {
			return this;
		}

		List<TemplateVariable> result = new ArrayList<TemplateVariable>();

		for (TemplateVariable variable : this.variables) {
			if (variable.isRequired()) {
				result.add(variable);
			}
		}

		return TemplateVariables.of(result);
	}

	public List<String> getNames() {

		List<String> names = new ArrayList<String>(variables.size());

		for (TemplateVariable variable : variables) {
			names.add(variable.getName());
		}

		return Collections.unmodifiableList(names);
	}

	public boolean isEmpty() {
		return variables.isEmpty();
	}

	public boolean hasVariable(String name) {

		for (TemplateVariable variable : variables) {
			if (variable.hasName(name)) {
				return true;
			}
		}

		return false;
	}

	private boolean containsEquivalentFor(TemplateVariable candidate) {

		for (TemplateVariable variable : this.variables) {
			if (variable.isEquivalent(candidate)) {
				return true;
			}
		}

		return false;
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
