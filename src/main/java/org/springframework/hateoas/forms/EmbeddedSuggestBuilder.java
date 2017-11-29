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

import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;

/**
 * Builds {@link ValueSuggest} of type {@link ValueSuggestType#EMBEDDED}
 *
 */
public class EmbeddedSuggestBuilder<D> extends ValueSuggestBuilder<D> {

	public EmbeddedSuggestBuilder(Iterable<D> values) {
		super(values);
	}

	@Override
	public Suggest build() {
		return new ValueSuggest<D>(values, textFieldName, valueFieldName, ValueSuggestType.EMBEDDED);
	}
}
