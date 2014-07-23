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

import java.io.Serializable;
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
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Custom URI template to support qualified URI template variables.
 * 
 * @author Oliver Gierke
 * @see http://tools.ietf.org/html/rfc6570
 * @since 0.9
 */
public class UriTemplate implements Iterable<TemplateVariable>, Serializable {

	private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{([\\?\\&#/]?)([\\w\\,]+)\\}");
	private static final long serialVersionUID = -1007874653930162262L;

	private final TemplateVariables variables;;
	private String baseUri;

	/**
	 * Creates a new {@link UriTemplate} using the given template string.
	 * 
	 * @param template must not be {@literal null} or empty.
	 */
	public UriTemplate(String template) {

		Assert.hasText(template, "Template must not be null or empty!");

		Matcher matcher = VARIABLE_REGEX.matcher(template);
		int baseUriEndIndex = template.length();
		List<TemplateVariable> variables = new ArrayList<TemplateVariable>();

		while (matcher.find()) {

			int start = matcher.start(0);

			VariableType type = VariableType.from(matcher.group(1));
			String[] names = matcher.group(2).split(",");

			for (String name : names) {
				TemplateVariable variable = new TemplateVariable(name, type);

				if (!variable.isRequired() && start < baseUriEndIndex) {
					baseUriEndIndex = start;
				}

				variables.add(variable);
			}
		}

		this.variables = variables.isEmpty() ? TemplateVariables.NONE : new TemplateVariables(variables);
		this.baseUri = template.substring(0, baseUriEndIndex);
	}

	/**
	 * Creates a new {@link UriTemplate} from the given base URI and {@link TemplateVariables}.
	 * 
	 * @param baseUri must not be {@literal null} or empty.
	 * @param variables defaults to {@link TemplateVariables#NONE}.
	 */
	public UriTemplate(String baseUri, TemplateVariables variables) {

		Assert.hasText("Base URI must not be null or empty!");

		this.baseUri = baseUri;
		this.variables = variables == null ? TemplateVariables.NONE : variables;
	}

	/**
	 * Creates a new {@link UriTemplate} with the current {@link TemplateVariable}s augmented with the given ones.
	 * 
	 * @param variables can be {@literal null}.
	 * @return
	 */
	public UriTemplate with(TemplateVariables variables) {

		if (variables == null) {
			return this;
		}

		UriComponents components = UriComponentsBuilder.fromUriString(baseUri).build();
		List<TemplateVariable> result = new ArrayList<TemplateVariable>();

		for (TemplateVariable variable : variables) {

			boolean isRequestParam = variable.isRequestParameterVariable();
			boolean alreadyPresent = components.getQueryParams().containsKey(variable.getName());

			if (isRequestParam && alreadyPresent) {
				continue;
			}

			if (variable.isFragment() && StringUtils.hasText(components.getFragment())) {
				continue;
			}

			result.add(variable);
		}

		return new UriTemplate(baseUri, this.variables.concat(result));
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
		return this.variables.asList();
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

		if (TemplateVariables.NONE.equals(variables)) {
			return URI.create(baseUri);
		}

		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUri);
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
	public URI expand(Map<String, ? extends Object> parameters) {

		if (TemplateVariables.NONE.equals(variables)) {
			return URI.create(baseUri);
		}

		Assert.notNull(parameters, "Parameters must not be null!");
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUri);

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

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		UriComponents components = UriComponentsBuilder.fromUriString(baseUri).build();
		boolean hasQueryParameters = !components.getQueryParams().isEmpty();

		return baseUri + getOptionalVariables().toString(hasQueryParameters);
	}

	private TemplateVariables getOptionalVariables() {

		List<TemplateVariable> result = new ArrayList<TemplateVariable>();

		for (TemplateVariable variable : this) {
			if (!variable.isRequired()) {
				result.add(variable);
			}
		}

		return new TemplateVariables(result);
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
