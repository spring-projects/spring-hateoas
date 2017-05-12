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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.UriTemplate;

/**
 * @author Oliver Gierke
 */
public abstract class Suggestions {

	public static Suggestions NONE = new EmptySuggestions();

	/**
	 * Returns the name of the field that is supposed to be used as human-readable value to symbolize the suggestion.
	 * 
	 * @return the field name. Can be {@literal null}.
	 */
	public abstract String getPromptField();

	/**
	 * Returns the name of the field that us supposed to be sent back to the server to identify the suggestion.
	 * 
	 * @return the field name. Can be {@literal null}.
	 */
	public abstract String getValueField();

	/**
	 * Invokes the given {@link SuggestionVisitor} for the {@link Suggestions}. Allows to act on the different suggestion
	 * types explicitly.
	 * 
	 * @param handler must not be {@literal null}.
	 * @return
	 */
	public abstract <T> T accept(SuggestionVisitor<T> handler);

	/**
	 * Returns {@link ValueSuggestions} for the given values.
	 * 
	 * @param values
	 * @return
	 */
	public static <S> ValueSuggestions<S> values(S... values) {
		return ValueSuggestions.of(values);
	}

	public static <S> ValueSuggestions<S> values(Collection<S> values) {
		return new ValueSuggestions<S>(values, null, null);
	}

	public static CustomizableSuggestions external(String reference) {
		return ExternalSuggestions.of(reference);
	}

	public static CustomizableSuggestions remote(UriTemplate template) {
		return RemoteSuggestions.of(template);
	}

	public static CustomizableSuggestions remote(Link link) {
		return remote(link.getHref());
	}

	public static CustomizableSuggestions remote(String template) {
		return remote(new UriTemplate(template));
	}

	public static abstract class CustomizableSuggestions extends Suggestions {

		public abstract CustomizableSuggestions withPromptField(String field);

		public abstract CustomizableSuggestions withValueField(String field);
	}

	/**
	 * Suggestions that consist of a static set of values.
	 *
	 * @author Oliver Gierke
	 */
	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode
	public static class ValueSuggestions<T> extends CustomizableSuggestions implements Iterable<T> {

		private final Collection<T> suggestions;
		private final @Wither String promptField;
		private final @Wither String valueField;

		static <T> ValueSuggestions<T> of(T... values) {
			return new ValueSuggestions<T>(Arrays.asList(values), null, null);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.Options#accept(org.springframework.hateoas.affordance.SuggestHandler)
		 */
		@Override
		public <S> S accept(SuggestionVisitor<S> handler) {
			return handler.visit(this);
		}

		/* 
		 * (non-Javadoc)
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<T> iterator() {
			return suggestions.iterator();
		}
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode
	public static class ExternalSuggestions extends CustomizableSuggestions {

		private final String reference;
		private final @Wither String promptField;
		private final @Wither String valueField;

		static ExternalSuggestions of(String reference) {
			return new ExternalSuggestions(reference, null, null);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.Options#accept(org.springframework.hateoas.affordance.SuggestHandler)
		 */
		@Override
		public <T> T accept(SuggestionVisitor<T> handler) {
			return handler.visit(this);
		}
	}

	@Getter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@EqualsAndHashCode
	public static class RemoteSuggestions extends CustomizableSuggestions {

		private final UriTemplate template;
		private final @Wither String promptField;
		private final @Wither String valueField;

		static RemoteSuggestions of(UriTemplate template) {
			return new RemoteSuggestions(template, null, null);
		}

		/**
		 * Invokes the given {@link SuggestionVisitor} for the {@link Suggestions}. Allows to act on the different suggestion
		 * types explicitly.
		 *
		 * @param handler must not be {@literal null}.
		 * @return
		 */
		@Override
		public <T> T accept(SuggestionVisitor<T> handler) {
			return handler.visit(this);
		}
	}

	private static class EmptySuggestions extends Suggestions {

		public String getPromptField() {
			return null;
		}

		public String getValueField() {
			return null;
		}

		/**
		 * Invokes the given {@link SuggestionVisitor} for the {@link Suggestions}. Allows to act on the different suggestion
		 * types explicitly.
		 *
		 * @param handler must not be {@literal null}.
		 * @return
		 */
		@Override
		public <T> T accept(SuggestionVisitor<T> handler) {
			return null;
		}
	}
}
