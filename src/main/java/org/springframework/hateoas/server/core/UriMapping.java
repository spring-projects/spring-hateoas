/*
 * Copyright 2022-2024 the original author or authors.
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

import static org.springframework.web.util.UriComponents.UriTemplateVariables.*;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.hateoas.TemplateVariable;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.util.UriTemplate;

/**
 * A URI mapping on a controller method.
 *
 * @author Oliver Drotbohm
 */
public class UriMapping {

	private final String mapping;
	private final MappingVariables variables;

	/**
	 * Creates a new {@link UriMapping} from a given source mapping string.
	 *
	 * @param source can be {@literal null}.
	 * @return
	 */
	@Nullable
	public static UriMapping of(@Nullable String source) {

		if (source == null) {
			return null;
		}

		UriTemplate template = UriTemplateFactory.templateFor(source);
		MappingVariables mappingVariables = new MappingVariables(template);

		if (source.contains("{*")) {
			source = new PathCapturingMappingPreparer(mappingVariables).apply(source);
		}

		return new UriMapping(source, new MappingVariables(template));
	}

	/**
	 * Creates a new {@link UriMapping} for the given mapping and {@link MappingVariables}.
	 *
	 * @param mapping must not be {@literal null}.
	 * @param variables must not be {@literal null}.
	 */
	private UriMapping(String mapping, MappingVariables variables) {

		Assert.notNull(mapping, "Mapping must not be null!");
		Assert.notNull(variables, "MappingVariables must not be null!");

		this.mapping = mapping;
		this.variables = variables;
	}

	/**
	 * Returns the raw mapping.
	 *
	 * @return
	 */
	public String getMapping() {
		return mapping;
	}

	/**
	 * Returns all {@link MappingVariables} contained in the mapping.
	 *
	 * @return
	 */
	MappingVariables getMappingVariables() {
		return this.variables;
	}

	/**
	 * All {@link MappingVariable}s contained in a {@link UriTemplate}.
	 *
	 * @author Oliver Drotbohm
	 */
	static class MappingVariables implements Iterable<MappingVariable> {

		private final List<MappingVariable> variables;

		public MappingVariables(UriTemplate template) {
			this.variables = template.getVariableNames().stream().map(MappingVariable::of).collect(Collectors.toList());
		}

		public boolean hasCapturingVariable() {
			return variables.stream().anyMatch(it -> it.isCapturing());
		}

		/**
		 * Returns the {@link MappingVariable} with the given name.
		 *
		 * @param name must not be {@literal null} or empty.
		 * @return
		 * @throws IllegalArgumentException if no {@link MappingVariable} with the given name can be found.
		 */
		public MappingVariable getVariable(String name) {

			Assert.hasText(name, "Variable must not be null or empty!");

			return variables.stream()
					.filter(it -> it.hasName(name))
					.findFirst()
					.orElseThrow(() -> new IllegalArgumentException("No variable named " + name + " found!"));
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<MappingVariable> iterator() {
			return variables.iterator();
		}
	}

	/**
	 * A variable present in a Spring MVC controller mapping. These variables follow slightly different semantics than URI
	 * template variables. For example, a capturing pattern {@code {*…}} indicates all trailing path segments to be
	 * mapped.
	 *
	 * @author Oliver Drotbohm
	 */
	static class MappingVariable {

		private final String name;
		private final boolean composite;

		/**
		 * Creates a new {@link MappingVariable} from the original source name as used in the mapping.
		 *
		 * @param source must not be {@literal null} or empty.
		 * @return
		 */
		public static MappingVariable of(String source) {

			Assert.hasText(source, "Variable source must not be null or empty!");

			return source.startsWith("*") //
					? new MappingVariable(source.substring(1), true) //
					: new MappingVariable(source, false);
		}

		private MappingVariable(String name, boolean composite) {

			this.name = name;
			this.composite = composite;
		}

		/**
		 * Returns whether the variable has the given name.
		 *
		 * @param candidate must not be {@literal null} or empty.
		 * @return
		 */
		public boolean hasName(String candidate) {
			return name.equals(candidate);
		}

		/**
		 * Returns whether the variable is capturing one.
		 *
		 * @return
		 */
		public boolean isCapturing() {
			return composite;
		}

		/**
		 * Returns the key to be used for variable expansion.
		 *
		 * @return will never be {@literal null}.
		 */
		public String getKey() {
			return composite ? "__composite-" + name + "__" : name;
		}

		/**
		 * Returns the placeholder to be used when preparing the original mapping for capturing variables.
		 *
		 * @return will never be {@literal null}.
		 */
		public String getPlaceholder() {
			return "{" + getKey() + "}";
		}

		/**
		 * Returns a segment {@link TemplateVariable} for the current variable.
		 *
		 * @return will never be {@literal null}.
		 */
		public TemplateVariable toSegment() {
			return TemplateVariable.segment(name);
		}

		/**
		 * Returns the value to be used for expansion if the original value for it was absent.
		 *
		 * @return
		 */
		public Object getAbsentValue() {
			return composite ? TemplateVariable.segment(name).composite().toString() : SKIP_VALUE;
		}
	}

	/**
	 * A {@link Function} that replaces capture style mapping variables ({@code {*…}}) as well as their preceding slash
	 * with simple placeholder variables, so that they can be expanded using the composite segment variable in case of an
	 * absent input value.
	 *
	 * @author Oliver Drotbohm
	 */
	private static class PathCapturingMappingPreparer implements Function<String, String> {

		private static final Pattern PATH_CAPTURE = Pattern.compile("\\/\\{\\*(\\w+)\\}");

		private final MappingVariables variables;

		/**
		 * Creates a new {@link PathCapturingMappingPreparer} for the given {@link MappingVariables}.
		 *
		 * @param variables must not be {@literal null}.
		 */
		public PathCapturingMappingPreparer(MappingVariables variables) {

			Assert.notNull(variables, "MappingVariables must not be null!");

			this.variables = variables;
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.function.Function#apply(java.lang.Object)
		 */
		@Override
		@SuppressWarnings("null")
		public String apply(String source) {

			Matcher matcher = PATH_CAPTURE.matcher(source);

			while (matcher.find()) {

				MappingVariable variable = variables.getVariable(matcher.group(1));

				source = source.replace(matcher.group(0), variable.getPlaceholder());
			}

			return source;
		}
	}
}
