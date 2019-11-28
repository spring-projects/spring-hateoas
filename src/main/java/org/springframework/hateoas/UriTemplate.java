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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * Custom URI template to support qualified URI template variables.
 *
 * @author Oliver Gierke
 * @author JamesE Richardson
 * @see https://tools.ietf.org/html/rfc6570
 * @since 0.9
 */
public class UriTemplate implements Iterable<TemplateVariable>, Serializable {

	private static final Pattern VARIABLE_REGEX = Pattern.compile("\\{([\\?\\&#/]?)([\\w\\,*]+)\\}");
	private static final long serialVersionUID = -1007874653930162262L;

	private static final Map<String, UriTemplate> CACHE = new ConcurrentReferenceHashMap<>();

	private final TemplateVariables variables;
	private String toString;
	private String baseUri;
	private transient UriBuilderFactory factory;

	/**
	 * Creates a new {@link UriTemplate} using the given template string.
	 *
	 * @param template must not be {@literal null} or empty.
	 */
	private UriTemplate(String template) {

		Assert.hasText(template, "Template must not be null or empty!");

		Matcher matcher = VARIABLE_REGEX.matcher(template);
		int baseUriEndIndex = template.length();
		List<TemplateVariable> variables = new ArrayList<>();

		while (matcher.find()) {

			int start = matcher.start(0);

			VariableType type = VariableType.from(matcher.group(1));
			String[] names = matcher.group(2).split(",");

			for (String name : names) {

				TemplateVariable variable;

				if (name.endsWith(VariableType.COMPOSITE_PARAM.toString())) {
					variable = new TemplateVariable(name.substring(0, name.length() - 1), VariableType.COMPOSITE_PARAM);
				} else {
					variable = new TemplateVariable(name, type);
				}

				if (!variable.isRequired() && start < baseUriEndIndex) {
					baseUriEndIndex = start;
				}

				variables.add(variable);
			}
		}

		this.variables = variables.isEmpty() ? TemplateVariables.NONE : new TemplateVariables(variables);
		this.baseUri = template.substring(0, baseUriEndIndex);
		this.factory = createFactory(baseUri);
	}

	/**
	 * Creates a new {@link UriTemplate} from the given base URI and {@link TemplateVariables}.
	 *
	 * @param baseUri must not be {@literal null} or empty.
	 * @param variables must not be {@literal null}.
	 */
	UriTemplate(String baseUri, TemplateVariables variables) {

		Assert.hasText(baseUri, "Base URI must not be null or empty!");
		Assert.notNull(variables, "Template variables must not be null!");

		this.baseUri = baseUri;
		this.variables = variables;
	}

	/**
	 * Returns a {@link UriTemplate} for the given {@link String} template.
	 *
	 * @param template must not be {@literal null} or empty.
	 * @return
	 */
	public static UriTemplate of(String template) {

		Assert.hasText(template, "Template must not be null or empty!");

		return CACHE.computeIfAbsent(template, UriTemplate::new);
	}

	/**
	 * Returns a {@link UriTemplate} for the given {@link String} template.
	 *
	 * @param template must not be {@literal null} or empty.
	 * @return
	 */
	public static UriTemplate of(String template, TemplateVariables variables) {

		Assert.hasText(template, "Template must not be null or empty!");

		return CACHE.computeIfAbsent(template, UriTemplate::new).with(variables);
	}

	/**
	 * Creates a new {@link UriTemplate} with the current {@link TemplateVariable}s augmented with the given ones.
	 *
	 * @param variables must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public UriTemplate with(TemplateVariables variables) {

		Assert.notNull(variables, "TemplateVariables must not be null!");

		if (variables.equals(TemplateVariables.NONE)) {
			return this;
		}

		UriComponents components = UriComponentsBuilder.fromUriString(baseUri).build();
		List<TemplateVariable> result = new ArrayList<>();

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
	 * Creates a new {@link UriTemplate} with the given {@link TemplateVariable} added.
	 *
	 * @param variable must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public UriTemplate with(TemplateVariable variable) {

		Assert.notNull(variable, "Template variable must not be null!");

		return with(new TemplateVariables(variable));
	}

	/**
	 * Creates a new {@link UriTemplate} with a {@link TemplateVariable} with the given name and type added.
	 *
	 * @param variableName must not be {@literal null} or empty.
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public UriTemplate with(String variableName, TemplateVariable.VariableType type) {
		return with(new TemplateVariables(new TemplateVariable(variableName, type)));
	}

	/**
	 * Returns whether the given candidate is a URI template.
	 *
	 * @param candidate
	 * @return
	 */
	public static boolean isTemplate(String candidate) {

		return StringUtils.hasText(candidate) //
				? VARIABLE_REGEX.matcher(candidate).find()
				: false;
	}

