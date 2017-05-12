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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @param <T>
 */
public class SimpleSuggest<T> extends SuggestImpl<SuggestObjectWrapper<T>> {

	private SimpleSuggest(SuggestObjectWrapper<T> wrapper) {
		super(wrapper, SuggestObjectWrapper.ID, SuggestObjectWrapper.TEXT);
	}

	/**
	 * Transfrom an array of objects into a collection of {@link Suggest}ions.
	 * 
	 * @param values
	 * @param <T>
	 * @return
	 */
	public static <T> List<Suggest<SuggestObjectWrapper<T>>> wrap(T[] values) {

		List<Suggest<SuggestObjectWrapper<T>>> suggests = new ArrayList<Suggest<SuggestObjectWrapper<T>>>(values.length);

		for (T value : values) {
			suggests.add(new SimpleSuggest<T>(
				new SuggestObjectWrapper<T>(String.valueOf(value), String.valueOf(value), value)));
		}

		return suggests;
	}

}
