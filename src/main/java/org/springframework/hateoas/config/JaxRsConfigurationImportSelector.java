/*
 * Copyright 2018 the original author or authors.
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
package org.springframework.hateoas.config;

import javax.ws.rs.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.core.ControllerEntityLinksFactoryBean;
import org.springframework.hateoas.jaxrs.JaxRsLinkBuilderFactory;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;
import org.springframework.util.ClassUtils;

/**
 * {@link ImportSelector} to register a {@link ControllerLinkBuilderFactory} for JaxRS' {@link Path} in case it's on the
 * classpath.
 * 
 * @author Oliver Gierke
 * @since 0.25
 */
class JaxRsConfigurationImportSelector implements ImportSelector {

	private static final boolean IS_JAX_RS_PRESENT = ClassUtils.isPresent("javax.ws.rs.Path",
			ClassUtils.getDefaultClassLoader());

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.annotation.ImportSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
	 */
	@Override
	public String[] selectImports(AnnotationMetadata importingClassMetadata) {

		return IS_JAX_RS_PRESENT //
				? new String[] { JaxRsEntityLinksConfiguration.class.getName() } //
				: new String[0];
	}

	@Configuration
	static class JaxRsEntityLinksConfiguration {

		@Bean
		ControllerEntityLinksFactoryBean jaxRsEntityLinks() {

			ControllerEntityLinksFactoryBean factory = new ControllerEntityLinksFactoryBean();
			factory.setAnnotation(Path.class);
			factory.setLinkBuilderFactory(new JaxRsLinkBuilderFactory());

			return factory;
		}
	}
}