	/**
	 * Returns the {@link TemplateVariable}s discovered.
	 *
	 * @return
	 */
	public List<TemplateVariable> getVariables() {
		return variables.asList();
	}

	/**
	 * Returns the names of the variables discovered.
	 *
	 * @return
	 */
	public List<String> getVariableNames() {

		return variables.asList().stream() //
				.map(TemplateVariable::getName) //
				.collect(Collectors.toList());
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

		UriBuilder builder = factory.uriString(baseUri);
		Iterator<Object> iterator = Arrays.asList(parameters).iterator();

		variables.asList().stream() //
				.filter(TemplateVariable::isRequired)//
				.filter(__ -> iterator.hasNext()) //
				.forEach(__ -> iterator.next());

		for (TemplateVariable variable : getOptionalVariables()) {

			Object value = iterator.hasNext() ? iterator.next() : null;
			appendToBuilder(builder, variable, value);
		}

		return builder.build(parameters);
	}

	/**
	 * Expands the {@link UriTemplate} using the given parameters.
	 *
	 * @param parameters must not be {@literal null}.
	 * @return
	 */
	public URI expand(Map<String, ?> parameters) {

		Assert.notNull(parameters, "Parameters must not be null!");

		if (TemplateVariables.NONE.equals(variables)) {
			return URI.create(baseUri);
		}

		UriBuilder builder = factory.uriString(baseUri);

		for (TemplateVariable variable : getOptionalVariables()) {
			appendToBuilder(builder, variable, parameters.get(variable.getName()));
		}

		return builder.build(parameters);
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
	@Override
	public String toString() {

		if (toString == null) {

			UriComponents components = UriComponentsBuilder.fromUriString(baseUri).build();
			boolean hasQueryParameters = !components.getQueryParams().isEmpty();

			this.toString = baseUri + getOptionalVariables().toString(hasQueryParameters);
		}

		return toString;
	}

	private TemplateVariables getOptionalVariables() {

		return variables.asList().stream() //
				.filter(variable -> !variable.isRequired()) //
				.collect(Collectors.collectingAndThen(Collectors.toList(), TemplateVariables::new));
	}

	/**
	 * Creates a {@link UriBuilderFactory} that might optionally encode the given base URI if it still needs to be
	 * encoded.
	 *
	 * @param baseUri must not be {@literal null} or empty.
	 * @return
	 */
	private static UriBuilderFactory createFactory(String baseUri) {

		EncodingMode mode = UriUtils.decode(baseUri, StandardCharsets.UTF_8).length() < baseUri.length() //
				? EncodingMode.VALUES_ONLY //
				: EncodingMode.TEMPLATE_AND_VALUES;

		DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
		factory.setEncodingMode(mode);

		return factory;
	}

	/**
	 * Appends the value for the given {@link TemplateVariable} to the given {@link UriComponentsBuilder}.
	 *
	 * @param builder must not be {@literal null}.
	 * @param variable must not be {@literal null}.
	 * @param value can be {@literal null}.
	 */
	private static void appendToBuilder(UriBuilder builder, TemplateVariable variable, @Nullable Object value) {

		if (value == null) {

			if (variable.isRequired()) {
				throw new IllegalArgumentException(
						String.format("Template variable %s is required but no value was given!", variable.getName()));
			}

			return;
		}

		switch (variable.getType()) {
			case COMPOSITE_PARAM:
				appendComposite(builder, variable.getName(), value);
				break;
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

	/**
	 * Expand what could be a single value, a {@link List}, or a {@link Map}.
	 *
	 * @param builder
	 * @param name
	 * @param value
	 * @see https://tools.ietf.org/html/rfc6570#section-2.4.2
	 */
	@SuppressWarnings("unchecked")
	private static void appendComposite(UriBuilder builder, String name, Object value) {

		if (value instanceof Iterable) {

			((Iterable<?>) value).forEach(it -> builder.queryParam(name, it));

		} else if (value instanceof Map) {

			((Map<Object, Object>) value).forEach((key, value1) -> builder.queryParam(key.toString(), value1));

		} else {

			builder.queryParam(name, value);
		}
	}

	/**
	 * Recreate {@link UriBuilderFactory} on deserialization.
	 *
	 * @param in
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

		in.defaultReadObject();

		this.factory = createFactory(baseUri);
	}
}
