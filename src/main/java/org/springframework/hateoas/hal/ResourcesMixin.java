/*
 * Copyright 2012-2013 the original author or authors.
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
package org.springframework.hateoas.hal;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.EmbeddedResource;
import org.springframework.hateoas.Resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonPropertyOrder({ "content", "links" })
public abstract class ResourcesMixin<T> extends Resources<T> {

	@Override
	@XmlElement(name = "embedded")
	@JsonProperty("_embedded")
	@JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY, using = Jackson2HalModule.HalResourcesSerializer.class)
	@JsonDeserialize(using = Jackson2HalModule.HalResourcesDeserializer.class)
	public abstract Collection<T> getContent();

	@Override
	@JsonIgnore
	public abstract List<EmbeddedResource> getEmbeddedResources();

}
