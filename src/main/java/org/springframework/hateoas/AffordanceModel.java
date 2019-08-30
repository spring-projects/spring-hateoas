/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.hateoas;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

/**
 * Collection of attributes needed to render any form of hypermedia.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
@EqualsAndHashCode
@AllArgsConstructor
@Getter
public abstract class AffordanceModel {

	/**
	 * Name for the REST action of this resource.
	 */
	private String name;

	/**
	 * {@link Link} for the URI of the resource.
	 */
	private Link link;

	/**
	 * Request method verb for this resource. For multiple methods, add multiple {@link Affordance}s.
	 */
	private HttpMethod httpMethod;

	/**
	 * Domain type used to create a new resource.
	 */
	private InputPayloadMetadata input;

	/**
	 * Collection of {@link QueryParameter}s to interrogate a resource.
	 */
	private List<QueryParameter> queryMethodParameters;

	/**
	 * Response body domain type.
	 */
	private PayloadMetadata output;

	/**
	 * Expand the {@link Link} into an {@literal href} with no parameters.
	 *
	 * @return
	 */
	public String getURI() {
		return this.link.expand().getHref();
	}

	/**
	 * Returns whether the {@link Affordance} has the given {@link HttpMethod}.
	 *
	 * @param method must not be {@literal null}.
	 * @return
	 */
	public boolean hasHttpMethod(HttpMethod method) {

		Assert.notNull(method, "HttpMethod must not be null!");

		return this.httpMethod.equals(method);
	}

	/**
	 * Returns whether the {@link Affordance} points to the target of the given {@link Link}.
	 *
	 * @param link must not be {@literal null}.
	 * @return
	 */
	public boolean pointsToTargetOf(Link link) {

		Assert.notNull(link, "Link must not be null!");

		return getURI().equals(link.expand().getHref());
	}

	/**
	 * Metadata about payloads.
	 *
	 * @author Oliver Drotbohm
	 */
	public interface PayloadMetadata {

		PayloadMetadata NONE = NoPayloadMetadata.INSTANCE;

		/**
		 * Returns all properties contained in a payload.
		 *
		 * @return
		 */
		Stream<PropertyMetadata> stream();

		default Optional<PropertyMetadata> getPropertyMetadata(String name) {
			return stream().filter(it -> it.hasName(name)).findFirst();
		}
	}

	/**
	 * Payload metadata for incoming requests.
	 *
	 * @author Oliver Drotbohm
	 */
	public interface InputPayloadMetadata extends PayloadMetadata {

		InputPayloadMetadata NONE = from(PayloadMetadata.NONE);

		static InputPayloadMetadata from(PayloadMetadata metadata) {

			return InputPayloadMetadata.class.isInstance(metadata) //
					? InputPayloadMetadata.class.cast(metadata)
					: DelegatingInputPayloadMetadata.of(metadata);
		}

		/**
		 * Applies the {@link InputPayloadMetadata} to the given target.
		 *
		 * @param <T>
		 * @param target
		 * @return
		 */
		<T extends PropertyMetadataConfigured<T> & Named> T applyTo(T target);

		<T extends Named> T customize(T target, Function<PropertyMetadata, T> customizer);

		/**
		 * Returns the I18n codes to be used to resolve a name for the payload metadata.
		 *
		 * @return
		 */
		List<String> getI18nCodes();
	}

	/**
	 * {@link InputPayloadMetadata} to delegate to a target {@link PayloadMetadata} not applying any customizations.
	 *
	 * @author Oliver Drotbohm
	 */
	@ToString
	@EqualsAndHashCode
	@RequiredArgsConstructor(staticName = "of")
	private static class DelegatingInputPayloadMetadata implements InputPayloadMetadata {

		private final PayloadMetadata metadata;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PayloadMetadata#stream()
		 */
		@Override
		public Stream<PropertyMetadata> stream() {
			return metadata.stream();
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.InputPayloadMetadata#customize(org.springframework.hateoas.AffordanceModel.PropertyMetadataConfigured)
		 */
		@Override
		public <T extends PropertyMetadataConfigured<T> & Named> T applyTo(T target) {
			return target;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.InputPayloadMetadata#customize(org.springframework.hateoas.AffordanceModel.Named, java.util.function.Function)
		 */
		@Override
		public <T extends Named> T customize(T target, Function<PropertyMetadata, T> customizer) {
			return target;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.InputPayloadMetadata#getI18nCodes()
		 */
		@Override
		public List<String> getI18nCodes() {
			return Collections.emptyList();
		}
	}

	/**
	 * Metadata about the property model of a representation.
	 *
	 * @author Oliver Drotbohm
	 */
	public interface PropertyMetadata {

		/**
		 * The name of the property.
		 *
		 * @return will never be {@literal null} or empty.
		 */
		String getName();

		/**
		 * Whether the property has the given name.
		 *
		 * @param name must not be {@literal null} or empty.
		 * @return
		 */
		default boolean hasName(String name) {

			Assert.hasText(name, "Name must not be null or empty!");

			return getName().equals(name);
		}

		/**
		 * Whether the property is required to be submitted or always present in the representation returned.
		 *
		 * @return
		 */
		boolean isRequired();

		/**
		 * Whether the property is read only, i.e. must not be manipulated in requests modifying state.
		 *
		 * @return
		 */
		boolean isReadOnly();

		/**
		 * Returns the (regular expression) pattern the property has to adhere to.
		 *
		 * @return will never be {@literal null}.
		 */
		Optional<String> getPattern();

		/**
		 * Return the type of the property. If no type can be determined, return {@link Object}.
		 *
		 * @return
		 */
		ResolvableType getType();
	}

	/**
	 * SPI for a type that can get {@link PropertyMetadata} applied.
	 *
	 * @author Oliver Drotbohm
	 */
	public interface PropertyMetadataConfigured<T> {

		/**
		 * Applies the given {@link PropertyMetadata}.
		 *
		 * @param metadata will never be {@literal null}.
		 * @return
		 */
		T apply(PropertyMetadata metadata);
	}

	/**
	 * A named component.
	 *
	 * @author Oliver Drotbohm
	 */
	public interface Named {
		String getName();
	}

	/**
	 * Empty {@link PayloadMetadata}.
	 *
	 * @author Oliver Drotbohm
	 */
	private enum NoPayloadMetadata implements PayloadMetadata {

		INSTANCE;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.AffordanceModel.PayloadMetadata#stream()
		 */
		@Override
		public Stream<PropertyMetadata> stream() {
			return Stream.empty();
		}
	}
}
