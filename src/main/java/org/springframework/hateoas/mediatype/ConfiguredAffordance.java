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

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;

/**
 * A configure affordance for inspection by media type implementations to create {@link AffordanceModel} instances.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 * @see AffordanceModelFactory#getAffordanceModel(ConfiguredAffordance)
 */
public interface ConfiguredAffordance {

	/**
	 * Returns the explicitly configured name of the {@link Affordance} or calculates a default based on the
	 * {@link HttpMethod} and type backing it.
	 *
	 * @return will never be {@literal null}.
	 */
	String getNameOrDefault();

	/**
	 * Returns the affordance's target.
	 *
	 * @return will never be {@literal null}.
	 */
	Link getTarget();

	/**
	 * The {@link HttpMethod} of the affordance.
	 *
	 * @return will never be {@literal null}.
	 */
	HttpMethod getMethod();

	/**
	 * Metadata about the input payload.
	 *
	 * @return will never be {@literal null}.
	 */
	InputPayloadMetadata getInputMetadata();

	/**
	 * The parameters of the affordance.
	 *
	 * @return will never be {@literal null}.
	 */
	List<QueryParameter> getQueryParameters();

	/**
	 * Metadata about the output payload.
	 *
	 * @return will never be {@literal null}.
	 */
	PayloadMetadata getOutputMetadata();
}
