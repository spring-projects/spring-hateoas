/*
 * Copyright 2022 the original author or authors.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.TypeReference;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A {@link BeanRegistrationAotProcessor} to register types that will be rendered by Jackson for reflection. The
 * registration will consider the media types activated via {@link EnableHypermediaSupport} but always register the core
 * HATEOAS package as well as the ones for ALPS and HTTP Error details.
 *
 * @author Oliver Drotbohm
 * @since 2.0
 */
class HypermediaTypeAotProcessor implements BeanRegistrationAotProcessor {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.aot.BeanRegistrationAotProcessor#processAheadOfTime(org.springframework.beans.factory.support.RegisteredBean)
	 */
	@Override
	public BeanRegistrationAotContribution processAheadOfTime(RegisteredBean registeredBean) {

		EnableHypermediaSupport annotation = AnnotatedElementUtils.findMergedAnnotation(registeredBean.getBeanClass(),
				EnableHypermediaSupport.class);

		if (annotation == null) {
			return null;
		}

		var fromConfig = Arrays.stream(annotation.type())
				.map(HypermediaType::getLocalPackageName);

		var mediaTypePackages = Stream.concat(fromConfig, Stream.of("alps", "problem"))
				.map("org.springframework.hateoas.mediatype."::concat);

		var packagesToScan = Stream.concat(Stream.of("org.springframework.hateoas"), mediaTypePackages).toList();

		return packagesToScan.isEmpty() ? null : new MediaTypeReflectionAotContribution(packagesToScan);
	}

	static class MediaTypeReflectionAotContribution implements BeanRegistrationAotContribution {

		private static final Logger LOGGER = LoggerFactory.getLogger(MediaTypeReflectionAotContribution.class);

		private final List<String> mediaTypePackage;
		private final Set<String> packagesSeen;

		/**
		 * Creates a new {@link MediaTypeReflectionAotContribution} for the given packages.
		 *
		 * @param mediaTypePackage must not be {@literal null}.
		 */
		public MediaTypeReflectionAotContribution(List<String> mediaTypePackage) {

			Assert.notNull(mediaTypePackage, "Media type packages must not be null!");

			this.mediaTypePackage = mediaTypePackage;
			this.packagesSeen = new HashSet<>();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.beans.factory.aot.BeanRegistrationAotContribution#applyTo(org.springframework.aot.generate.GenerationContext, org.springframework.beans.factory.aot.BeanRegistrationCode)
		 */
		@Override
		public void applyTo(GenerationContext generationContext, BeanRegistrationCode beanRegistrationCode) {

			var reflection = generationContext.getRuntimeHints().reflection();

			mediaTypePackage.forEach(it -> {

				if (packagesSeen.contains(it)) {
					return;
				}

				packagesSeen.add(it);

				// Register RepresentationModel types for full reflection
				FullTypeScanner provider = new FullTypeScanner();
				provider.addIncludeFilter(new JacksonAnnotationPresentFilter());
				provider.addIncludeFilter(new JacksonSuperTypeFilter());

				// Add filter to limit scan to sole package, not nested ones
				provider.addExcludeFilter(new EnforcedPackageFilter(it));

				LOGGER.info("Registering Spring HATEOAS types in {} for reflection.", it);

				provider.findCandidateComponents(it).stream()
						.map(BeanDefinition::getBeanClassName)
						.sorted()
						.peek(type -> LOGGER.debug("> {}", type))
						.map(TypeReference::of)
						.forEach(reference -> reflection.registerType(reference, //
								MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS));
			});
		}
	}

	static class FullTypeScanner extends ClassPathScanningCandidateComponentProvider {

		public FullTypeScanner() {
			super(false);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider#isCandidateComponent(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition)
		 */
		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			return true;
		}
	}

	/**
	 * A {@link TypeFilter} to only match types <em>outside</em> the configured package. Usually used as exclude filter
	 * to limit scans to not find nested packages.
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
			return !referencePackage
					.equals(ClassUtils.getPackageName(metadataReader.getClassMetadata().getClassName()));
		}
	}

	static abstract class TraversingTypeFilter implements TypeFilter {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.type.filter.TypeFilter#match(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
		 */
		@Override
		public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
				throws IOException {

			if (doMatch(metadataReader, metadataReaderFactory)) {
				return true;
			}

			var classMetadata = metadataReader.getClassMetadata();

			String superClassName = classMetadata.getSuperClassName();

			if (superClassName != null && !superClassName.startsWith("java")
					&& match(metadataReaderFactory.getMetadataReader(superClassName), metadataReaderFactory)) {
				return true;
			}

			for (String names : classMetadata.getInterfaceNames()) {

				MetadataReader reader = metadataReaderFactory.getMetadataReader(names);

				if (match(reader, metadataReaderFactory)) {
					return true;
				}
			}

			return false;
		}

		protected abstract boolean doMatch(MetadataReader reader, MetadataReaderFactory factory);
	}

	static class JacksonAnnotationPresentFilter extends TraversingTypeFilter {

		private static final Predicate<String> IS_JACKSON_ANNOTATION = it -> it.startsWith("com.fasterxml.jackson");

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.aot.HateoasRuntimeHints.TraversingTypeFilter#doMatch(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
		 */
		@Override
		protected boolean doMatch(MetadataReader reader, MetadataReaderFactory factory) {

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

	static class JacksonSuperTypeFilter extends TraversingTypeFilter {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.aot.HateoasRuntimeHints.TraversingTypeFilter#doMatch(org.springframework.core.type.classreading.MetadataReader, org.springframework.core.type.classreading.MetadataReaderFactory)
		 */
		@Override
		protected boolean doMatch(MetadataReader reader, MetadataReaderFactory factory) {
			return reader.getClassMetadata().getClassName().startsWith("com.fasterxml.jackson");
		}
	}
}
