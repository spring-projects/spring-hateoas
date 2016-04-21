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

import java.util.Arrays;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;

/**
 * Builder returned by {@link PropertyBuilder#suggest()} that provides different types of {@link SuggestBuilder}
 *
 */
public class SuggestBuilderProvider {

	private SuggestBuilder suggestBuilder;

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link ValueSuggest} of type {@link ValueSuggestType#DIRECT}
	 * @param values
	 * @return
	 */
	public <D> SuggestBuilder values(Iterable<D> values) {
		this.suggestBuilder = new ValueSuggestBuilder<D>(values);
		return suggestBuilder;
	}

	public <D> SuggestBuilder values(D[] values) {
		this.suggestBuilder = new ValueSuggestBuilder<D>(Arrays.asList(values));
		return suggestBuilder;
	}

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link ValueSuggest} of type {@link ValueSuggestType#EMBEDDED}
	 * @param values
	 * @return
	 */
	public <D> SuggestBuilder embedded(Iterable<D> values) {
		this.suggestBuilder = new EmbeddedSuggestBuilder<D>(values);
		return suggestBuilder;
	}

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link ValueSuggest} of type {@link ValueSuggestType#EMBEDDED}
	 * @param values
	 * @return
	 */
	public <D> SuggestBuilder embedded(D[] values) {
		this.suggestBuilder = new EmbeddedSuggestBuilder<D>(Arrays.asList(values));
		return suggestBuilder;
	}

	/**
	 * Returns a {@link SuggestBuilder} to construct a {@link LinkSuggest}
	 * @param link
	 * @return
	 */
	public SuggestBuilder link(Link link) {
		this.suggestBuilder = new LinkSuggestBuilder(link);
		return suggestBuilder;
	}

	public SuggestBuilder getSuggestBuilder() {
		return suggestBuilder;
	}

	public Suggest build() {
		return suggestBuilder != null ? suggestBuilder.build() : null;
	}
}
