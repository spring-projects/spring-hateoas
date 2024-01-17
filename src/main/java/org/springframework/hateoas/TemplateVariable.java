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

import static org.springframework.hateoas.TemplateVariable.VariableType.*;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriUtils;

/**
 * A single template variable.
 *
 * @author Oliver Gierke
 * @author JamesE Richardson
 */
public final class TemplateVariable implements Serializable, UriTemplate.Expandable {

	private static final long serialVersionUID = -2731446749851863774L;

	private final String name;
	private final TemplateVariable.VariableType type;
	private final String description;
	private final Cardinality cardinality;
	private final int limit;

	/**
	 * Creates a new {@link TemplateVariable} with the given name and type.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @param type must not be {@literal null}.
	 */
	public TemplateVariable(String name, TemplateVariable.VariableType type) {
		this(name, type, "");
	}

	public TemplateVariable(String name, TemplateVariable.VariableType type, String description) {
		this(name, type, description, Cardinality.SINGULAR, -1);
	}

	TemplateVariable(String name, TemplateVariable.VariableType type, String description,
			Cardinality cardinality) {
		this(name, type, description, cardinality, -1);
	}

	/**
	 * Creates a new {@link TemplateVariable} with the given name, type and description.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @param type must not be {@literal null}.
	 * @param description must not be {@literal null}.
	 * @since 1.4
	 */
	TemplateVariable(String name, TemplateVariable.VariableType type, String description,
			Cardinality cardinality, int limit) {

		Assert.hasText(name, "Variable name must not be null or empty!");
		Assert.notNull(type, "Variable type must not be null!");
		Assert.notNull(description, "Description must not be null!");
		Assert.notNull(cardinality, "Cardinality must not be null!");

		this.name = name;
		this.type = type;
		this.description = description;
		this.cardinality = cardinality;
		this.limit = limit;
	}

	/**
	 * Static helper to fashion {@link VariableType#PATH_VARIABLE} variables.
	 *
	 * @param variable must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	public static TemplateVariable pathVariable(String variable) {
		return new TemplateVariable(variable, VariableType.PATH_VARIABLE);
	}

	/**
	 * Static helper to fashion {@link VariableType#REQUEST_PARAM} variables.
	 *
	 * @param parameter must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	public static TemplateVariable requestParameter(String parameter) {
		return new TemplateVariable(parameter, VariableType.REQUEST_PARAM);
	}

	/**
	 * Static helper to fashion {@link VariableType#REQUEST_PARAM_CONTINUED} variables.
	 *
	 * @param parameter must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	public static TemplateVariable requestParameterContinued(String parameter) {
		return new TemplateVariable(parameter, VariableType.REQUEST_PARAM_CONTINUED);
	}

	/**
	 * Static helper to fashion {@link VariableType#SEGMENT} variables.
	 *
	 * @param segment must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	public static TemplateVariable segment(String segment) {
		return new TemplateVariable(segment, VariableType.SEGMENT);
	}

	/**
	 * Static helper to fashion {@link VariableType#FRAGMENT} variables.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 */
	public static TemplateVariable fragment(String name) {
		return new TemplateVariable(name, VariableType.FRAGMENT);
	}

	public static TemplateVariable reservedString(String name) {
		return new TemplateVariable(name, VariableType.RESERVED_STRING);
	}

	/**
	 * Static helper to fashion {@link VariableType#COMPOSITE_PARAM} variables.
	 *
	 * @param parameter must not be {@literal null} or empty.
	 * @return
	 * @since 1.1
	 * @deprecated since 1.4, use actual parameter type and call {@link #composite()} on the instance instead.
	 */
	@Deprecated
	public static TemplateVariable compositeParameter(String parameter) {
		return new TemplateVariable(parameter, VariableType.COMPOSITE_PARAM);
	}

	/**
	 * Marks the current template variable as composite value.
	 *
	 * @return
	 * @since 1.4
	 */
	public TemplateVariable composite() {
		return isComposite() ? this : new TemplateVariable(name, type, description, Cardinality.COMPOSITE, limit);
	}

