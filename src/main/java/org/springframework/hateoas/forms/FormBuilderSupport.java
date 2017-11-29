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

import org.springframework.web.util.UriComponentsBuilder;

public abstract class FormBuilderSupport<T extends TemplateBuilder> extends TemplateBuilderSupport<T> {

	public FormBuilderSupport(UriComponentsBuilder builder) {
		super(builder);
	}

	@Override
	public Form withKey(String key) {
		Form form = new Form(toString(), key);
		form.setBody(getBody());
		form.setProperties(getProperties());
		form.setMethod(getMethod());
		form.setContentType(getContentType());
		return form;
	}

	@Override
	public Form withDefaultKey() {
		return withKey(Template.DEFAULT_KEY);
	}

	public abstract Object getBody();

	public abstract String getContentType();

}
