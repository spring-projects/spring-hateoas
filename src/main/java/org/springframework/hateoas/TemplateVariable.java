/*
 * Copyright 2014-2020 the original author or authors.
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
import lombok.Value;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A single template variable.
 * 
 * @author Oliver Gierke
 * @author JamesE Richardson
 */
@Value
@EqualsAndHashCode
public final class TemplateVariable implements Serializable {

	private static final long serialVersionUID = -2731446749851863774L;

	String name;
	TemplateVariable.VariableType type;
	String description;

	/**
	 * Creates a new {@link TemplateVariable} with the given name and type.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param type must not be {@literal null}.
	 */
	public TemplateVariable(String name, TemplateVariable.VariableType type) {
		this(name, type, "");
	}

	/**
	 * Creates a new {@link TemplateVariable} with the given name, type and description.
	 * 
	 * @param name must not be {@literal null} or empty.
	 * @param type must not be {@literal null}.
	 * @param description must not be {@literal null}.
	 */
	public TemplateVariable(String name, TemplateVariable.VariableType type, String description) {

		Assert.hasText(name, "Variable name must not be null or empty!");
		Assert.notNull(type, "Variable type must not be null!");
		Assert.notNull(description, "Description must not be null!");

		this.name = name;
		this.type = type;
		this.description = description;
	}

	/**
	 * Static helper to fashion {@link VariableType#PATH_VARIABLE} variables.
	 *
	 * @param pathVariable
	 * @return
	 */
	public static TemplateVariable pathVariable(String pathVariable) {
		return new TemplateVariable(pathVariable, VariableType.PATH_VARIABLE);
	}

	/**
	 * Static helper to fashion {@link VariableType#REQUEST_PARAM} variables.
	 *
	 * @param requestParam
	 * @return
	 */
	public static TemplateVariable requestParam(String requestParam) {
		return new TemplateVariable(requestParam, VariableType.REQUEST_PARAM);
	}

	/**
	 * Static helper to fashion {@link VariableType#REQUEST_PARAM_CONTINUED} variables.
	 *
	 * @param requestParam
	 * @return
	 */
	public static TemplateVariable requestParamContinued(String requestParam) {
		return new TemplateVariable(requestParam, VariableType.REQUEST_PARAM_CONTINUED);
	}

	/**
	 * Static helper to fashion {@link VariableType#SEGMENT} variables.
	 *
	 * @param segment
	 * @return
	 */
	public static TemplateVariable segment(String segment) {
		return new TemplateVariable(segment, VariableType.SEGMENT);
	}

	/**
	 * Static helper to fashion {@link VariableType#FRAGMENT} variables.
	 *
	 * @param fragment
	 * @return
	 */
	public static TemplateVariable fragment(String fragment) {
		return new TemplateVariable(fragment, VariableType.FRAGMENT);
	}

	/**
	 * Static helper to fashion {@link VariableType#COMPOSITE_PARAM} variables.
	 *
	 * @param compositeParam
	 * @return
	 */
	public static TemplateVariable compositeParam(String compositeParam) {
		return new TemplateVariable(compositeParam, VariableType.COMPOSITE_PARAM);
	}
	
	/**
	 * Returns whether the variable has a description.
	 * 
	 * @return
	 */
	public boolean hasDescription() {
		return StringUtils.hasText(description);
	}

	/**
	 * Returns whether the template variable is optional, which means the template can be expanded to a URI without a
	 * value given for that variable.
	 * 
	 * @return
	 */
	boolean isRequired() {
		return !type.isOptional();
	}

	/**
	 * Returns whether the given {@link TemplateVariable} is of the same type as the current one.
	 * 
	 * @param variable must not be {@literal null}.
	 * @return
	 */
	boolean isCombinable(TemplateVariable variable) {
		return this.type.canBeCombinedWith(variable.type);
	}

	/**
	 * Returns whether the given {@link TemplateVariable} is logically equivalent to the given one. This considers request
	 * parameter variables equivalent independently from whether they're continued or not.
	 * 
	 * @param variable
	 * @return
	 */
	boolean isEquivalent(TemplateVariable variable) {
		return this.name.equals(variable.name) && isCombinable(variable);
	}

	/**
	 * Returns whether the current {@link TemplateVariable} is representing a request parameter.
	 * 
	 * @return
	 */
	boolean isRequestParameterVariable() {
		return type.equals(REQUEST_PARAM) || type.equals(REQUEST_PARAM_CONTINUED);
	}

	/**
	 * Returns whether the variable is a fragment one.
	 * 
	 * @return
	 */
	boolean isFragment() {
		return type.equals(FRAGMENT);
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		String base = String.format("{%s%s}", type.toString(), name);
		return StringUtils.hasText(description) ? String.format("%s - %s", base, description) : base;
	}

	/**
	 * An enumeration for all supported variable types.
	 * 
	 * @author Oliver Gierke
	 */
	public enum VariableType {

		PATH_VARIABLE("", false), //
		REQUEST_PARAM("?", true), //
		REQUEST_PARAM_CONTINUED("&", true), //
		SEGMENT("/", true), //
		FRAGMENT("#", true), //
		COMPOSITE_PARAM("*", true);

		private static final List<VariableType> COMBINABLE_TYPES = Arrays.asList(REQUEST_PARAM, REQUEST_PARAM_CONTINUED);

		private final String key;
		private final boolean optional;

		VariableType(String key, boolean optional) {

			this.key = key;
			this.optional = optional;
		}

		/**
		 * Returns whether the variable of this type is optional.
		 * 
		 * @return
		 */
		public boolean isOptional() {
			return optional;
		}

		public boolean canBeCombinedWith(VariableType type) {
			return this.equals(type) || COMBINABLE_TYPES.contains(this) && COMBINABLE_TYPES.contains(type);
		}

		/**
		 * Returns the {@link VariableType} for the given variable key.
		 * 
		 * @param key must not be {@literal null}.
		 * @return
		 */
		public static TemplateVariable.VariableType from(String key) {

			return Arrays.stream(values()) //
					.filter(type -> type.key.equals(key)) //
					.findFirst() //
					.orElseThrow(() -> new IllegalArgumentException("Unsupported variable type " + key + "!"));
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			return key;
		}
	}
}
