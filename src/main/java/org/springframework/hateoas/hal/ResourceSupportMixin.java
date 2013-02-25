/*
 * Copyright 2012 the original author or authors.
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

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

abstract class ResourceSupportMixin extends ResourceSupport {

	@Override
	@XmlElement(name = "link")
	@org.codehaus.jackson.annotate.JsonProperty("_links")
	@com.fasterxml.jackson.annotation.JsonProperty("_links")
	@org.codehaus.jackson.map.annotate.JsonSerialize(include = org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion.NON_EMPTY, using = org.springframework.hateoas.hal.Jackson1HalModule.HalLinkListSerializer.class)
	@org.codehaus.jackson.map.annotate.JsonDeserialize(using = org.springframework.hateoas.hal.Jackson1HalModule.HalLinkListDeserializer.class)
	@com.fasterxml.jackson.databind.annotation.JsonSerialize(include = com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion.NON_EMPTY, using = org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListSerializer.class)
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = org.springframework.hateoas.hal.Jackson2HalModule.HalLinkListDeserializer.class)
	public abstract List<Link> getLinks();
}
