/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.hateoas.affordance;

import static org.springframework.hateoas.affordance.support.Path.*;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @param <T>
 */
@Data
@EqualsAndHashCode(of = {"text", "suggestionValue"})
public class SuggestObjectWrapper<T> implements WrappedValue<T> {

	public static final String ID = path(on(SuggestObjectWrapper.class).getSuggestionValue());
	public static final String TEXT = path(on(SuggestObjectWrapper.class).getText());

	private final String text;
	private final String suggestionValue;
	private final T original;

	public SuggestObjectWrapper(String text, String suggestionValue, T original) {
		
		this.text = text;
		this.suggestionValue = suggestionValue;
		this.original = original;
	}

	@Override
	public T getValue() {
		return original;
	}

}
