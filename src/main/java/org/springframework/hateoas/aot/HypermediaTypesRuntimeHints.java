/*
 * Copyright 2023-2024 the original author or authors.
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
import java.util.function.Predicate;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

/**
 * {@link RuntimeHintsRegistrar} to register Jackson model types for hypermedia types.
 *
 * @author Oliver Drotbohm
 */
class HypermediaTypesRuntimeHints implements RuntimeHintsRegistrar {

	private static final TypeFilter HAS_JACKSON_SUPER_TYPE_FILTER = new JacksonSuperTypeFilter();
	private static final TypeFilter IS_JACKSON_ANNOTATION_PRESENT_FILTER = new JacksonAnnotationPresentFilter();

	private final String hypermediaPackage;

	/**
	 * Creates a new {@link HypermediaTypesRuntimeHints} for the given package.
	 *
	 * @param hypermediaPackage the package to scan for types.
	 */
	HypermediaTypesRuntimeHints(String hypermediaPackage) {

		Assert.hasText(hypermediaPackage, "Package must not be null or empty!");

		this.hypermediaPackage = hypermediaPackage;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.aot.hint.RuntimeHintsRegistrar#registerHints(org.springframework.aot.hint.RuntimeHints, java.lang.ClassLoader)
	 */
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

		AotUtils.registerTypesForReflection(hypermediaPackage, hints.reflection(),
				HAS_JACKSON_SUPER_TYPE_FILTER, IS_JACKSON_ANNOTATION_PRESENT_FILTER);
	}

	static class JacksonAnnotationPresentFilter implements TypeFilter {

		private static final Predicate<String> IS_JACKSON_ANNOTATION = it -> it.startsWith("com.fasterxml.jackson");

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.type.filter.TypeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
		 */
		@Override
		public boolean match(MetadataReader reader, MetadataReaderFactory factory)
				throws IOException {

			var annotationMetadata = reader.getAnnotationMetadata();

			// Type annotations
			return annotationMetadata
					.getAnnotationTypes()
					.stream()
					.anyMatch(IS_JACKSON_ANNOTATION)

					// Method annotations
					|| annotationMetadata.getDeclaredMethods().stream()
							.flatMap(it -> it.getAnnotations().stream())
							.map(MergedAnnotation::getType)
							.map(Class::getName)
							.anyMatch(IS_JACKSON_ANNOTATION);
		}
	}

	static class JacksonSuperTypeFilter extends AbstractTypeHierarchyTraversingFilter {

		private static final String JACKSON_PACKAGE = "com.fasterxml.jackson";

		JacksonSuperTypeFilter() {
			super(true, true);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter#matchSuperClass(java.lang.String)
		 */
		@Override
		protected Boolean matchSuperClass(String superClassName) {
			return superClassName.startsWith(JACKSON_PACKAGE);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter#matchInterface(java.lang.String)
		 */
		@Override
		protected Boolean matchInterface(String interfaceName) {
			return interfaceName.startsWith(JACKSON_PACKAGE);
		}
	}
}
