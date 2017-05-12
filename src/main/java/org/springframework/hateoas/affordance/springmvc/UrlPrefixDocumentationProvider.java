/*
 * Copyright 2014-2017 the original author or authors.
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

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.util.Assert;

/**
 * Provides documentation URLs by applying an URL prefix.
 *
 * @author Dietrich Schulten
 */
public class UrlPrefixDocumentationProvider implements DocumentationProvider {

	private String defaultUrlPrefix;

	public UrlPrefixDocumentationProvider(String defaultUrlPrefix) {

		Assert.isTrue(defaultUrlPrefix.endsWith("/") || defaultUrlPrefix.endsWith("#"),
				"URL prefix should end with separator / or #");
		this.defaultUrlPrefix = defaultUrlPrefix;
	}

	public UrlPrefixDocumentationProvider() {
		this.defaultUrlPrefix = "";
	}

	/*
	 * @see DocumentationProvider#getDocumentationUrl
	 */
	@Override
	public String getDocumentationUrl(ActionInputParameter annotatedParameter, Object content) {
		return this.defaultUrlPrefix + annotatedParameter.getParameterName();
	}

	/*
	 * @see DocumentationProvider#getDocumentationUrl
	 */
	@Override
	public String getDocumentationUrl(Field field, Object content) {
		return this.defaultUrlPrefix + field.getName();
	}

	/*
	 * @see DocumentationProvider#getDocumentationUrl
	 */
	@Override
	public String getDocumentationUrl(Method getter, Object content) {

		String methodName = getter.getName();
		String propertyName = Introspector.decapitalize(methodName.substring(methodName.startsWith("is") ? 2 : 3));
		return this.defaultUrlPrefix + propertyName;
	}

	/*
	 * @see DocumentationProvider#getDocumentationUrl
	 */
	@Override
	public String getDocumentationUrl(Class<?> clazz, Object content) {
		return this.defaultUrlPrefix + clazz.getSimpleName();
	}

	/*
	 * @see DocumentationProvider#getDocumentationUrl
	 */
	@Override
	public String getDocumentationUrl(String name, Object content) {
		return this.defaultUrlPrefix + name;
	}
}
