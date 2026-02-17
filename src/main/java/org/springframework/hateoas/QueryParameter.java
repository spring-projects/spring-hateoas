/*
 * Copyright 2018-2024 the original author or authors.
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

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Representation of a web request's query parameter (https://example.com?name=foo) => {"name", "foo", true}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
public final class QueryParameter {

	private final String name;
	private final @Nullable String value;
	private final boolean required;
	private final boolean exploded; // RFC6570 explode modifier support

	private QueryParameter(String name, @Nullable String value, boolean required) {
		this(name, value, required, false);
	}

	private QueryParameter(String name, @Nullable String value, boolean required, boolean exploded) {

		this.name = name;
		this.value = value;
		this.required = required;
		this.exploded = exploded;
	}

	/**
	 * Creates a new {@link QueryParameter} from the given {@link MethodParameter}.
	 * Supports both {@link RequestParam} and {@link ModelAttribute} annotations.
	 *
	 * @param parameter must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static QueryParameter of(MethodParameter parameter) {

		// Check for @RequestParam first (existing behavior)
		MergedAnnotation<RequestParam> requestParamAnnotation = MergedAnnotations //
				.from(parameter.getParameter()) //
				.get(RequestParam.class);

		if (requestParamAnnotation.isPresent()) {
			return createFromRequestParam(parameter, requestParamAnnotation);
		}

		// Check for @ModelAttribute
		MergedAnnotation<ModelAttribute> modelAttributeAnnotation = MergedAnnotations //
				.from(parameter.getParameter()) //
				.get(ModelAttribute.class);

		if (modelAttributeAnnotation.isPresent()) {
			return createFromModelAttribute(parameter, modelAttributeAnnotation);
		}

		// Check for implicit @ModelAttribute (when parameter is a complex object and no other annotations)
		if (isImplicitModelAttribute(parameter)) {
			return createFromImplicitModelAttribute(parameter);
		}

		// Fallback to original logic for backward compatibility
		return createFromRequestParam(parameter, requestParamAnnotation);
	}

	private static QueryParameter createFromRequestParam(MethodParameter parameter, MergedAnnotation<RequestParam> annotation) {

		String name = annotation.isPresent() && annotation.hasNonDefaultValue("name") //
				? annotation.getString("name") //
				: parameter.getParameterName();

		if (name == null || !StringUtils.hasText(name)) {
			throw new IllegalStateException(String.format("Couldn't determine parameter name for %s!", parameter));
		}

		boolean required = annotation.isPresent() && annotation.hasNonDefaultValue("required") //
				? annotation.getBoolean("required") //
				: !Optional.class.equals(parameter.getParameterType()); //

		return required ? required(name) : optional(name);
	}

	private static QueryParameter createFromModelAttribute(MethodParameter parameter, MergedAnnotation<ModelAttribute> annotation) {

		String name = annotation.hasNonDefaultValue("name") //
				? annotation.getString("name") //
				: parameter.getParameterName();

		if (name == null || !StringUtils.hasText(name)) {
			throw new IllegalStateException(String.format("Couldn't determine parameter name for %s!", parameter));
		}

		// @ModelAttribute parameters are typically required unless they're Optional
		boolean required = !Optional.class.equals(parameter.getParameterType());

		// ModelAttribute represents composite values, so mark as exploded for RFC6570
		return required ? requiredExploded(name) : optionalExploded(name);
	}

	private static QueryParameter createFromImplicitModelAttribute(MethodParameter parameter) {

		String name = parameter.getParameterName();

		if (name == null || !StringUtils.hasText(name)) {
			throw new IllegalStateException(String.format("Couldn't determine parameter name for %s!", parameter));
		}

		boolean required = !Optional.class.equals(parameter.getParameterType());

		// Implicit ModelAttribute also represents composite values
		return required ? requiredExploded(name) : optionalExploded(name);
	}

	private static boolean isImplicitModelAttribute(MethodParameter parameter) {
		Class<?> parameterType = parameter.getParameterType();

		// Simple types are not implicit @ModelAttribute
		if (isSimpleValueType(parameterType)) {
			return false;
		}

		// Check if it's annotated with other Spring MVC annotations
		MergedAnnotations annotations = MergedAnnotations.from(parameter.getParameter());

		return !annotations.isPresent(RequestParam.class) &&
			   !annotations.isPresent(org.springframework.web.bind.annotation.RequestBody.class) &&
			   !annotations.isPresent(org.springframework.web.bind.annotation.PathVariable.class) &&
			   !annotations.isPresent(org.springframework.web.bind.annotation.RequestHeader.class) &&
			   !annotations.isPresent(org.springframework.web.bind.annotation.CookieValue.class);
	}

	private static boolean isSimpleValueType(Class<?> type) {
		return type.isPrimitive() ||
			   type == String.class ||
			   Number.class.isAssignableFrom(type) ||
			   type == Boolean.class ||
			   type.isEnum() ||
			   java.util.Date.class.isAssignableFrom(type) ||
			   java.time.temporal.Temporal.class.isAssignableFrom(type);
	}

	/**
	 * Creates a new required {@link QueryParameter} with the given name;
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return a new required QueryParameter instance
	 */
	public static QueryParameter required(String name) {

		Assert.hasText(name, "Name must not be null or empty!");

		return new QueryParameter(name, null, true);
	}

	/**
	 * Creates a new optional {@link QueryParameter} with the given name;
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return a new optional QueryParameter instance
	 */
	public static QueryParameter optional(String name) {
		return new QueryParameter(name, null, false);
	}

	/**
	 * Creates a new required {@link QueryParameter} with explode modifier for composite values.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return a new required QueryParameter instance with explode modifier
	 */
	public static QueryParameter requiredExploded(String name) {

		Assert.hasText(name, "Name must not be null or empty!");

		return new QueryParameter(name, null, true, true);
	}

	/**
	 * Creates a new optional {@link QueryParameter} with explode modifier for composite values.
	 *
	 * @param name must not be {@literal null} or empty.
	 * @return a new optional QueryParameter instance with explode modifier
	 */
	public static QueryParameter optionalExploded(String name) {
		return new QueryParameter(name, null, false, true);
	}

	/**
	 * Create a new {@link QueryParameter} by copying all attributes and applying the new {@literal value}.
	 *
	 * @param value the new value to apply
	 * @return a new QueryParameter instance with the updated value
	 */
	public QueryParameter withValue(@Nullable String value) {
		return this.value == value ? this : new QueryParameter(this.name, value, this.required, this.exploded);
	}

	public String getName() {
		return this.name;
	}

	@Nullable
	public String getValue() {
		return this.value;
	}

	public boolean isRequired() {
		return this.required;
	}

	public boolean isExploded() {
		return this.exploded;
	}

	@Override
	public boolean equals(@Nullable Object o) {

		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		QueryParameter that = (QueryParameter) o;
		return this.required == that.required && this.exploded == that.exploded && Objects.equals(this.name, that.name)
				&& Objects.equals(this.value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.name, this.value, this.required, this.exploded);
	}

	@Override
	public String toString() {
		return "QueryParameter(name=" + this.name + ", value=" + this.value + ", required=" + this.required + ", exploded=" + this.exploded + ")";
	}
}
