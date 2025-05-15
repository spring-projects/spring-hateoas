/*
 * Copyright 2016-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import tools.jackson.core.Version;
import tools.jackson.databind.annotation.JsonAppend;
import tools.jackson.databind.annotation.JsonAppend.Prop;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.CollectionModelMixin;
import org.springframework.hateoas.mediatype.hal.LinkMixin;
import org.springframework.hateoas.server.mvc.JacksonSerializers.MediaTypeDeserializer;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Serialize / deserialize all the parts of HAL-FORMS documents using Jackson.
 *
 * @author Dietrich Schulten
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
public class HalFormsJacksonModule extends SimpleModule {

	private static final long serialVersionUID = -4496351128468451196L;

	public HalFormsJacksonModule() {

		super("hal-forms-module", new Version(1, 0, 0, null, "org.springframework.hateoas", "spring-hateoas"));

		setMixInAnnotation(Link.class, LinkMixin.class);
		setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
		setMixInAnnotation(CollectionModel.class, CollectionModelMixin.class);
		setMixInAnnotation(MediaType.class, MediaTypeMixin.class);
	}

	@JsonAppend(
			props = @Prop(name = "_templates", value = HalFormsTemplatePropertyWriter.class, include = Include.NON_EMPTY))
	abstract class RepresentationModelMixin extends org.springframework.hateoas.mediatype.hal.RepresentationModelMixin {}

	@JsonSerialize(using = ToStringSerializer.class)
	@JsonDeserialize(using = MediaTypeDeserializer.class)
	interface MediaTypeMixin {}
}
