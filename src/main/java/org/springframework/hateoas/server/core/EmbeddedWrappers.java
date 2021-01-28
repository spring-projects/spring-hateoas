/*
 * Copyright 2014-2021 the original author or authors.
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
package org.springframework.hateoas.server.core;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.LinkRelation;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Interface to mark objects that are aware of the rel they'd like to be exposed under.
 *
 * @author Oliver Gierke
 */
public class EmbeddedWrappers {

	private static ResolvableType SUPPLIER_OF_STREAM = ResolvableType.forClassWithGenerics(Supplier.class, Stream.class);

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
	 * @param source must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public EmbeddedWrapper wrap(Object source) {
		return wrap(source, AbstractEmbeddedWrapper.NO_REL);
	}

	/**
	 * Creates an {@link EmbeddedWrapper} for an empty {@link Collection} with the given element type.
	 *
	 * @param type must not be {@literal null}.
	 * @return
	 */
	public EmbeddedWrapper emptyCollectionOf(Class<?> type) {
		return new EmptyCollectionEmbeddedWrapper(type);
	}

	/**
	 * Creates a new {@link EmbeddedWrapper} with the given rel.
	 *
	 * @param source must not be {@literal null}.
	 * @param rel must not be {@literal null} or empty.
	 * @return will never be {@literal null}.
	 */
	public EmbeddedWrapper wrap(Object source, LinkRelation rel) {

		Assert.notNull(source, "Source must not be null!");
		Assert.notNull(rel, "Link relation must not be null!");

		if (source instanceof EmbeddedWrapper) {
			return (EmbeddedWrapper) source;
		}

		return source instanceof Collection //
				|| source instanceof Stream //
				|| preferCollections //
				|| SUPPLIER_OF_STREAM.isAssignableFrom(source.getClass()) //
						? new EmbeddedCollection(asCollection(source), rel) //
						: new EmbeddedElement(source, rel);
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> asCollection(@Nullable Object source) {

		if (source == null) {
			return Collections.emptyList();
		}

		if (Collection.class.isInstance(source)) {
			return Collection.class.cast(source);
		}

		if (Stream.class.isInstance(source)) {
			return (Collection<Object>) Stream.class.cast(source).collect(Collectors.toList());
		}

		if (source.getClass().isArray()) {
			return Arrays.asList((Object[]) source);
		}

		if (SUPPLIER_OF_STREAM.isInstance(source)) {
			return asCollection(((Supplier<Stream<?>>) source).get());
		}

		return Collections.singleton(source);
	}

	private static abstract class AbstractEmbeddedWrapper implements EmbeddedWrapper {

		private static final LinkRelation NO_REL = LinkRelation.of("___norel___");

		private final LinkRelation rel;

		/**
		 * Creates a new {@link AbstractEmbeddedWrapper} with the given rel.
		 *
		 * @param rel must not be {@literal null} or empty.
		 */
		public AbstractEmbeddedWrapper(LinkRelation rel) {

			Assert.notNull(rel, "Rel must not be null or empty!");
			this.rel = rel;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#getRel()
		 */
		@Override
		public Optional<LinkRelation> getRel() {

			return Optional.ofNullable(rel) //
					.filter(it -> !it.equals(NO_REL));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#hasRel(org.springframework.hateoas.LinkRelation)
		 */
		@Override
		public boolean hasRel(LinkRelation rel) {
			return this.rel.isSameAs(rel);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#getRelTargetType()
		 */
		@Override
		@SuppressWarnings("unchecked")
		@Nullable
		public Class<?> getRelTargetType() {

			Object peek = peek();

			peek = peek instanceof EntityModel ? ((EntityModel<Object>) peek).getContent() : peek;

			if (peek == null) {
				return null;
			}

			return AopUtils.getTargetClass(peek);
		}

		/**
		 * Peek into the wrapped element. The object returned is used to determine the actual value type of the wrapper.
		 *
		 * @return
		 */
		@Nullable
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
		 * Creates a new {@link EmbeddedElement} for the given value and link relation.
		 *
		 * @param value must not be {@literal null}.
		 * @param relation must not be {@literal null}.
		 */
		public EmbeddedElement(Object value, LinkRelation relation) {

			super(relation);
			Assert.notNull(value, "Value must not be null!");
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.hal.EmbeddedWrapper#getValue()
		 */
		@NonNull
		@Override
		public Object getValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.EmbeddedWrappers.AbstractElementWrapper#peek()
		 */
		@NonNull
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
		public EmbeddedCollection(Collection<Object> value, LinkRelation rel) {

			super(rel);

			Assert.notNull(value, "Collection must not be null!");

			if (AbstractEmbeddedWrapper.NO_REL.equals(rel) && value.isEmpty()) {
				throw new IllegalArgumentException("Cannot wrap an empty collection with no rel given!");
			}

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
		@Nullable
		protected Object peek() {
			return value.isEmpty() ? null : value.iterator().next();
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

	/**
	 * An {@link EmbeddedWrapper} to simulate a {@link Collection} of a given element type.
	 *
	 * @author Oliver Gierke
	 * @since 0.17
	 */
	private static class EmptyCollectionEmbeddedWrapper implements EmbeddedWrapper {

		private final Class<?> type;

		/**
		 * Creates a new {@link EmptyCollectionEmbeddedWrapper}.
		 *
		 * @param type must not be {@literal null}.
		 */
		public EmptyCollectionEmbeddedWrapper(Class<?> type) {

			Assert.notNull(type, "Element type must not be null!");

			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#getRel()
		 */
		@Override
		public Optional<LinkRelation> getRel() {
			return Optional.empty();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#getValue()
		 */
		@Override
		public Object getValue() {
			return Collections.emptySet();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#getRelTargetType()
		 */
		@NonNull
		@Override
		public Class<?> getRelTargetType() {
			return type;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#isCollectionValue()
		 */
		@Override
		public boolean isCollectionValue() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.core.EmbeddedWrapper#hasRel(org.springframework.hateoas.LinkRelation)
		 */
		@Override
		public boolean hasRel(LinkRelation rel) {
			return false;
		}
	}
}
