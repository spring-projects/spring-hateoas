/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.collectionjson;

import lombok.Getter;

import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.hateoas.GenericAffordanceModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.core.AffordanceModelFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Factory for creating {@link CollectionJsonAffordanceModel}s.
 *
 * @author Greg Turnquist
 */
class CollectionJsonAffordanceModelFactory implements AffordanceModelFactory {

	private final @Getter MediaType mediaType = MediaTypes.COLLECTION_JSON;

	@Override
	public GenericAffordanceModel getAffordanceModel(String name, Link link, HttpMethod httpMethod, ResolvableType inputType, List<QueryParameter> queryMethodParameters, ResolvableType outputType) {
		return new CollectionJsonAffordanceModel(name, link, httpMethod, inputType, queryMethodParameters, outputType);
	}
}
