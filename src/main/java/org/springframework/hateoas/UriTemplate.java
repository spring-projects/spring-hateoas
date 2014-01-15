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

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.hateoas.UriTemplate.TemplateVariable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Custom URI template to support qualified URI template variables.
 * 
 * @author Oliver Gierke
 * @see http://tools.ietf.org/html/rfc6570
 * @since 0.9
 */
public class UriTemplate implements Iterable<TemplateVariable> {

	private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{([\\?\\&#/]?)([\\w\\,]+)\\}");

	private final List<TemplateVariable> variables = new ArrayList<TemplateVariable>();
	private String baseUri;

	/**
	 * Creates a new {@link UriTemplate} using the given template string.
	 * 
	 * @param template must not be {@literal null} or empty.
	 */
	public UriTemplate(String template) {

		Assert.hasText(template, "Template must not be null or empty!");

		Matcher matcher = VARIABLE_REGEX.matcher(template);

		while (matcher.find()) {

			if (baseUri == null) {
				this.baseUri = template.substring(0, matcher.start(0));
			}

			VariableType type = VariableType.from(matcher.group(1));
			String[] names = matcher.group(2).split(",");

			for (String name : names) {
				this.variables.add(new TemplateVariable(name, type));
			}
		}
	}

	/**
	 * Returns whether the given candidate is a URI template.
	 * 
	 * @param candidate
	 * @return
	 */
	public static boolean isTemplate(String candidate) {

		if (!StringUtils.hasText(candidate)) {
			return false;
		}

		return VARIABLE_REGEX.matcher(candidate).find();
	}

	/**
	 * Returns the {@link TemplateVariable}s discovered.
	 * 
	 * @return
	 */
	public List<TemplateVariable> getVariables() {
		return this.variables;
	}

	/**
	 * Returns the names of the variables discovered.
	 * 
	 * @return
	 */
	public List<String> getVariableNames() {

		List<String> names = new ArrayList<String>();

		for (TemplateVariable variable : variables) {
			names.add(variable.getName());
		}

		return names;
	}

	/**
	 * Expands the {@link UriTemplate} using the given parameters. The values will be applied in the order of the
	 * variables discovered.
	 * 
	 * @param parameters
	 * @return
	 * @see #expand(Map)
	 */
	public URI expand(Object... parameters) {

		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(baseUri);
		Iterator<Object> iterator = Arrays.asList(parameters).iterator();

		for (TemplateVariable variable : variables) {

			Object value = iterator.hasNext() ? iterator.next() : null;
			appendToBuilder(builder, variable, value);
		}

		return builder.build().toUri();
	}

	/**
	 * Expands the {@link UriTemplate} using the given parameters.
	 * 
	 * @param parameters must not be {@literal null}.
	 * @return
	 */
	public URI expand(Map<String, Object> parameters) {

		Assert.notNull(parameters, "Parameters must not be null!");
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath(baseUri);

		for (TemplateVariable variable : variables) {
			appendToBuilder(builder, variable, parameters.get(variable.name));
		}

		return builder.build().toUri();
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<TemplateVariable> iterator() {
		return this.variables.iterator();
	}

	/**
	 * Appends the value for the given {@link TemplateVariable} to the given {@link UriComponentsBuilder}.
	 * 
	 * @param builder must not be {@literal null}.
	 * @param variable must not be {@literal null}.
	 * @param value can be {@literal null}.
	 */
	private static void appendToBuilder(UriComponentsBuilder builder, TemplateVariable variable, Object value) {

		if (value == null) {

			if (variable.isRequired()) {
				throw new IllegalArgumentException(String.format("Template variable %s is required but no value was given!",
						variable.name));
			}

			return;
		}

		switch (variable.type) {
			case REQUEST_PARAM:
			case REQUEST_PARAM_CONTINUED:
				builder.queryParam(variable.name, value);
				break;
			case PATH_VARIABLE:
			case SEGMENT:
				builder.pathSegment(value.toString());
				break;
			case FRAGMENT:
				builder.fragment(value.toString());
				break;
		}
	}

	public static final class TemplateVariable {

		private final String name;
		private final VariableType type;

		/**
		 * Creates a new {@link TemplateVariable} with the given name and type.
		 * 
		 * @param name must not be {@literal null} or empty.
		 * @param type must not be {@literal null}.
		 */
		TemplateVariable(String name, VariableType type) {

			Assert.hasText("Variable name must not be null or empty!");
			Assert.notNull("Variable type must not be null!");

			this.name = name;
			this.type = type;
		}

		/**
		 * Returns the name of the variable.
		 * 
		 * @return
		 */
		public String getName() {
			return this.name;
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

		/* 
		 * (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {

			if (obj == this) {
				return true;
			}

			if (!(obj instanceof TemplateVariable)) {
				return false;
			}

			TemplateVariable that = (TemplateVariable) obj;
			return this.name.equals(that.name) && this.type.equals(that.type);
		}

		/* 
		 * (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {

			int result = 17;

			result += this.name.hashCode();
			result += this.type.hashCode();

			return result;
		}
	}

	/**
	 * An enumeration for all supported variable types.
	 * 
	 * @author Oliver Gierke
	 */
	static enum VariableType {

		PATH_VARIABLE("", false), //
		REQUEST_PARAM("?", true), //
		REQUEST_PARAM_CONTINUED("&", true), //
		SEGMENT("/", true), //
		FRAGMENT("#", true);

		private final String key;
		private final boolean optional;

		private VariableType(String key, boolean optional) {
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

		/**
		 * Returns the {@link VariableType} for the given variable key.
		 * 
		 * @param key must not be {@literal null}.
		 * @return
		 */
		public static VariableType from(String key) {

			for (VariableType type : values()) {
				if (type.key.equals(key)) {
					return type;
				}
			}

			throw new IllegalArgumentException("Unsupported variable type " + key + "!");
		}
	}
}
