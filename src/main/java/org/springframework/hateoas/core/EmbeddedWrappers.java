/*
 * Copyright 2014 the original author or authors.
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
package org.springframework.hateoas.core;

import java.util.Collection;
import java.util.Collections;

import org.springframework.aop.support.AopUtils;
import org.springframework.hateoas.Resource;
import org.springframework.util.Assert;

/**
 * Interface to mark objects that are aware of the rel they'd like to be exposed under.
 * 
 * @author Oliver Gierke
 */
public class EmbeddedWrappers {

	private final boolean preferCollections;

	/**
	 * Creates a new {@link EmbeddedWrappers}.
	 * 
	 * @param preferCollections whether wrappers for single elements should rather treat the value as collection.
	 */
	public EmbeddedWrappers(boolean preferCollections) {
		this.preferCollections = preferCollections;
	}

	/**
	 * Creates a new {@link EmbeddedWrapper} that
	 * 
	 * @param source
	 * @return
	 */
	public EmbeddedWrapper wrap(Object source) {
		return wrap(source, AbstractEmbeddedWrapper.NO_REL);
	}

	/**
	 * Creates a new {@link EmbeddedWrapper} with the given rel.
	 * 
	 * @param source can be {@literal null}, will return {@literal null} if so.
	 * @param rel must not be {@literal null} or empty.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public EmbeddedWrapper wrap(Object source, String rel) {

		if (source == null) {
			return null;
		}

		if (source instanceof EmbeddedWrapper) {
			return (EmbeddedWrapper) source;
		}

		if (source instanceof Collection) {
			return new EmbeddedCollection((Collection<Object>) source, rel);
		}

		if (preferCollections) {
			return new EmbeddedCollection(Collections.singleton(source), rel);
		}

		return new EmbeddedElement(source, rel);
	}

	private static abstract class AbstractEmbeddedWrapper implements EmbeddedWrapper {

		private static final String NO_REL = "___norel___";

		private final String rel;

		/**
		 * Creates a new {@link AbstractEmbeddedWrapper} with the given rel.
		 * 
		 * @param rel must not be {@literal null} or empty.
		 */
		public AbstractEmbeddedWrapper(String rel) {

			Assert.hasText(rel, "Rel must not be null or empty!");
			this.rel = rel;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#getRel()
		 */
		@Override
		public String getRel() {
			return NO_REL.equals(rel) ? null : rel;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#hasRel(java.lang.String)
		 */
		@Override
		public boolean hasRel(String rel) {
			return this.rel.equals(rel);
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#getRelTargetType()
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Class<?> getRelTargetType() {

			Object peek = peek();
			peek = peek instanceof Resource ? ((Resource<Object>) peek).getContent() : peek;

			return AopUtils.getTargetClass(peek);
		}

		/**
		 * Peek into the wrapped element. The object returned is used to determine the actual value type of the wrapper.
		 * 
		 * @return
		 */
		protected abstract Object peek();
	}

	/**
	 * {@link EmbeddedWrapper} for a single element.
	 *
	 * @author Oliver Gierke
	 */
	private static class EmbeddedElement extends AbstractEmbeddedWrapper {

		private final Object value;

		/**
		 * Creates a new {@link EmbeddedElement} for the given value and rel.
		 * 
		 * @param value must not be {@literal null}.
		 * @param rel must not be {@literal null} or empty.
		 */
		public EmbeddedElement(Object value, String rel) {

			super(rel);
			Assert.notNull(value, "Value must not be null!");
			this.value = value;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#getValue()
		 */
		@Override
		public Object getValue() {
			return value;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.EmbeddedWrappers.AbstractElementWrapper#peek()
		 */
		@Override
		protected Object peek() {
			return getValue();
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#isCollectionValue()
		 */
		@Override
		public boolean isCollectionValue() {
			return false;
		}
	}

	/**
	 * {@link EmbeddedWrapper} for a collection of elements.
	 *
	 * @author Oliver Gierke
	 */
	private static class EmbeddedCollection extends AbstractEmbeddedWrapper {

		private final Collection<Object> value;

		/**
		 * @param value must not be {@literal null} or empty.
		 * @param rel must not be {@literal null} or empty.
		 */
		public EmbeddedCollection(Collection<Object> value, String rel) {

			super(rel);

			Assert.notNull(value, "Collection must not be null!");
			Assert.notEmpty(value, "Collection must not be empty");

			this.value = value;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#getValue()
		 */
		@Override
		public Collection<Object> getValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrappers.AbstractEmbeddedWrapper#peek()
		 */
		@Override
		protected Object peek() {
			return value.iterator().next();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#isCollectionValue()
		 */
		@Override
		public boolean isCollectionValue() {
			return true;
		}
	}
}
