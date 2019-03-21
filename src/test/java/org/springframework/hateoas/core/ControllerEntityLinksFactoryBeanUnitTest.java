/*
 * Copyright 2012-2015 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.hateoas.core.ControllerEntityLinksUnitTest.Person;
import org.springframework.hateoas.core.ControllerEntityLinksUnitTest.SampleController;
import org.springframework.hateoas.mvc.ControllerLinkBuilderFactory;
import org.springframework.stereotype.Controller;

/**
 * Unit tests for {@link ControllerEntityLinksFactoryBean}.
 * 
 * @author Oliver Gierke
 */
public class ControllerEntityLinksFactoryBeanUnitTest {

	@Rule public ExpectedException exception = ExpectedException.none();

	@Test
	public void rejectsFactoryBeanIfAnnotationNotSet() throws Exception {

		exception.expect(IllegalStateException.class);
		exception.expectMessage("Annotation");

		ControllerEntityLinksFactoryBean builder = new ControllerEntityLinksFactoryBean();
		builder.afterPropertiesSet();
	}

	@Test
	public void discoversSampleControllerFromApplicationContext() throws Exception {

		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		factory.registerBeanDefinition("controller", new RootBeanDefinition(SampleController.class));

		ConfigurableApplicationContext context = new GenericApplicationContext(factory);
		context.refresh();

		ControllerEntityLinksFactoryBean builder = new ControllerEntityLinksFactoryBean();
		builder.setAnnotation(Controller.class);
		builder.setLinkBuilderFactory(new ControllerLinkBuilderFactory());
		builder.setApplicationContext(context);
		builder.afterPropertiesSet();

		ControllerEntityLinks entityLinks = builder.getObject();
		assertThat(entityLinks.supports(Person.class), is(true));
	}
}
