/*
 * Copyright 2012-2016 the original author or authors.
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
package org.springframework.hateoas.jaxrs;

import java.util.Map;

import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.LinkBuilderFactory;

/**
 * Factory for {@link LinkBuilder} instances based on the path mapping annotated on the given JAX-RS service.
 * 
 * @author Ricardo Gladwell
 * @author Oliver Gierke
 * @author Andrew Naydyonock
 */
public class JaxRsLinkBuilderFactory implements LinkBuilderFactory<JaxRsLinkBuilder> {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class)
	 */
	public JaxRsLinkBuilder linkTo(Class<?> service) {
		return JaxRsLinkBuilder.linkTo(service);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.lang.Object[])
	 */
	@Override
	public JaxRsLinkBuilder linkTo(Class<?> service, Object... parameters) {
		return JaxRsLinkBuilder.linkTo(service, parameters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.LinkBuilderFactory#linkTo(java.lang.Class, java.util.Map)
	 */
	@Override
	public JaxRsLinkBuilder linkTo(Class<?> service, Map<String, ?> parameters) {
		return JaxRsLinkBuilder.linkTo(service, parameters);
	}
}
