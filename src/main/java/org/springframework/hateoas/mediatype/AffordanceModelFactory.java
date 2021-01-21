/*
 * Copyright 2017-2021 the original author or authors.
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

import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;

/**
 * SPI for media type implementations to create a specific {@link AffordanceModel} for a {@link ConfiguredAffordance}.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public interface AffordanceModelFactory {

	/**
	 * Declare the {@link MediaType} this factory supports.
	 *
	 * @return
	 */
	MediaType getMediaType();

	/**
	 * Look up the {@link AffordanceModel} for this factory.
	 *
	 * @param name
	 * @param link
	 * @param httpMethod
	 * @param inputType
	 * @param queryMethodParameters
	 * @param outputType
	 * @return
	 * @deprecated since 1.3 in favor of {@link #getAffordanceModel(ConfiguredAffordance)}. Will be removed in 1.4.
	 */
	@Deprecated
	default AffordanceModel getAffordanceModel(String name, Link link, HttpMethod httpMethod,
			InputPayloadMetadata inputType,
			List<QueryParameter> queryMethodParameters, PayloadMetadata outputType) {
		throw new IllegalStateException(
				"This method needs to be implemented unless you implement getAffordanceModel(ConfiguredAffordance)!");
	}

	/**
	 * Return the {@link AffordanceModel} for the given {@link ConfiguredAffordance}.
	 *
	 * @param configured will never be {@literal null}.
	 * @return must not be {@literal null}.
	 * @since 1.3
	 */
	default AffordanceModel getAffordanceModel(ConfiguredAffordance configured) {

		Assert.notNull(configured, "Configured affordance must not be null!");

		return getAffordanceModel(configured.getNameOrDefault(), configured.getTarget(), configured.getMethod(),
				configured.getInputMetadata(), configured.getQueryParameters(), configured.getOutputMetadata());
	}
}
