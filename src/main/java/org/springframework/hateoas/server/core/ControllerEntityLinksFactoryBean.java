/*
 * Copyright 2012-2021 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.server.ExposesResourceFor;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.LinkBuilderFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * {@link FactoryBean} implementation to create {@link ControllerEntityLinks} instances looking up controller classes
 * from an {@link ApplicationContext}. The controller types are identified by the annotation type configured.
 *
 * @author Oliver Gierke
 */
public class ControllerEntityLinksFactoryBean extends AbstractFactoryBean<ControllerEntityLinks>
		implements ApplicationContextAware {

	private Class<? extends Annotation> annotation;
	private LinkBuilderFactory<? extends LinkBuilder> linkBuilderFactory;
	private ApplicationContext context;

	/**
	 * Configures the annotation type to inspect the {@link ApplicationContext} for beans that carry the given annotation.
	 *
	 * @param annotation must not be {@literal null}.
	 */
	public void setAnnotation(Class<? extends Annotation> annotation) {
		Assert.notNull(annotation, "Annotation must not be null!");
		this.annotation = annotation;
	}

	/**
	 * Configures the {@link LinkBuilderFactory} to be used to create {@link LinkBuilder} instances.
	 *
	 * @param linkBuilderFactory the linkBuilderFactory to set
	 */
	public void setLinkBuilderFactory(LinkBuilderFactory<? extends LinkBuilder> linkBuilderFactory) {
		this.linkBuilderFactory = linkBuilderFactory;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext context) {
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType()
	 */
	@NonNull
	@Override
	public Class<?> getObjectType() {
		return ControllerEntityLinks.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#createInstance()
	 */
	@Override
	protected ControllerEntityLinks createInstance() {

		Collection<Class<?>> controllerTypes = new HashSet<>();

		for (Class<?> controllerType : getBeanTypesWithAnnotation(annotation)) {
			if (AnnotationUtils.findAnnotation(controllerType, ExposesResourceFor.class) != null) {
				controllerTypes.add(controllerType);
			}
		}

		return new ControllerEntityLinks(controllerTypes, linkBuilderFactory);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.beans.factory.config.AbstractFactoryBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {

		Assert.state(annotation != null, "Annotation type must be configured!");
		Assert.state(linkBuilderFactory != null, "LinkBuilderFactory must be configured!");
		super.afterPropertiesSet();
	}

	private Iterable<Class<?>> getBeanTypesWithAnnotation(Class<? extends Annotation> type) {

		Set<Class<?>> annotatedTypes = new HashSet<>();

		for (String beanName : context.getBeanDefinitionNames()) {

			Annotation annotation = context.findAnnotationOnBean(beanName, type);
			if (annotation != null) {
				annotatedTypes.add(context.getType(beanName));
			}
		}

		return annotatedTypes;
	}
}
