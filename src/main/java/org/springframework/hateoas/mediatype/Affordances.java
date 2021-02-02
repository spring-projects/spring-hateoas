/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.ResolvableType;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Primary API to construct {@link Affordance} instances.
 *
 * @author Oliver Drotbohm
 * @see #afford(HttpMethod)
 */
public class Affordances implements AffordanceOperations {

	private static List<AffordanceModelFactory> factories = SpringFactoriesLoader
			.loadFactories(AffordanceModelFactory.class, Affordance.class.getClassLoader());

	private final Link link;

	public static Affordances of(Link link) {
		return new Affordances(link);
	}

	private Affordances(Link link) {
		this.link = link;
	}

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
	 * @see org.springframework.hateoas.mediatype.AffordanceOperations#toLink()
	 */
	public Link toLink() {
		return link;
	}

	/**
	 * Builder API for {@link Affordance} instances.
	 *
	 * @author Oliver Drotbohm
	 * @deprecated since 1.3, refer to {@link ConfiguredAffordance} instead. Will be made private in 1.4.
	 * @see ConfigurableAffordance
	 * @see ConfiguredAffordance
	 */
	@Deprecated
	public static class AffordanceBuilder implements ConfigurableAffordance, ConfiguredAffordance {

		private final Affordances context;
		private final HttpMethod method;
		private final Link target;
		private final InputPayloadMetadata inputMetdata;
		private final PayloadMetadata outputMetadata;

		private List<QueryParameter> parameters = Collections.emptyList();
		private @Nullable String name;

		private AffordanceBuilder(Affordances context, HttpMethod method, Link target, InputPayloadMetadata inputMetdata,
				PayloadMetadata outputMetadata, List<QueryParameter> parameters, @Nullable String name) {

			this.context = context;
			this.method = method;
			this.target = target;
			this.inputMetdata = inputMetdata;
			this.outputMetadata = outputMetadata;
			this.parameters = parameters;
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInputAndOutput(java.lang.Class)
		 */
		@Override
		public AffordanceBuilder withInputAndOutput(Class<?> type) {
			return withInput(type).withOutput(type);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInputAndOutput(org.springframework.core.ResolvableType)
		 */
		@Override
		public AffordanceBuilder withInputAndOutput(ResolvableType type) {
			return withInput(type).withOutput(type);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInputAndOutput(org.springframework.hateoas.AffordanceModel.PayloadMetadata)
		 */
		@Override
		public AffordanceBuilder withInputAndOutput(PayloadMetadata metadata) {
			return withInput(metadata).withOutput(metadata);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInput(java.lang.Class)
		 */
		@Override
		public AffordanceBuilder withInput(Class<?> type) {

			Assert.notNull(type, "Type must not be null!");

			return withInput(ResolvableType.forClass(type));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInput(org.springframework.core.ResolvableType)
		 */
		@Override
		public AffordanceBuilder withInput(ResolvableType type) {

			Assert.notNull(type, "Type must not be null!");

			return withInput(PropertyUtils.getExposedProperties(type));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInput(org.springframework.hateoas.AffordanceModel.PayloadMetadata)
		 */
		@Override
		public AffordanceBuilder withInput(PayloadMetadata metadata) {

			InputPayloadMetadata inputMetadata = InputPayloadMetadata.from(metadata);

			return new AffordanceBuilder(context, method, target, inputMetadata, outputMetadata, parameters, name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withOutput(java.lang.Class)
		 */
		@Override
		public AffordanceBuilder withOutput(Class<?> type) {
			return withOutput(ResolvableType.forClass(type));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withOutput(org.springframework.core.ResolvableType)
		 */
		@Override
		public AffordanceBuilder withOutput(ResolvableType type) {
			return withOutput(PropertyUtils.getExposedProperties(type));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withOutput(org.springframework.hateoas.AffordanceModel.PayloadMetadata)
		 */
		@Override
		public AffordanceBuilder withOutput(PayloadMetadata metadata) {
			return new AffordanceBuilder(context, method, target, inputMetdata, metadata, parameters, name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInputMediaType(org.springframework.http.MediaType)
		 */
		@Override
		public ConfigurableAffordance withInputMediaType(MediaType inputMediaType) {
			return withInputMediaTypes(Arrays.asList(inputMediaType));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withInputMediaTypes(java.util.List)
		 */
		@Override
		public ConfigurableAffordance withInputMediaTypes(List<MediaType> inputMediaTypes) {
			return withInput(inputMetdata.withMediaTypes(inputMediaTypes));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withParameters(org.springframework.hateoas.QueryParameter[])
		 */
		@Override
		public AffordanceBuilder withParameters(QueryParameter... parameters) {
			return withParameters(Arrays.asList(parameters));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withParameters(java.util.List)
		 */
		@Override
		public AffordanceBuilder withParameters(List<QueryParameter> parameters) {
			return new AffordanceBuilder(context, method, target, inputMetdata, outputMetadata, parameters, name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#addParameters(org.springframework.hateoas.QueryParameter[])
		 */
		@Override
		public AffordanceBuilder addParameters(QueryParameter... parameters) {

			List<QueryParameter> newParameters = new ArrayList<>(this.parameters.size() + parameters.length);
			newParameters.addAll(this.parameters);
			newParameters.addAll(Arrays.asList(parameters));

			return new AffordanceBuilder(context, method, target, inputMetdata, outputMetadata, newParameters, name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withTarget(org.springframework.hateoas.Link)
		 */
		@Override
		public ConfigurableAffordance withTarget(Link target) {

			Assert.notNull(target, "Target must not be null!");

			return this.target == target ? this
					: new AffordanceBuilder(this.context, this.method, target, this.inputMetdata, this.outputMetadata,
							this.parameters, this.name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#withName(java.lang.String)
		 */
		@Override
		public ConfigurableAffordance withName(@Nullable String name) {

			return this.name == name ? this
					: new AffordanceBuilder(this.context, this.method, this.target, this.inputMetdata, this.outputMetadata,
							this.parameters, name);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#andAfford(org.springframework.http.HttpMethod)
		 */
		@Override
		public AffordanceBuilder andAfford(HttpMethod method) {
			return build().afford(method);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfigurableAffordance#build()
		 */
		@Override
		public Affordances build() {
			return Affordances.of(toLink());
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.AffordanceOperations#toLink()
		 */
		@Override
		public Link toLink() {
			return context.link.andAffordance(buildAffordance());
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfiguredAffordance#getNameOrDefault()
		 */
		public String getNameOrDefault() {

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

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfiguredAffordance#getMethod()
		 */
		@Override
		public HttpMethod getMethod() {
			return method;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfiguredAffordance#getInputMetadata()
		 */
		@Override
		public InputPayloadMetadata getInputMetadata() {
			return inputMetdata;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfiguredAffordance#getOutputMetadata()
		 */
		@Override
		public PayloadMetadata getOutputMetadata() {
			return outputMetadata;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfiguredAffordance#getTarget()
		 */
		@Override
		public Link getTarget() {
			return target;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.mediatype.ConfiguredAffordance#getQueryParameters()
		 */
		@Override
		public List<QueryParameter> getQueryParameters() {
			return parameters;
		}

		/**
		 * Builds an {@link Affordance} from the current state of the builder.
		 *
		 * @return must not be {@literal null}.
		 */
		private Affordance buildAffordance() {

			return factories.stream() //
					.collect(collectingAndThen(toMap(AffordanceModelFactory::getMediaType, //
							it -> it.getAffordanceModel(this)), Affordance::new));
		}
	}
}
