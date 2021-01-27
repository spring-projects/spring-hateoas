/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.hal.forms;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.hateoas.Link;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Oliver Drotbohm
 */
@JsonInclude(Include.NON_EMPTY)
public interface HalFormsOptions {

	public static Inline inline(Object... values) {
		return inline(Arrays.asList(values));
	}

	public static Inline inline(Collection<Object> values) {
		return new Inline(values, null, null, null, null);
	}

	public static Remote remote(Link link) {
		return new Remote(link, null, null, null, null);
	}

	public static Remote remote(String href) {
		return remote(Link.of(href));
	}

	/**
	 * @return the promptRef
	 */
	@Nullable
	String getPromptField();

	@Nullable
	String getValueField();

	@Nullable
	Long getMinItems();

	@Nullable
	Long getMaxItems();

	public static abstract class AbstractHalFormsOptions<T extends AbstractHalFormsOptions<T>>
			implements HalFormsOptions {

		private final @Nullable String promptField, valueField;
		private final @Nullable Long minItems, maxItems;

		protected AbstractHalFormsOptions(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems) {

			Assert.isTrue(minItems == null || minItems >= 0, "MinItems must be greater than or equal to 0!");

			this.promptField = promptRef;
			this.valueField = valueRef;
			this.minItems = minItems;
			this.maxItems = maxItems;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getPromptRef()
		 */
		@Nullable
		@Override
		public String getPromptField() {
			return promptField;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getValueField()
		 */
		@Nullable
		@Override
		public String getValueField() {
			return valueField;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getMinItems()
		 */
		@Nullable
		@Override
		public Long getMinItems() {
			return minItems;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getMaxItems()
		 */
		@Nullable
		@Override
		public Long getMaxItems() {
			return maxItems;
		}

		public T withPromptField(String promptField) {
			return with(promptField, valueField, minItems, maxItems);
		}

		public T withValueField(String valueField) {
			return with(promptField, valueField, minItems, maxItems);
		}

		public T withMinItems(Long minItems) {
			return with(promptField, valueField, minItems, maxItems);
		}

		public T withMaxItems(Long maxItems) {
			return with(promptField, valueField, minItems, maxItems);
		}

		protected abstract T with(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems);
	}

	public static class Inline extends AbstractHalFormsOptions<Inline> {

		private final Collection<Object> inline;

		/**
		 * @param values
		 * @param promptRef
		 * @param valueRef
		 */
		private Inline(Collection<Object> values, @Nullable String promptRef, @Nullable String valueRef,
				@Nullable Long minItems, @Nullable Long maxItems) {

			super(promptRef, valueRef, minItems, maxItems);

			Assert.notNull(values, "Values must not be null!");

			this.inline = values;
		}

		@JsonProperty
		public Collection<Object> getInline() {
			return inline;
		}

		@Override
		protected Inline with(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems) {
			return new Inline(inline, promptRef, valueRef, minItems, maxItems);
		}
	}

	public static class Remote extends AbstractHalFormsOptions<Remote> {

		private final Link link;

		private Remote(Link link, @Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems) {

			super(promptRef, valueRef, minItems, maxItems);

			Assert.notNull(link, "Link must not be null!");

			this.link = link;
		}

		@JsonProperty
		public Link getLink() {
			return link;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions.Foo#withFoo(java.lang.String, java.lang.String)
		 */
		@Override
		protected Remote with(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems) {
			return new Remote(link, promptRef, valueRef, minItems, maxItems);
		}
	}
}
