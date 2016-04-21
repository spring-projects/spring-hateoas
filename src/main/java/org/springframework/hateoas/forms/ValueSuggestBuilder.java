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

import org.springframework.util.Assert;

public class ValueSuggestBuilder<D> extends AbstractSuggestBuilder {

	protected Iterable<D> values;

	private Class<?> componentType;

	public ValueSuggestBuilder(Iterable<D> values) {
		this.values = values;
		if (values.iterator().hasNext()) {
			this.componentType = values.iterator().next().getClass();
		}
	}

	@Override
	public AbstractSuggestBuilder textField(String textFieldName) {
		Assert.notNull(FieldUtils.findFieldClass(componentType, textFieldName));
		return super.textField(textFieldName);
	}

	@Override
	public AbstractSuggestBuilder valueField(String valueFieldName) {
		Assert.notNull(FieldUtils.findFieldClass(componentType, valueFieldName));
		return super.valueField(valueFieldName);
	}

	@Override
	public Suggest build() {
		return new ValueSuggest<D>(values, textFieldName, valueFieldName);
	}
}
