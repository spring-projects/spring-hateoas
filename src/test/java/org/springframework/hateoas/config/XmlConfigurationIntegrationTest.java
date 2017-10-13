/*
 * Copyright 2014 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;

/**
 * Integration tests for using {@link EnableHypermediaSupport} from within XML documents.
 * 
 * @author Oliver Gierke
 */
public class XmlConfigurationIntegrationTest {

	/**
	 * @see #259
	 */
	@Test
	public void enablesHyperMediaSupportFromXml() {

		ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("application-context.xml", getClass());

		assertThat(context.getBean(RelProvider.class)).isNotNull();

		context.close();
	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HypermediaConfiguration {}
}
