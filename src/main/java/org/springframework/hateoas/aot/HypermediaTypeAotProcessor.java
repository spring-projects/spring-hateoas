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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
import org.springframework.beans.factory.aot.BeanRegistrationCode;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.util.Assert;

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

		var packagesToScan = mediaTypePackages.toList();

		return packagesToScan.isEmpty() ? null : new MediaTypeReflectionAotContribution(packagesToScan);
	}

	static class MediaTypeReflectionAotContribution implements BeanRegistrationAotContribution {

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

			mediaTypePackage.forEach(it -> {

				if (packagesSeen.contains(it)) {
					return;
				}

				packagesSeen.add(it);

				new HypermediaTypesRuntimeHints(it) //
						.registerHints(generationContext.getRuntimeHints(), getClass().getClassLoader());
			});
		}
	}
}
