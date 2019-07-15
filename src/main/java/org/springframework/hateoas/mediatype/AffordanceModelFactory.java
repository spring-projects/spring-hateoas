/*
 * Copyright 2017-2019 the original author or authors.
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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
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
	 */
	AffordanceModel getAffordanceModel(String name, Link link, HttpMethod httpMethod, InputPayloadMetadata inputType,
			List<QueryParameter> queryMethodParameters, PayloadMetadata outputType);
}
