/*
 * Copyright 2015 the original author or authors.
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

import static org.springframework.hateoas.collectionjson.Jackson2CollectionJsonModule.*;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Jackson 2 mixin to invoke the related serializer/deserializer.
 *
 * @author Greg Turnquist
 */
@JsonDeserialize(using = CollectionJsonResourceSupportDeserializer.class)
abstract class ResourceSupportMixin extends ResourceSupport {

	@Override
	@XmlElement(name = "collection")
	@JsonProperty("collection")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonSerialize(using = CollectionJsonLinkListSerializer.class)
	@JsonDeserialize(using = CollectionJsonLinkListDeserializer.class)
	public abstract List<Link> getLinks();


}
