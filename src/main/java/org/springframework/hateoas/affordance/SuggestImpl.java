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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.ReflectionUtils;

/**
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @param <T>
 */
public class SuggestImpl<T> implements Suggest<T> {

	private final T value;
	private final String valueField;
	private final String textField;

	public SuggestImpl(T value, String valueField, String textField) {

		this.value = value;
		this.valueField = valueField;
		this.textField = textField;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public String getValueField() {
		return valueField;
	}

	@Override
	public String getTextField() {
		return textField;
	}

	@Override
	public String getText() {

		if (value != null) {
			try {
				return getField(textField);
			} catch (Exception e) {
				throw new IllegalArgumentException("Textfield could not be serialized", e);
			}
		}
		return null;
	}

	@Override
	public String getValueAsString() {

		if (value != null) {
			try {
				if (valueField != null) {
					return getField(valueField);
				} else {
					return value.toString();
				}
			} catch (Exception e) {
				throw new IllegalArgumentException("Valuefield could not be serialized", e);
			}
		}
		return null;
	}

	public static <T> List<Suggest<T>> wrap(List<T> list, String valueField, String textField) {

		List<Suggest<T>> suggests = new ArrayList<Suggest<T>>(list.size());

		for (T value : list) {
			suggests.add(new SuggestImpl<T>(value, valueField, textField));
		}

		return suggests;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public <U> U getUnwrappedValue() {

		if (value instanceof WrappedValue) {
			return (U) ((WrappedValue) value).getValue();
		}
		return (U) value;
	}

	private String getField(String name) throws IllegalAccessException {

		Field field = ReflectionUtils.findField(value.getClass(), name);
		field.setAccessible(true);
		return String.valueOf(field.get(value));
	}
	
}
