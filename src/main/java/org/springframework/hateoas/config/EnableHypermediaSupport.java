/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaConfigurationImportSelector;
import org.springframework.hateoas.hal.forms.HalFormsConfiguration;

/**
 * Activates hypermedia support in the {@link ApplicationContext}. Will register infrastructure beans available for
 * injection to ease building hypermedia related code. Which components get registered depends on the hypermedia type
 * being activated through the {@link #type()} attribute. Hypermedia-type-specific implementations of the following
 * components will be registered:
 * <ul>
 * <li>{@link LinkDiscoverer}</li>
 * <li>a Jackson 2 module to correctly marshal the resource model classes into the appropriate representation.
 * </ul>
 * 
 * @see LinkDiscoverer
 * @see EntityLinks
 * @author Oliver Gierke
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ HypermediaSupportBeanDefinitionRegistrar.class, HateoasConfiguration.class,
		HypermediaConfigurationImportSelector.class })
public @interface EnableHypermediaSupport {

	/**
	 * The hypermedia type to be supported.
	 * 
	 * @return
	 */
	HypermediaType[] type() default {};

	/**
	 * Hypermedia representation types supported.
	 * 
	 * @author Oliver Gierke
	 */
	enum HypermediaType {

		/**
		 * HAL - Hypermedia Application Language.
		 * 
		 * @see http://stateless.co/hal_specification.html
		 * @see http://tools.ietf.org/html/draft-kelly-json-hal-05
		 */
		HAL,

		HAL_FORMS(HalFormsConfiguration.class.getName());

		private List<String> configurations;

		HypermediaType(String... configurations) {
			this.configurations = Arrays.asList(configurations);
		}
	}

	@Slf4j
	class HypermediaConfigurationImportSelector implements ImportSelector {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.context.annotation.ImportSelector#selectImports(org.springframework.core.type.AnnotationMetadata)
		 */
		@Override
		public String[] selectImports(AnnotationMetadata metadata) {

			Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableHypermediaSupport.class.getName());

			Collection<HypermediaType> types = Arrays.asList((HypermediaType[]) attributes.get("type"));
			types = types.isEmpty() ? Arrays.asList(HypermediaType.values()) : types;

			log.debug("Registering support for hypermedia types {} according configuration on {}.", types,
					metadata.getClassName());

			List<String> configurations = new ArrayList<String>();

			for (HypermediaType type : types) {
				configurations.addAll(type.configurations);
			}

			return configurations.toArray(new String[configurations.size()]);
		}
	}
}
