/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.hateoas.forms;

import org.springframework.hateoas.mvc.ControllerFormBuilder;

/**
 * Builder used by {@link ControllerFormBuilder} to create instances of {@link Property}
 * 
 */
public class PropertyBuilder {

	private String name;

	// we want to idenfify if user has setted the readonly value (default value is not false)
	private Boolean readonly;

	private boolean templated;

	private String value;

	private String prompt;

	private String regex;

	private Class<?> declaringClass;

	protected TemplateBuilder formBuilder;

	private SuggestBuilderFactory suggestBuilderFactory;

	public PropertyBuilder(String name, Class<?> declaringClass, TemplateBuilder formBuilder) {
		this.name = name;
		this.declaringClass = declaringClass;
		this.formBuilder = formBuilder;
	}

	public PropertyBuilder readonly(boolean readonly) {
		this.readonly = readonly;
		return this;
	}

	public SuggestBuilderFactory suggest() {
		this.suggestBuilderFactory = new SuggestBuilderFactory();
		return this.suggestBuilderFactory;
	}

	public Property build() {
		return new Property(name, readonly, templated, value, prompt, regex,
				suggestBuilderFactory != null ? suggestBuilderFactory.build() : null);
	}

	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	public PropertyBuilder regex(String regex) {
		this.regex = regex;
		return this;
	}

	public PropertyBuilder prompt(String prompt) {
		this.prompt = prompt;
		return this;
	}

	public PropertyBuilder value(String value) {
		this.value = value;
		this.templated = isTemplatedValue(value);
		return this;
	}

	private boolean isTemplatedValue(String value) {
		return value.startsWith("{") && value.endsWith("}");
	}
}
