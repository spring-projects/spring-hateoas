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
package org.springframework.hateoas.aot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.ResolvableType;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.util.ClassUtils;

/**
 * Some helper classes to register types for reflection.
 *
 * @author Oliver Drotbohm
 * @since 2.0
 */
class AotUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(AotUtils.class);
	private static final List<Class<?>> MODEL_TYPES = List.of(EntityModel.class, CollectionModel.class);
	private static final Set<Class<?>> SEEN_TYPES = new HashSet<>();

	/**
	 * Registers domain types held in {@link EntityModel} and {@link CollectionModel}s for reflection.
	 *
	 * @param type must not be {@literal null}.
	 * @param reflection must not be {@literal null}.
	 * @param context must not be {@literal null}.
	 */
	public static void registerModelDomainTypesForReflection(ResolvableType type, ReflectionHints reflection,
			Class<?> context) {

		if (HttpEntity.class.isAssignableFrom(type.resolve(Object.class))) {
			registerModelDomainTypesForReflection(type.as(HttpEntity.class).getGeneric(0), reflection, context);
		}

		MODEL_TYPES.stream()
				.flatMap(it -> extractGenerics(it, type).stream())
				.forEach(it -> registerTypeForReflection(it, reflection, context));
	}

	/**
	 * Registers the given type for constructor and method invocation reflection.
	 *
	 * @param type must not be {@literal null}.
	 * @param reflection must not be {@literal null}.
	 * @param context must not be {@literal null}.
	 */
	public static void registerTypeForReflection(Class<?> type, ReflectionHints reflection, Class<?> context) {

		if (SEEN_TYPES.contains(type)) {
			return;
		}

		LOGGER.info("Registering {} for reflection (for {})", type.getName(), context.getName());

		reflection.registerType(type,
				MemberCategory.INVOKE_DECLARED_METHODS,
				MemberCategory.INTROSPECT_DECLARED_CONSTRUCTORS);

		SEEN_TYPES.add(type);
	}

	public static void registerTypesForReflection(String packageName, ReflectionHints reflection, TypeFilter... filters) {

		// Register RepresentationModel types for full reflection
		var provider = AotUtils.getScanner(packageName, filters);

		LOGGER.info("Registering Spring HATEOAS types in {} for reflection.", packageName);

		provider.findClasses()
				.sorted(Comparator.comparing(TypeReference::getName))
				.peek(type -> LOGGER.debug("> {}", type.getName()))
				.forEach(reference -> reflection.registerType(reference, //
						MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));
	}

	/**
	 * Extracts the generics from the given model type if the given {@link ResolvableType} is assignable.
	 *
	 * @param modelType must not be {@literal null}.
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	private static Optional<Class<?>> extractGenerics(Class<?> modelType, ResolvableType type) {

		if (!modelType.isAssignableFrom(type.resolve(Object.class))) {
			return Optional.empty();
		}

		var unresolved = type.as(modelType).getGeneric(0);
		var resolved = unresolved.resolve();

		if (resolved == null) {
			return Optional.empty();
		}

		var nested = MODEL_TYPES.stream()
				.filter(it -> it.isAssignableFrom(resolved))
				.toList();

		// No nested matches -> return original
		if (nested.isEmpty()) {
			return Optional.of(resolved);
		}

		return nested.stream()
				.flatMap(it -> extractGenerics(it, unresolved).stream())
				.findFirst();
	}

	public static FullTypeScanner getScanner(String packageName, TypeFilter... includeFilters) {

		var provider = new ClassPathScanningCandidateComponentProvider(false) {

			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				return super.isCandidateComponent(beanDefinition) || beanDefinition.getMetadata().isAbstract();
			}
		};

		var filters = new ArrayList<TypeFilter>();
		filters.add(new EnforcedPackageFilter(packageName));
		filters.add(new AssignableTypeFilter(Object.class));

		if (includeFilters.length == 0) {
			provider.addIncludeFilter(all(filters));
		}

		for (TypeFilter filter : includeFilters) {

			var includeFilterComponents = new ArrayList<>(filters);
			includeFilterComponents.add(filter);
			provider.addIncludeFilter(all(includeFilterComponents));
		}

		return () -> provider.findCandidateComponents(packageName).stream()
				.map(BeanDefinition::getBeanClassName)
				.map(TypeReference::of);
	}

	/**
	 * A {@link TypeFilter} to only match types <em>outside</em> the configured package. Usually used as exclude filter to
	 * limit scans to not find nested packages.
	 *
	 * @author Oliver Drotbohm
	 */
	static class EnforcedPackageFilter implements TypeFilter {

		private final String referencePackage;

		public EnforcedPackageFilter(String referencePackage) {
			this.referencePackage = referencePackage;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.type.filter.TypeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
		 */
		@Override
		public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
				throws IOException {
			return referencePackage
					.equals(ClassUtils.getPackageName(metadataReader.getClassMetadata().getClassName()));
		}
	}

	private static TypeFilter all(Collection<TypeFilter> filters) {

		return new TypeFilter() {

			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
					throws IOException {

				for (TypeFilter filter : filters) {
					if (!filter.match(metadataReader, metadataReaderFactory)) {
						return false;
					}
				}

				return true;
			}
		};
	}

	static interface FullTypeScanner {

		abstract Stream<TypeReference> findClasses();
	}
}