	/**
	 * Marks the current template variable as singular value.
	 *
	 * @return
	 * @since 1.4
	 */
	public TemplateVariable singular() {
		return isSingular() ? this : new TemplateVariable(name, type, description, Cardinality.SINGULAR, limit);
	}

	public TemplateVariable limit(int limit) {
		return new TemplateVariable(name, type, description, cardinality, limit);
	}

	/**
	 * Returns whether the current {@link TemplateVariable} is a composite one.
	 *
	 * @return
	 * @since 1.4
	 */
	public boolean isComposite() {
		return cardinality.equals(Cardinality.COMPOSITE);
	}

	/**
	 * Returns whether the current {@link TemplateVariable} is a singular one.
	 *
	 * @return
	 * @since 1.4
	 */
	public boolean isSingular() {
		return cardinality.equals(Cardinality.SINGULAR);
	}

	String fakeName() {
		return String.format("{_____%s_____}", name);
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
	 * @deprecated since 1.4. No replacement as template variables are never required actually.
	 */
	@Deprecated
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

	TemplateVariable withType(VariableType type) {
		return new TemplateVariable(name, type, description, cardinality, limit);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return StringUtils.hasText(description) ? asString() + " - " + description : asString();
	}

	public String asString() {

		return "{" + type.toString() + essence() + "}";
	}

	String essence() {

		String result = name;
		result += limit != -1 ? ":" + limit : "";
		result += isComposite() ? "*" : "";

		return result;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Returns the type of the {@link TemplateVariable}.
	 *
	 * @return will never be {@literal null}.
	 */
	public VariableType getType() {
		return this.type;
	}

	public String getDescription() {
		return this.description;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.UriTemplate.Expandable#expand(org.springframework.web.util.UriBuilder, java.util.Map)
	 */
	@Nullable
	@Override
	public String expand(Map<String, ?> parameters) {

		Object value = parameters.get(name);

		if (value == null) {
			return null;
		}

		return handleComposite(prepareAndEncode(value));

	}

	@Nullable
	@SuppressWarnings("unchecked")
	public String prepareAndEncode(@Nullable Object value) {

		if (value == null) {
			return null;
		}

		String separator = isComposite() ? type.combiner : DEFAULT_SEPARATOR;

		if (value instanceof Iterable) {

			Iterable<?> source = (Iterable<?>) value;

			if (!source.iterator().hasNext()) {
				return null;
			}

			return StreamSupport.stream(source.spliterator(), false)
					.map(it -> prepareElement(it, false))
					.collect(Collectors.joining(separator));

		} else if (value instanceof Map) {

			String keyValueSeparator = isComposite() ? "=" : DEFAULT_SEPARATOR;

			return ((Map<Object, Object>) value).entrySet().stream()
					.map(it -> it.getKey().toString().concat(keyValueSeparator).concat(prepareElement(it.getValue(), true)))
					.collect(Collectors.joining(separator));

		} else {
			return prepareElement(value, false);
		}
	}

	@Nullable
	private String prepareElement(Object value, boolean forMap) {

		String encoded = limitAndEncode(value);

		if (encoded == null) {
			return null;
		}

		switch (type) {
			case REQUEST_PARAM:
			case REQUEST_PARAM_CONTINUED:
			case PATH_STYLE_PARAMETER:
				return isComposite() && !forMap ? name.concat("=").concat(encoded) : encoded;
			default:
				return encoded;
		}
	}

	@Nullable
	private String limitAndEncode(@Nullable Object value) {

		if (value == null) {
			return null;
		}

		String source = value.toString();

		if (limit != -1 && limit < source.length()) {
			source = source.substring(0, limit);
		}

		return type.encode(source);
	}

	@Nullable
	private String handleComposite(@Nullable String value) {

		if (value == null) {
			return null;
		}

		switch (type) {
			case REQUEST_PARAM:
			case REQUEST_PARAM_CONTINUED:

				if (isComposite()) {
					return value;
				}

				return name.concat("=").concat(value);

			case PATH_STYLE_PARAMETER:

				if (isComposite()) {
					return value;
				}

				return StringUtils.hasText(value)
						? name.concat("=").concat(value)
						: name;

			default:
				return value;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TemplateVariable that = (TemplateVariable) o;

		return Objects.equals(this.name, that.name) //
				&& this.type == that.type
				&& this.limit == that.limit
				&& this.cardinality == that.cardinality
				&& Objects.equals(this.description, that.description);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.type, this.description);
	}

	/**
	 * An enumeration for all supported variable types.
	 *
	 * @author Oliver Gierke
	 */
	public enum VariableType {

		SIMPLE("", ",", false), //

		/**
		 * @deprecated since 1.4, use {@link #SIMPLE} instead.
		 */
		@Deprecated
		PATH_VARIABLE("", ",", true), //
		RESERVED_STRING("+", ",", true), //
		DOT(".", ".", true), //
		REQUEST_PARAM("?", "&", true), //
		REQUEST_PARAM_CONTINUED("&", "&", true), //

		PATH_SEGMENT("/", "/", true), //

		/**
		 * @deprecated since 1.4, use {@link #PATH_SEGMENT} instead.
		 */
		@Deprecated
		SEGMENT("/", "/", true), //
		PATH_STYLE_PARAMETER(";", ";", true), //
		FRAGMENT("#", ",", true), //

		/**
		 * @deprecated since 1.4. Use the actual type and call {@link TemplateVariable#composite()}.
		 */
		COMPOSITE_PARAM("*", "", true);

		private static final EnumSet<VariableType> COMBINABLE_TYPES = EnumSet.of(REQUEST_PARAM, REQUEST_PARAM_CONTINUED);
		static final String DEFAULT_SEPARATOR = ",";

		private final String key, combiner;
		private final boolean optional;

		VariableType(String key, String combiner, boolean optional) {

			this.key = key;
			this.combiner = combiner;
			this.optional = optional;
		}

		public String encode(String value) {

			switch (this) {
				case DOT:
				case SEGMENT:
				case PATH_SEGMENT:
				case PATH_STYLE_PARAMETER:
				case REQUEST_PARAM:
				case REQUEST_PARAM_CONTINUED:
				case SIMPLE:
					return UriUtils.encode(value, StandardCharsets.UTF_8);
				case FRAGMENT:
				default:
					return UriUtils.encodePath(value, StandardCharsets.UTF_8);
			}
		}

		/**
		 * Returns whether the variable of this type is optional.
		 *
		 * @return
		 */
		public boolean isOptional() {
			return optional;
		}

		String join(Collection<String> values) {

			if (values.isEmpty()) {
				return "";
			}

			String prefix = this.equals(RESERVED_STRING) ? "" : key;

			return values.stream()
					.collect(Collectors.joining(combiner, prefix, ""));
		}

		boolean canBeCombinedWith(VariableType type) {
			return this.equals(type) || COMBINABLE_TYPES.contains(this) && COMBINABLE_TYPES.contains(type);
		}

		int findIndexWithin(String template) {
			return template.indexOf(key);
		}

		Stream<VariableType> getFollowingTypes() {

			return switch (this) {
				case PATH_SEGMENT -> Stream.of(PATH_STYLE_PARAMETER, REQUEST_PARAM, FRAGMENT);
				case PATH_STYLE_PARAMETER -> Stream.of(REQUEST_PARAM, FRAGMENT);
				case REQUEST_PARAM, REQUEST_PARAM_CONTINUED -> Stream.of(FRAGMENT);
				default -> Stream.<VariableType> empty();
			};
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

	/**
	 * The cardinality of the {@link TemplateVariable}.
	 *
	 * @author Oliver Drotbohm
	 * @since 1.4
	 * @see <a href=
	 *      "https://datatracker.ietf.org/doc/html/rfc6570#section-2.4.2">https://datatracker.ietf.org/doc/html/rfc6570#section-2.4.2</a>
	 */
	public enum Cardinality {
		SINGULAR, COMPOSITE;
	}
}
