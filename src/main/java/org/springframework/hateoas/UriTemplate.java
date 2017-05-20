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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;

import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.springmvc.AffordanceBuilder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * URI template with the ability to be partially expanded, no matter if its variables are required or not. Unsatisfied
 * variables are kept as variables. Other implementations either remove all unsatisfied variables or fail when required
 * variables are unsatisfied. This behavior is required due to the way an Affordance is created by
 * {@link AffordanceBuilder}, see package info for an overview of affordance creation.
 *
 * @author Oliver Gierke
 * @author Dietrich Schulten
 * @see http://tools.ietf.org/html/rfc6570
 * @see org.springframework.hateoas.affordance.springmvc
 * @since 0.9
 */
@EqualsAndHashCode
public class UriTemplate implements Iterable<TemplateVariable>, Serializable {

	private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{([\\?\\&#/]?)([\\w\\,\\.]+)(:??.*?)\\}");

	private static final Object REMOVE_VARIABLE = new Object();

	private static final long serialVersionUID = 3603502049431337211L;

	private final List<String> urlComponents = new ArrayList<String>();

	private final List<List<Integer>> variableIndices = new ArrayList<List<Integer>>();

	private final TemplateVariables variables;

	/**
	 * Creates a new {@link UriTemplate} using the given template string.
	 *
	 * @param template must not be {@literal null} or empty.
	 */
	public UriTemplate(String template) {
		this(template, TemplateVariables.NONE);
	}

	public UriTemplate(String template, TemplateVariables additionals) {

		Assert.hasText(template, "Template must not be null or empty!");

		Matcher matcher = VARIABLE_REGEX.matcher(template);
		// first group is the variable start without leading {: "", "/", "?", "#",
		// second group is the comma-separated name list without the trailing } of the variable
		int endOfPart = 0;

		List<TemplateVariable> variables = new ArrayList<TemplateVariable>();

		while (matcher.find()) {

			// 0 is the current match, i.e. the entire variable expression
			int startOfPart = matcher.start(0);
			// add part before current match
			if (endOfPart < startOfPart) {

				String partWithoutVariables = template.substring(endOfPart, startOfPart);
				StringTokenizer stringTokenizer = new StringTokenizer(partWithoutVariables, "?", true);
				boolean inQuery = false;

				while (stringTokenizer.hasMoreTokens()) {

					String token = stringTokenizer.nextToken();

					if ("?".equals(token)) {
						inQuery = true;
					} else {
						urlComponents.add(inQuery ? "?".concat(token) : token);
						variableIndices.add(Collections.<Integer>emptyList());
					}
				}
			}

			endOfPart = matcher.end(0);

			// add current match as part
			urlComponents.add(template.substring(startOfPart, endOfPart));

			// collect variablesInPart and track for each part which variables it contains
			// group(1) is the variable head without the leading {
			VariableType type = TemplateVariable.VariableType.from(matcher.group(1));

			// group(2) are the variable names
			String[] names = matcher.group(2).split(",");
			List<Integer> variablesInPart = new ArrayList<Integer>();

			for (String name : names) {

				TemplateVariable variable = TemplateVariable.of(name, type);
				variablesInPart.add(variables.size());
				variables.add(variable);
			}

			variableIndices.add(variablesInPart);
		}

		// finish off remaining part
		if (endOfPart < template.length()) {

			urlComponents.add(template.substring(endOfPart));
			variableIndices.add(Collections.<Integer>emptyList());
		}

		TemplateVariables temp = TemplateVariables.of(variables);

		for (TemplateVariable additional : additionals) {

			if (!temp.hasVariable(additional.getName())) {

				urlComponents.add(additional.toString());
				variableIndices.add(Collections.<Integer>emptyList());
				variables.add(additional);
			}
		}

		this.variables = TemplateVariables.of(variables);
	}

	private UriTemplate(UriTemplateComponents components, TemplateVariables additionals) {
		this(components.toString(), additionals);
	}

	/**
	 * Creates a new {@link UriTemplate} with the current {@link TemplateVariable}s augmented with the given ones.
	 *
	 * @param variables can be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public UriTemplate with(TemplateVariables variables) {

		if (variables == null) {
			return this;
		}

		UriComponents components = UriComponentsBuilder.fromUriString(urlComponents.get(0)).build();
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

		return new UriTemplate(asComponents().toString(), TemplateVariables.of(result));
	}

	/**
	 * Creates a new {@link UriTemplate} with a {@link TemplateVariable} with the given name and type added.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public UriTemplate with(String name, VariableType type) {
		return new UriTemplate(asComponents(), TemplateVariables.of(TemplateVariable.of(name, type)));
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
		return this.variables.getNames();
	}

	/**
	 * Returns the template as uri components, without variable expansion.
	 *
	 * @return components of the Uri
	 */
	public UriTemplateComponents asComponents() {
		return getUriTemplateComponents(Collections.<String, Object>emptyMap(), Collections.<String>emptyList());
	}

	/**
	 * Expands the template using given parameters
	 *
	 * @param parameters for expansion in the order of appearance in the template, must not be empty
	 * @return expanded template
	 */
	public UriTemplate expand(Object... parameters) {

		List<String> variableNames = getVariableNames();
		Map<String, Object> parameterMap = new LinkedHashMap<String, Object>();

		/**
		 * Zip the {@link variableNames} with the {@link parameters}, into a {@link Map}.
		 */
		for (int i=0; i < parameters.length; i++) {
			try {
				parameterMap.put(variableNames.get(i), parameters[i]);
			} catch (IndexOutOfBoundsException e) {
				break;
			}
		}

		return new UriTemplate(getUriTemplateComponents(parameterMap, Collections.<String>emptyList()),
				TemplateVariables.NONE);
	}

	/**
	 * Expands the template using given parameters. In case all variables are resolved, the returned {@link UriTemplate}
	 * can be turned into a URI.
	 *
	 * @param parameters must not be {@literal null}.
	 * @return expanded template
	 */
	public UriTemplate expand(Map<String, ?> parameters) {

		Assert.notNull(parameters, "Parameters must not be null!");

		return new UriTemplate(getUriTemplateComponents(parameters, Collections.<String>emptyList()),
				TemplateVariables.NONE);
	}

	/**
	 * Applies parameters to template variables.
	 *
	 * @param parameters to apply to variables
	 * @param requiredArgs if not empty, retains given requiredArgs
	 * @return uri components
	 */
	private UriTemplateComponents getUriTemplateComponents(Map<String, ?> parameters, List<String> requiredArgs) {

		Assert.notNull(parameters, "Parameters must not be null!");

		StringBuilder baseUrl = new StringBuilder(urlComponents.get(0));
		StringBuilder queryHead = new StringBuilder();
		StringBuilder queryTail = new StringBuilder();
		StringBuilder fragmentIdentifier = new StringBuilder();

		for (int i = 1; i < urlComponents.size(); i++) {

			String part = urlComponents.get(i);
			List<Integer> variablesInPart = variableIndices.get(i);

			if (variablesInPart.isEmpty()) {

				if (part.startsWith("?") || part.startsWith("&")) {
					queryHead.append(part);
				} else if (part.startsWith("#")) {
					fragmentIdentifier.append(part);
				} else {
					baseUrl.append(part);
				}

			} else {

				List<TemplateVariable> variableList = variables.asList();

				for (Integer variableInPart : variablesInPart) {

					TemplateVariable variable = variableList.get(variableInPart);

					Object value = parameters.get(variable.getName());

					// Strip variable
					if (value == REMOVE_VARIABLE) {
						continue;
					}

					// Keep variable
					if (value == null) {

						switch (variable.getType()) {

							case REQUEST_PARAM:
							case REQUEST_PARAM_CONTINUED:
								if (requiredArgs.isEmpty() || requiredArgs.contains(variable.getName())) {
									// query vars without value always go last (query tail)
									if (queryTail.length() > 0) {
										queryTail.append(',');
									}
									queryTail.append(variable.getName());
								}
								break;
							case FRAGMENT:
								fragmentIdentifier.append(variable.toString());
								break;
							default:
								baseUrl.append(variable.toString());
						}

						continue;

					} else {

						// Replace variable with value
						switch (variable.getType()) {

							case REQUEST_PARAM:
							case REQUEST_PARAM_CONTINUED:

								if (queryHead.length() == 0) {
									queryHead.append('?');
								} else {
									queryHead.append('&');
								}
								queryHead.append(variable.getName()).append('=').append(urlEncode(value.toString()));
								break;

							case SEGMENT:

								baseUrl.append('/');
								// fall through
							case PATH_VARIABLE:

								if (queryHead.length() != 0) {
									// level 1 variable in query
									queryHead.append(urlEncode(value.toString()));
								} else {
									baseUrl.append(urlEncode(value.toString()));
								}
								break;

							case FRAGMENT:

								fragmentIdentifier.append('#');
								fragmentIdentifier.append(urlEncode(value.toString()));
								break;
						}
					}
				}
			}
		}

		return new UriTemplateComponents(baseUrl.toString(), queryHead.toString(), queryTail.toString(),
				fragmentIdentifier.toString(), variables.getNames());
	}

	/**
	 * Turns the {@link UriTemplate} into a URI by expanding all remaining template variables with empty values.
	 * 
	 * @return the URI represented by the {@link UriTemplate}.
	 * @throws IllegalStateException in case the template still contains required template variables.
	 */
	public URI toUri() {

		TemplateVariables required = variables.getRequiredVariables();

		if (!required.isEmpty()) {
			throw new IllegalStateException("Required variables ".concat(required.toString()).concat(" were not expanded!"));
		}

		Map<String, Object> parameters = new HashMap<String, Object>();

		for (String name : variables.getNames()) {
			parameters.put(name, REMOVE_VARIABLE);
		}

		return URI.create(expand(parameters).asComponents().toString());
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<TemplateVariable> iterator() {
		return variables.iterator();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return asComponents().toString();
	}

	/**
	 * Strips all variables which are not required by any of the given action descriptors. If no action descriptors are
	 * given, nothing will be stripped.
	 *
	 * @param actionDescriptors to decide which variables are optional, may be empty
	 * @return partial uri template components without optional variables, if actionDescriptors was not empty
	 */
	public UriTemplateComponents stripOptionalVariables(List<ActionDescriptor> actionDescriptors) {
		return getUriTemplateComponents(Collections.<String, Object>emptyMap(), getRequiredArgNames(actionDescriptors));
	}

	private static String urlEncode(String s) {

		try {
			return URLEncoder.encode(s, Charset.forName("UTF-8").toString());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("failed to urlEncode " + s, e);
		}
	}

	private static List<String> getRequiredArgNames(List<ActionDescriptor> actionDescriptors) {

		List<String> ret = new ArrayList<String>();

		for (ActionDescriptor actionDescriptor : actionDescriptors) {
			ret.addAll(actionDescriptor.getRequiredParameters().keySet());
		}
		return ret;
	}
}
