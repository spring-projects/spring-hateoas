/*
 * Copyright 2026 the original author or authors.
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

import java.beans.PropertyDescriptor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Detects the properties of a {@link ModelAttribute} that Spring MVC's {@literal WebDataBinder} will populate from
 * request parameters. Those are the properties that need to show up in a URI template as individual request parameters.
 * <p>
 * A property is considered bindable if it is writable (or a record component, as those are bound through the canonical
 * constructor) and resolves to a simple type or a {@link Collection} of such, as those are the ones representable as a
 * request parameter. Derived, read-only properties and nested objects are skipped.
 * <p>
 * This deliberately does not reuse {@literal mediatype.PropertyUtils}: that one answers which properties Jackson
 * <em>serializes</em> (honoring {@literal @JsonIgnore}, unwrapping {@link org.springframework.hateoas.EntityModel} and
 * friends, ignoring writability), which is a different question from which properties a {@literal WebDataBinder}
 * <em>binds</em>.
 *
 * @author Kim Tae Eun
 * @since 3.2
 * @see <a href="https://tools.ietf.org/html/rfc6570#section-3.2.8">RFC6570 - Form-Style Query Expansion</a>
 */
class ModelAttributeProperties {

	private static final Map<ResolvableType, List<String>> CACHE = new ConcurrentReferenceHashMap<>();

	private ModelAttributeProperties() {}

	/**
	 * Returns the names of all bindable properties of the given type, in alphabetical order. The type is keyed with its
	 * generics intact, so that {@code Form<String>} and {@code Form<LocalDate>} are told apart.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	static List<String> getPropertyNames(ResolvableType type) {

		Assert.notNull(type, "Type must not be null!");

		return CACHE.computeIfAbsent(type, ModelAttributeProperties::detectPropertyNames);
	}

	/**
	 * Introspects the given type for bindable property names.
	 *
	 * @param owner must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	private static List<String> detectPropertyNames(ResolvableType owner) {

		Class<?> type = owner.resolve();

		if (type == null) {
			return Collections.emptyList();
		}

		Set<String> recordComponents = detectRecordComponents(type);

		return List.copyOf(Arrays.stream(BeanUtils.getPropertyDescriptors(type)) //
				.filter(it -> !"class".equals(it.getName())) //
				.filter(it -> it.getReadMethod() != null) //
				.filter(it -> it.getWriteMethod() != null || recordComponents.contains(it.getName())) //
				.filter(it -> isBindable(it, owner)) //
				.map(PropertyDescriptor::getName) //
				.sorted() //
				.collect(Collectors.toList()));
	}

	/**
	 * Returns the names of the record components of the given type, or an empty {@link Set} if it is not a record.
	 * Record components are bound through the canonical constructor and thus do not expose a setter.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	private static Set<String> detectRecordComponents(Class<?> type) {

		if (!type.isRecord()) {
			return Collections.emptySet();
		}

		return Arrays.stream(type.getRecordComponents()) //
				.map(RecordComponent::getName) //
				.collect(Collectors.toSet());
	}

	/**
	 * Returns whether the given property can be represented as a request parameter, i.e. whether it resolves to a
	 * simple type or a {@link Collection} of such. Type variables are resolved against the owning type, so that a
	 * {@code T value} declared on {@code Form<T>} is judged by the type argument actually used.
	 *
	 * @param descriptor must not be {@literal null}.
	 * @param owner must not be {@literal null}.
	 * @return whether the property binds to a request parameter.
	 */
	private static boolean isBindable(PropertyDescriptor descriptor, ResolvableType owner) {

		ResolvableType type = ResolvableType.forType(descriptor.getReadMethod().getGenericReturnType(), owner);
		Class<?> resolved = type.resolve();

		if (resolved == null) {
			return false;
		}

		if (BeanUtils.isSimpleProperty(resolved)) {
			return true;
		}

		if (!Collection.class.isAssignableFrom(resolved)) {
			return false;
		}

		Class<?> elementType = type.asCollection().resolveGeneric(0);

		return elementType != null && BeanUtils.isSimpleProperty(elementType);
	}
}
