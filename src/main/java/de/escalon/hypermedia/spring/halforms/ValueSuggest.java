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
package de.escalon.hypermedia.spring.halforms;

import org.springframework.hateoas.Resource;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.escalon.hypermedia.affordance.SuggestType;

/**
 * Suggested values of a {@link Property} that are included into the response. There are two ways: include text/value
 * pairs into the "suggest" attribute of {@link Property} or include a reference to a _embedded element of
 * {@link Resource}
 * 
 * @see ValueSuggestType
 * @param <D>
 */
@JsonSerialize(using = ValueSuggestSerializer.class)
class ValueSuggest<D> extends AbstractSuggest {

	private final Iterable<D> values;

	private final SuggestType type;

	public ValueSuggest(Iterable<D> values, String textFieldName, String valueFieldName) {
		this(values, textFieldName, valueFieldName, SuggestType.INTERNAL);
	}

	public ValueSuggest(Iterable<D> values, String textFieldName, String valueFieldName, SuggestType type) {
		super(textFieldName, valueFieldName);
		this.values = values;
		this.type = type;
	}

	public SuggestType getType() {
		return type;
	}

	public Iterable<D> getValues() {
		return values;
	}

}
