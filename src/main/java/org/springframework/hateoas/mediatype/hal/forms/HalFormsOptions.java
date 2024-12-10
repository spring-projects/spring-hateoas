/*
 * Copyright 2021-2024 the original author or authors.
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
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Representation of HAL-FORMS {@code options} attribute.
 *
 * @author Oliver Drotbohm
 * @author Réda Housni Alaoui
 * @see https://rwcbook.github.io/hal-forms/#options-element
 * @since 1.3
 */
@JsonInclude(Include.NON_EMPTY)
public interface HalFormsOptions {

	/**
	 * Creates a new {@link Inline} options representation listing the given values.
	 *
	 * @param values must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Inline inline(T... values) {

		Assert.notNull(values, "Values must not be null!");

		return inline(Arrays.asList(values));
	}

	/**
	 * Creates a new {@link Inline} options representation listing the given collection of values.
	 *
	 * @param values must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static Inline inline(Collection<? extends Object> values) {

		Assert.notNull(values, "Values must not be null!");

		return new Inline(values, null, null, null, null, null);
	}

	/**
	 * Creates a new {@link Remote} options representation using the given {@link Link}.
	 *
	 * @param link must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static Remote remote(Link link) {

		Assert.notNull(link, "Link must not be null!");

		return new Remote(link, null, null, null, null, null);
	}

	/**
	 * Creates a new {@link Remote} options representation using the given href.
	 *
	 * @param href must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public static Remote remote(String href) {

		Assert.hasText(href, "Href must not by null or empty!");

		return remote(Link.of(href));
	}

	/**
	 * The field to look up the prompt from.
	 *
	 * @return
	 */
	@Nullable
	String getPromptField();

	/**
	 * The field to use as the value to be sent.
	 *
	 * @return
	 */
	@Nullable
	String getValueField();

	/**
	 * Returns the minimum number of items to be selected.
	 *
	 * @return {@literal null}, 0 or a positive {@link Long}.
	 */
	@Nullable
	Long getMinItems();

	/**
	 * Returns the maximum number of items to be selected.
	 *
	 * @return {@literal null} or a positive {@link Long}.
	 */
	@Nullable
	Long getMaxItems();

	@Nullable
	Object getSelectedValue();

	public static abstract class AbstractHalFormsOptions<T extends AbstractHalFormsOptions<T>>
			implements HalFormsOptions {

		private final @Nullable String promptField, valueField;
		private final @Nullable Long minItems, maxItems;
		private final @Nullable Object selectedValue;

		protected AbstractHalFormsOptions(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems, @Nullable Object selectedValue) {

			Assert.isTrue(minItems == null || minItems >= 0, "MinItems must be greater than or equal to 0!");

			this.promptField = promptRef;
			this.valueField = valueRef;
			this.minItems = minItems;
			this.maxItems = maxItems;
			this.selectedValue = selectedValue;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getPromptRef()
		 */
		@Nullable
		@Override
		@JsonProperty
		public String getPromptField() {
			return promptField;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getValueField()
		 */
		@Nullable
		@Override
		@JsonProperty
		public String getValueField() {
			return valueField;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getMinItems()
		 */
		@Nullable
		@Override
		@JsonProperty
		public Long getMinItems() {
			return minItems;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getMaxItems()
		 */
		@Nullable
		@Override
		@JsonProperty
		public Long getMaxItems() {
			return maxItems;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions#getSelectedValue()
		 */
		@Nullable
		@Override
		@JsonIgnore
		public Object getSelectedValue() {
			return selectedValue;
		}

		/**
		 * Configures the given field to be used as prompt field.
		 *
		 * @param promptField must either be {@literal null} or actually have text.
		 * @return
		 */
		public T withPromptField(String promptField) {

			if (promptField != null && !StringUtils.hasText(promptField)) {
				throw new IllegalArgumentException("Prompt field has to either be null or actually have text!");
			}

			return with(promptField, valueField, minItems, maxItems, selectedValue);
		}

		/**
		 * Configures the given field to be used as value field.
		 *
		 * @param valueField must either be {@literal null} or actually have text.
		 * @return
		 */
		public T withValueField(String valueField) {

			if (valueField != null && !StringUtils.hasText(valueField)) {
				throw new IllegalArgumentException("Value field has to either be null or actually have text!");
			}

			return with(promptField, valueField, minItems, maxItems, selectedValue);
		}

		/**
		 * Configures the minimum number of items to be selected.
		 *
		 * @param minItems must be {@literal null} or greater than or equal to zero.
		 * @return
		 */
		public T withMinItems(Long minItems) {

			if (minItems != null && minItems < 0) {
				throw new IllegalArgumentException("minItems has to be null or greater or equal to zero!");
			}

			return with(promptField, valueField, minItems, maxItems, selectedValue);
		}

		/**
		 * Configures the maximum number of items to be selected.
		 *
		 * @param maxItems must be {@literal null} or greater than zero.
		 * @return
		 */
		public T withMaxItems(@Nullable Long maxItems) {

			if (maxItems != null && maxItems <= 0) {
				throw new IllegalArgumentException("maxItems has to be null or greater than zero!");
			}

			return with(promptField, valueField, minItems, maxItems, selectedValue);
		}

		/**
		 * Configured the value to be initially selected
		 *
		 * @param value
		 * @return
		 */
		public T withSelectedValue(@Nullable Object value) {
			return with(promptField, valueField, minItems, maxItems, value);
		}

		/**
		 * Create a new concrete {@link AbstractHalFormsOptions}
		 *
		 * @param promptRef
		 * @param valueRef
		 * @param minItems
		 * @param maxItems
		 * @return
		 */
		protected abstract T with(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems, @Nullable Object selectedValue);
	}

	public static class Inline extends AbstractHalFormsOptions<Inline> {

		private final Collection<? extends Object> inline;

		/**
		 * @param values
		 * @param promptRef
		 * @param valueRef
		 */
		private Inline(Collection<? extends Object> values, @Nullable String promptRef, @Nullable String valueRef,
				@Nullable Long minItems, @Nullable Long maxItems, @Nullable Object selectedValue) {

			super(promptRef, valueRef, minItems, maxItems, selectedValue);

			Assert.notNull(values, "Values must not be null!");

			this.inline = values;
		}

		/**
		 * Returns the inline values.
		 *
		 * @return
		 */
		@JsonProperty
		public Collection<? extends Object> getInline() {
			return inline;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions.AbstractHalFormsOptions#with(java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Object)
		 */
		@Override
		protected Inline with(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems, @Nullable Object selectedValue) {
			return new Inline(inline, promptRef, valueRef, minItems, maxItems, selectedValue);
		}
	}

	/**
	 * Representation of a remote options element.
	 *
	 * @author Oliver Drotbohm
	 */
	public static class Remote extends AbstractHalFormsOptions<Remote> {

		private final Link link;

		private Remote(Link link, @Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems, @Nullable Object selectedValue) {

			super(promptRef, valueRef, minItems, maxItems, selectedValue);

			Assert.notNull(link, "Link must not be null!");

			this.link = link;
		}

		/**
		 * Returns the {@link Link} pointing to the resource returning option values.
		 *
		 * @return
		 */
		@JsonProperty
		public Link getLink() {
			return link;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.hal.forms.HalFormsOptions.AbstractHalFormsOptions#with(java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Object)
		 */
		@Override
		protected Remote with(@Nullable String promptRef, @Nullable String valueRef, @Nullable Long minItems,
				@Nullable Long maxItems, @Nullable Object selectedValue) {
			return new Remote(link, promptRef, valueRef, minItems, maxItems, selectedValue);
		}
	}
}
