/*
 * Copyright 2015-2020 the original author or authors.
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

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Helper to easily create {@link ParameterizedTypeReference} instances to Spring HATEOAS resource types. They're
 * basically a shortcut over using a verbose
 * {@code new ParameterizedTypeReference<CollectionRepresentationModel<DomainType>>()}.
 *
 * @author Oliver Gierke
 * @since 0.17
 */
public class TypeReferences {

	/**
	 * A {@link ParameterizedTypeReference} to return a {@link org.springframework.hateoas.EntityModel} of some type.
	 *
	 * @author Oliver Gierke
	 * @since 0.17
	 */
	public static class EntityModelType<T>
			extends SyntheticParameterizedTypeReference<org.springframework.hateoas.EntityModel<T>> {}

	/**
	 * A {@link ParameterizedTypeReference} to return a {@link org.springframework.hateoas.CollectionModel} of some type.
	 *
	 * @author Oliver Gierke
	 * @since 0.17
	 */
	public static class CollectionModelType<T>
			extends SyntheticParameterizedTypeReference<org.springframework.hateoas.CollectionModel<T>> {}

	/**
	 * A {@link ParameterizedTypeReference} to return a {@link org.springframework.hateoas.PagedModel} of some type.
	 *
	 * @author Oliver Gierke
	 * @since 0.17
	 */
	public static class PagedModelType<T>
			extends SyntheticParameterizedTypeReference<org.springframework.hateoas.PagedModel<T>> {}

	/**
	 * Special {@link ParameterizedTypeReference} to customize the generic type detection and eventually return a
	 * synthetic {@link ParameterizedType} to represent the resource type along side its generic parameter.
	 *
	 * @author Oliver Gierke
	 * @since 0.17
	 */
	private static abstract class SyntheticParameterizedTypeReference<T> extends ParameterizedTypeReference<T> {

		private final Type type;

		@SuppressWarnings("rawtypes")
		SyntheticParameterizedTypeReference() {

			Class<? extends SyntheticParameterizedTypeReference> foo = getClass();
			Type genericSuperclass = foo.getGenericSuperclass();
			ParameterizedType bar = (ParameterizedType) genericSuperclass;
			Type domainType = bar.getActualTypeArguments()[0];

			Class<?> parameterizedTypeReferenceSubclass = findParameterizedTypeReferenceSubclass(getClass());
			Type type = parameterizedTypeReferenceSubclass.getGenericSuperclass();
			Assert.isInstanceOf(ParameterizedType.class, type);
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Assert.isTrue(parameterizedType.getActualTypeArguments().length == 1,
					String.format("Type must have exactly one generic type argument but has %s.",
							parameterizedType.getActualTypeArguments().length));

			Class<?> resourceType = GenericTypeResolver.resolveType(parameterizedType.getActualTypeArguments()[0],
					new HashMap<>());

			this.type = new SyntheticParameterizedType(resourceType, domainType);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.ParameterizedTypeReference#getType()
		 */
		@Override
		public Type getType() {
			return this.type;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.ParameterizedTypeReference#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(@Nullable Object obj) {
			return this == obj || obj instanceof SyntheticParameterizedTypeReference
					&& this.type.equals(((SyntheticParameterizedTypeReference<?>) obj).type);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.ParameterizedTypeReference#hashCode()
		 */
		@Override
		public int hashCode() {
			return this.type.hashCode();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.core.ParameterizedTypeReference#toString()
		 */
		@Override
		public String toString() {
			return "SyntheticParameterizedTypeReference<" + this.type + ">";
		}

		private static Class<?> findParameterizedTypeReferenceSubclass(Class<?> child) {

			Class<?> parent = child.getSuperclass();
			if (Object.class.equals(parent)) {
				throw new IllegalStateException("Expected SyntheticParameterizedTypeReference superclass");
			} else if (SyntheticParameterizedTypeReference.class.equals(parent)) {
				return child;
			} else {
				return findParameterizedTypeReferenceSubclass(parent);
			}
		}
	}

	/**
	 * A synthetic {@link ParameterizedType}.
	 *
	 * @author Oliver Gierke
	 * @since 0.17
	 */
	private static final class SyntheticParameterizedType implements ParameterizedType, Serializable {

		private static final long serialVersionUID = -521679299810654826L;

		private final Type rawType;
		private final Type[] typeArguments;

		SyntheticParameterizedType(Type rawType, Type... typeArguments) {

			this.rawType = rawType;
			this.typeArguments = typeArguments;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.reflect.ParameterizedType#getActualTypeArguments()
		 */
		@Override
		public Type[] getActualTypeArguments() {
			return this.typeArguments;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.reflect.ParameterizedType#getRawType()
		 */
		@Override
		public Type getRawType() {
			return this.rawType;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.reflect.ParameterizedType#getOwnerType()
		 */
		@Override
		@Nullable
		public Type getOwnerType() {
			return null;
		}
	}
}
