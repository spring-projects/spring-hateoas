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
package org.springframework.hateoas.mediatype;

import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.Affordances.AffordanceBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;

/**
 * An affordance in creation. Superseding {@link AffordanceBuilder} to build up affordances manually to clearly
 * distinguish between building the affordance and consuming the configured state.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 */
public interface ConfigurableAffordance extends AffordanceOperations {

	/**
	 * Registers the given type as input and output model for the affordance.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInputAndOutput(Class<?> type);

	/**
	 * Registers the given {@link ResolvableType} as input and output model for the affordance.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInputAndOutput(ResolvableType type);

	/**
	 * Registers the given {@link PayloadMetadata} as input and output model.
	 *
	 * @param metadata must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInputAndOutput(PayloadMetadata metadata);

	/**
	 * Registers the given type as input model for the affordance.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInput(Class<?> type);

	/**
	 * Registers the given {@link ResolvableType} as input model for the affordance.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInput(ResolvableType type);

	/**
	 * Registers the given {@link PayloadMetadata} as input model.
	 *
	 * @param metadata must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInput(PayloadMetadata metadata);

	/**
	 * Registers the given type as the output model.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withOutput(Class<?> type);

	/**
	 * Registers the given {@link ResolvableType} as the output model.
	 *
	 * @param type must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withOutput(ResolvableType type);

	/**
	 * Registers the given {@link PayloadMetadata} as output model.
	 *
	 * @param metadata must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withOutput(PayloadMetadata metadata);

	/**
	 * Registers the input to expect to be of the given {@link MediaType}.
	 *
	 * @param inputMediaType can be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInputMediaType(MediaType inputMediaType);

	/**
	 * Registers the given {@link MediaType}s as input payload media types.
	 *
	 * @param inputMediaTypes must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withInputMediaTypes(List<MediaType> inputMediaTypes);

	/**
	 * Replaces the current {@link QueryParameter} list with the given ones.
	 *
	 * @param parameters must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withParameters(QueryParameter... parameters);

	/**
	 * Replaces the current {@link QueryParameter} list with the given ones.
	 *
	 * @param parameters must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance withParameters(List<QueryParameter> parameters);

	/**
	 * Adds the given {@link QueryParameter}s to the {@link Affordance} to build.
	 *
	 * @param parameters must not be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	ConfigurableAffordance addParameters(QueryParameter... parameters);

	/**
	 * Concludes the creation of the current {@link Affordance} to build and starts a new one.
	 *
	 * @param method must not be {@literal null}.
	 * @return
	 * @see #build()
	 * @see #toLink()
	 */
	ConfigurableAffordance andAfford(HttpMethod method);

	/**
	 * Builds the {@link Affordance} currently under construction and returns in alongside the ones already contained in
	 * the {@link Link} the buildup started from.
	 *
	 * @return will never be {@literal null}.
	 */
	Affordances build();

	/**
	 * Create a new {@link AffordanceBuilder} by copying all attributes and replacing the {@literal target}.
	 *
	 * @param target
	 * @return
	 */
	ConfigurableAffordance withTarget(Link target);

	/**
	 * Create a new {@link AffordanceBuilder} by copying all attributes and replacing the {@literal name}.
	 *
	 * @param name can be {@literal null}.
	 * @return
	 */
	ConfigurableAffordance withName(@Nullable String name);
}
