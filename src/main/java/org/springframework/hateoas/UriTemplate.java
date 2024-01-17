/*
 * Copyright 2014-2024 the original author or authors.
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

import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
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

	private static final Pattern VARIABLE_REGEX = Pattern
			.compile("\\{([\\?\\&#/\\.\\+\\;]?)([\\w\\.(\\:\\d+)*%\\,*]+)\\}");
	private static final Pattern ELEMENT_REGEX = Pattern.compile("([\\w\\.\\%]+)(\\:\\d+)?(\\*)?");
	private static final long serialVersionUID = -1007874653930162262L;

	private final TemplateVariables variables;
	private final ExpandGroups groups;
	private final String baseUri, template;

	/**
	 * Creates a new {@link UriTemplate} using the given template string.
	 *
	 * @param template must not be {@literal null} or empty.
	 */
	private UriTemplate(String template) {

		Assert.hasText(template, "Template must not be null or empty!");

		int firstCurlyBraceIndex = template.indexOf('{');
		template = prepareTemplate(template, firstCurlyBraceIndex);
		String baseUri = template;

		List<TemplateVariable> variables = new ArrayList<>();
		List<ExpandGroup> expandGroups = new ArrayList<>();

		if (firstCurlyBraceIndex != -1) {

			Matcher matcher = VARIABLE_REGEX.matcher(template);

			while (matcher.find()) {

				String typeFlag = matcher.group(1);
				String[] segments = matcher.group(2).split(",");
				VariableType type = VariableType.from(typeFlag);
				List<TemplateVariable> variableGroup = new ArrayList<>();

				for (String segment : segments) {

					Matcher inner = ELEMENT_REGEX.matcher(segment);

					while (inner.find()) {

						String name = inner.group(1);
						String limit = inner.group(2);
						String composite = inner.group(3);

						TemplateVariable variable = new TemplateVariable(name, type);

						variable = StringUtils.hasText(composite) ? variable.composite() : variable;
						variable = StringUtils.hasText(limit) ? variable.limit(Integer.valueOf(limit.substring(1))) : variable;

						variableGroup.add(variable);
						variables.add(variable);
					}
				}

				expandGroups.add(new ExpandGroup(variableGroup));
			}
		}

		this.variables = variables.isEmpty() ? TemplateVariables.NONE : new TemplateVariables(variables);
		this.groups = new ExpandGroups(expandGroups);
		this.baseUri = baseUri;
		this.template = template;
	}

	/**
	 * Creates a new {@link UriTemplate} from the given base URI, {@link TemplateVariables} and {@link UriBuilderFactory}.
	 *
	 * @param baseUri must not be {@literal null} or empty.
	 * @param variables must not be {@literal null}.
	 */
	private UriTemplate(String baseUri, String template, TemplateVariables variables, ExpandGroups groups) {

		Assert.hasText(baseUri, "Base URI must not be null or empty!");
		Assert.notNull(variables, "Template variables must not be null!");

		this.baseUri = baseUri;
		this.variables = variables;
		this.groups = groups;
		this.template = template;
	}

	/**
	 * Returns a {@link UriTemplate} for the given {@link String} template.
	 *
	 * @param template must not be {@literal null} or empty.
	 * @return
	 */
	public static UriTemplate of(String template) {

		Assert.hasText(template, "Template must not be null or empty!");

		return new UriTemplate(template);
	}

	/**
	 * Returns a {@link UriTemplate} for the given {@link String} template.
	 *
	 * @param template must not be {@literal null} or empty.
	 * @return
	 */
	public static UriTemplate of(String template, TemplateVariables variables) {

		Assert.hasText(template, "Template must not be null or empty!");

		return new UriTemplate(template).with(variables);
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
		MultiValueMap<String, String> parameters = components.getQueryParams();
		List<TemplateVariable> result = new ArrayList<>();

		for (TemplateVariable variable : variables) {

			boolean isRequestParam = variable.isRequestParameterVariable();
			boolean alreadyPresent = parameters.containsKey(variable.getName());

			if (isRequestParam && alreadyPresent) {
				continue;
			}

			if (variable.isFragment() && StringUtils.hasText(components.getFragment())) {
				continue;
			}

			// Use request parameter continuation if base contains parameters already
			if (!parameters.isEmpty() && variable.getType().equals(VariableType.REQUEST_PARAM)) {
				variable = variable.withType(VariableType.REQUEST_PARAM_CONTINUED);
			}

			result.add(variable);
		}

		String newOriginal = template;
		ExpandGroups groups = this.groups;

		MultiValueMap<VariableType, TemplateVariable> groupedByVariableType = new LinkedMultiValueMap<>();

		for (TemplateVariable templateVariable : result) {
			groupedByVariableType.add(templateVariable.getType(), templateVariable);
		}

		for (Entry<VariableType, List<TemplateVariable>> entry : groupedByVariableType.entrySet()) {

			ExpandGroup existing = groups.findLastExpandGroupOfType(entry.getKey());
			ExpandGroup group = new ExpandGroup(entry.getValue());

			if (existing != null) {
				group = existing.merge(group);
				newOriginal = newOriginal.replace(existing.asString(), group.asString());
			} else {
				newOriginal = group.insertInto(newOriginal);
			}

			groups = groups.addOrAugment(group);
		}

		return new UriTemplate(baseUri, newOriginal, this.variables.concat(result), groups);
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

		Iterator<Object> iterator = Arrays.asList(parameters).iterator();
		Map<String, Object> foo = new HashMap<>();

		variables.stream()
				.map(TemplateVariable::getName)
				.forEach(it -> {

					Object value = iterator.hasNext() ? iterator.next() : null;
					foo.put(it, value);
				});

		return expand(foo);
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

		String result = template;

		for (ExpandGroup group : groups.groupList) {
			result = result.replace(group.asString(), group.expand(parameters));
		}

		return URI.create(result);
	}

	interface Expandable {

		@Nullable
		String expand(Map<String, ?> parameters);

		String asString();
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
		return template;
	}

	private static String prepareTemplate(String template, int index) {

		String decodedTemplate = UriUtils.decode(template, StandardCharsets.UTF_8);

		if (decodedTemplate.length() != template.length()) {
			return template;
		}

		String head = index == -1 ? template : template.substring(0, index);
		String tail = index == -1 ? "" : template.substring(index);

		// Encode head if it's more than just the scheme
		String encodedBase = head.endsWith("://") && tail.startsWith("{")
				? head
				: UriComponentsBuilder.fromUriString(head)
						.encode()
						.build()
						.toUriString();

		head = encodedBase.length() > head.length() ? encodedBase : head;

		return head + tail;
	}

	private static class ExpandGroups implements Serializable {

		private static final long serialVersionUID = 6260926152179514011L;

		private final List<ExpandGroup> groupList;

		public ExpandGroups(List<ExpandGroup> groups) {
			this.groupList = groups;
		}

		public ExpandGroups addOrAugment(ExpandGroup group) {

			ExpandGroup existing = findLastExpandGroupOfType(group.type);
			List<ExpandGroup> foo = new ArrayList<>(groupList);

			if (existing == null) {

				foo.add(group);

				return new ExpandGroups(foo);
			}

			ExpandGroup merged = existing.merge(group);

			foo.remove(existing);
			foo.add(merged);

			return new ExpandGroups(foo);
		}

		@Nullable
		ExpandGroup findLastExpandGroupOfType(VariableType type) {

			ExpandGroup result = null;

			for (ExpandGroup entry : groupList) {
				if (entry.canBeCombinedWith(type)) {
					result = entry;
				}
			}

			return result;
		}
	}

	private static class ExpandGroup implements Expandable, Serializable {

		private static final long serialVersionUID = -6057608202572953271L;

		private final TemplateVariables variables;
		private final VariableType type;

		public ExpandGroup(List<TemplateVariable> variables) {
			this(new TemplateVariables(variables));
		}

		ExpandGroup(TemplateVariables variables) {

			this.variables = variables;
			this.type = variables.asList().get(0).getType();
		}

		ExpandGroup merge(ExpandGroup group) {

			Assert.isTrue(this.type.canBeCombinedWith(group.type), "Incompatible expand groups!");

			return new ExpandGroup(variables.concat(group.variables));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.UriTemplate.Expandable#expand(org.springframework.web.util.UriBuilder, java.util.Map)
		 */
		@Nullable
		@Override
		public String expand(Map<String, ?> parameters) {

			return type.join(variables.stream()
					.map(it -> it.expand(parameters))
					.filter(it -> it != null)
					.collect(Collectors.toList()));
		}

		boolean canBeCombinedWith(VariableType type) {
			return this.type.canBeCombinedWith(type);
		}

		/**
		 * Inserts the current {@link ExpandGroup} into the given URI template.
		 *
		 * @param template must not be {@literal null} or empty.
		 * @return will never be {@literal null}.
		 */
		String insertInto(String template) {

			return type.getFollowingTypes().map(it -> it.findIndexWithin(template))
					.filter(it -> it != -1)
					.findFirst()
					.map(it -> template.substring(0, it) + toString() + template.substring(it))
					.orElseGet(() -> template.concat(toString()));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.UriTemplate.Expandable#asString()
		 */
		@Override
		public String asString() {

			return variables.stream().map(TemplateVariable::essence)
					.collect(Collectors.joining(",", "{".concat(type.toString()), "}"));
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return asString();
		}
	}
}
