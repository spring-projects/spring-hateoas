/*
 * Copyright 2016-2017 the original author or authors.
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
package org.springframework.hateoas.affordance.springmvc;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.hateoas.affordance.ActionInputParameter;

/**
 * Default documentation provider, always returns null as documentation url.
 * 
 * @author Dietrich Schulten
 * @author Oliver Gierke
 */
public class DefaultDocumentationProvider implements DocumentationProvider {

	/*
	 * (non-Javadoc)
	 * @see DocumentationProvider#getDocumentationUrl(ActionInputParameter, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(ActionInputParameter annotatedParameter, Object content) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see DocumentationProvider#getDocumentationUrl(java.lang.reflect.Field, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(Field field, Object content) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see DocumentationProvider#getDocumentationUrl(java.lang.reflect.Method, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(Method method, Object content) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see DocumentationProvider#getDocumentationUrl(java.lang.Class, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(Class<?> clazz, Object content) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see DocumentationProvider#getDocumentationUrl(java.lang.String, java.lang.Object)
	 */
	@Override
	public String getDocumentationUrl(String name, Object content) {
		return null;
	}
}
