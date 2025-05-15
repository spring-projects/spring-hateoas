/*
 * Copyright 2015-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.collectionjson;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonJacksonModule.CollectionJsonLinksDeserializer;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonJacksonModule.CollectionJsonLinksSerializer;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonJacksonModule.CollectionJsonResourceSupportDeserializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson 2 mixin to invoke the related serializer/deserializer.
 *
 * @author Greg Turnquist
 * @author Jens Schauder
 */
@JsonDeserialize(using = CollectionJsonResourceSupportDeserializer.class)
abstract class RepresentationModelMixin extends RepresentationModel<RepresentationModelMixin> {

	@Override
	@JsonProperty("collection")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonSerialize(using = CollectionJsonLinksSerializer.class)
	@JsonDeserialize(using = CollectionJsonLinksDeserializer.class)
	public abstract Links getLinks();

}
