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

import org.springframework.hateoas.TemplateVariable.VariableType;
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

		if (this.baseUri == null) {
			this.baseUri = template;
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
			appendToBuilder(builder, variable, parameters.get(variable.getName()));
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
						variable.getName()));
			}

			return;
		}

		switch (variable.getType()) {
			case REQUEST_PARAM:
			case REQUEST_PARAM_CONTINUED:
				builder.queryParam(variable.getName(), value);
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
}
