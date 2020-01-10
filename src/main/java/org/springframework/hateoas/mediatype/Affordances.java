/*
 * Copyright 2019-2020 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import static java.util.stream.Collectors.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Wither;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Primary API to construct {@link Affordance} instances.
 *
 * @author Oliver Drotbohm
 * @see #afford(HttpMethod)
 */
@RequiredArgsConstructor(staticName = "of")
public class Affordances implements AffordanceOperations {

	private static List<AffordanceModelFactory> factories = SpringFactoriesLoader
			.loadFactories(AffordanceModelFactory.class, Affordance.class.getClassLoader());

	private final Link link;

	/**
	 * Returns all {@link Affordance}s created.
	 *
	 * @return will never be {@literal null}.
	 */
	public Stream<Affordance> stream() {
		return link.getAffordances().stream();
	}

	/**
	 * Creates a new {@link AffordanceBuilder} for the given HTTP method for further customization. See the wither-methods
	 * for details.
	 *
	 * @param httpMethod must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	public AffordanceBuilder afford(HttpMethod httpMethod) {

		Assert.notNull(httpMethod, "HTTP method must not be null!");

		return new AffordanceBuilder(this, httpMethod, link, InputPayloadMetadata.NONE, PayloadMetadata.NONE,
				Collections.emptyList(), null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.mediatype.TerminalOperations#build()
	 */
	public Link toLink() {
		return link;
	}

	/**
	 * Builder API for {@link Affordance} instances.
	 *
	 * @author Oliver Drotbohm
	 */
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class AffordanceBuilder implements AffordanceOperations {

		private final Affordances context;

		private final HttpMethod method;
		private final @Wither Link target;
		private final InputPayloadMetadata inputMetdata;
		private final PayloadMetadata outputMetadata;

		private List<QueryParameter> parameters = Collections.emptyList();
		private @Nullable @Wither String name;

		/**
		 * Registers the given type as input and output model for the affordance.
		 *
		 * @param type must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withInputAndOutput(Class<?> type) {
			return withInput(type).withOutput(type);
		}

		/**
		 * Registers the given {@link ResolvableType} as input and output model for the affordance.
		 *
		 * @param type must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withInputAndOutput(ResolvableType type) {
			return withInput(type).withOutput(type);
		}

		/**
		 * Registers the given {@link PayloadMetadata} as input and output model.
		 *
		 * @param metadata must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withInputAndOutput(PayloadMetadata metadata) {
			return withInput(metadata).withOutput(metadata);
		}

		/**
		 * Registers the given type as input model for the affordance.
		 *
		 * @param type must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withInput(Class<?> type) {

			Assert.notNull(type, "Type must not be null!");

			return withInput(ResolvableType.forClass(type));
		}

		/**
		 * Registers the given {@link ResolvableType} as input model for the affordance.
		 *
		 * @param type must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withInput(ResolvableType type) {

			Assert.notNull(type, "Type must not be null!");

			return withInput(PropertyUtils.getExposedProperties(type));
		}

		/**
		 * Registers the given {@link PayloadMetadata} as input model.
		 *
		 * @param metadata must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withInput(PayloadMetadata metadata) {

			InputPayloadMetadata inputMetadata = InputPayloadMetadata.from(metadata);

			return new AffordanceBuilder(context, method, target, inputMetadata, outputMetadata, parameters, name);
		}

		/**
		 * Registers the given type as the output model.
		 *
		 * @param type must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withOutput(Class<?> type) {
			return withOutput(ResolvableType.forClass(type));
		}

		/**
		 * Registers the given {@link ResolvableType} as the output model.
		 *
		 * @param type must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withOutput(ResolvableType type) {
			return withOutput(PropertyUtils.getExposedProperties(type));
		}

		/**
		 * Registers the given {@link PayloadMetadata} as output model.
		 *
		 * @param metadata must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withOutput(PayloadMetadata metadata) {
			return new AffordanceBuilder(context, method, target, inputMetdata, metadata, parameters, name);
		}

		/**
		 * Replaces the current {@link QueryParameters} with the given ones.
		 *
		 * @param parameters must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withParameters(QueryParameter... parameters) {
			return withParameters(Arrays.asList(parameters));
		}

		/**
		 * Replaces the current {@link QueryParameters} with the given ones.
		 *
		 * @param parameters must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder withParameters(List<QueryParameter> parameters) {
			return new AffordanceBuilder(context, method, target, inputMetdata, outputMetadata, parameters, name);
		}

		/**
		 * Adds the given {@link QueryParameter}s to the {@link Affordance} to build.
		 *
		 * @param parameters must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		public AffordanceBuilder addParameters(QueryParameter... parameters) {

			List<QueryParameter> newParameters = new ArrayList<>(this.parameters.size() + parameters.length);
			newParameters.addAll(this.parameters);
			newParameters.addAll(Arrays.asList(parameters));

			return new AffordanceBuilder(context, method, target, inputMetdata, outputMetadata, newParameters, name);
		}

		/**
		 * Concludes the creation of the current {@link Affordance} to build and starts a new one.
		 *
		 * @param method must not be {@literal null}.
		 * @return
		 * @see #build()
		 * @see #toLink()
		 */
		public AffordanceBuilder andAfford(HttpMethod method) {
			return build().afford(method);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.AffordanceOperations#toLink()
		 */
		@Override
		public Link toLink() {
			return context.link.andAffordance(buildAffordance());
		}

		/**
		 * Builds the {@link Affordance} currently under construction and returns in alongside the ones already contained in
		 * the {@link Link} the buildup started from.
		 *
		 * @return will never be {@literal null}.
		 */
		public Affordances build() {
			return Affordances.of(toLink());
		}

		/**
		 * Builds an {@link Affordance} from the current state of the builder.
		 *
		 * @return must not be {@literal null}.
		 */
		private Affordance buildAffordance() {

			return factories.stream() //
					.collect(collectingAndThen(toMap(AffordanceModelFactory::getMediaType, //
							it -> createModel(it, parameters == null ? Collections.emptyList() : parameters)), //
							Affordance::new));
		}

		/**
		 * Creates a new {@link AffordanceModel} using the given {@link AffordanceModelFactory} and {@link QueryParameter}s.
		 *
		 * @param factory must not be {@literal null}.
		 * @param parameters must not be {@literal null}.
		 * @return will never be {@literal null}.
		 */
		private AffordanceModel createModel(AffordanceModelFactory factory, List<QueryParameter> parameters) {
			return factory.getAffordanceModel(getNameOrDefault(), target, method, inputMetdata, parameters, outputMetadata);
		}

		/**
		 * Returns the explicitly configured name of the {@link Affordance} or calculates a default based on the
		 * {@link HttpMethod} and type backing it.
		 *
		 * @return
		 */
		private String getNameOrDefault() {

			if (name != null) {
				return name;
			}

			String name = method.toString().toLowerCase();

			ResolvableType type = TypeBasedPayloadMetadata.class.isInstance(inputMetdata) //
					? TypeBasedPayloadMetadata.class.cast(inputMetdata).getType() //
					: null;

			if (type == null) {
				return name;
			}

			Class<?> resolvedType = type.resolve();

			return resolvedType == null ? name : name.concat(resolvedType.getSimpleName());
		}
	}
}
