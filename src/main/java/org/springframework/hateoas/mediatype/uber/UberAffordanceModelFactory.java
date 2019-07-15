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
package org.springframework.hateoas.mediatype.uber;

import lombok.Getter;

import java.util.List;

import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.mediatype.AffordanceModelFactory;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * {@link AffordanceModelFactory} for {@literal UBER+JSON}.
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class UberAffordanceModelFactory implements AffordanceModelFactory {

	private final @Getter MediaType mediaType = MediaTypes.UBER_JSON;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.AffordanceModelFactory#getAffordanceModel(java.lang.String, org.springframework.hateoas.Link, org.springframework.http.HttpMethod, org.springframework.hateoas.mediatype.PayloadMetadata, java.util.List, org.springframework.core.ResolvableType)
	 */
	@Override
	public AffordanceModel getAffordanceModel(String name, Link link, HttpMethod httpMethod,
			InputPayloadMetadata inputType, List<QueryParameter> queryMethodParameters, PayloadMetadata outputType) {
		return new UberAffordanceModel(name, link, httpMethod, inputType, queryMethodParameters, outputType);
	}
}
