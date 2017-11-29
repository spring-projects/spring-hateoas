/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * Collection of components needed to serialize a HAL-FORMS document.
 *
 * @author Greg Turnquist
 */
class HalFormsSerializers {

	/**
	 * Serializer for {@link Resources}.
	 */
	static class HalFormsResourceSerializer extends ContainerSerializer<Resource<?>> implements ContextualSerializer {

		private static final long serialVersionUID = -7912243216469101379L;

		private final BeanProperty property;

		HalFormsResourceSerializer(BeanProperty property) {

			super(Resource.class, false);
			this.property = property;
		}

		HalFormsResourceSerializer() {
			this(null);
		}

		@Override
		public void serialize(Resource<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			HalFormsDocument<?> doc = HalFormsDocument.forResource(value.getContent()) //
					.withLinks(value.getLinks()) //
					.withTemplates(findTemplates(value));

			provider.findValueSerializer(HalFormsDocument.class, property).serialize(doc, gen, provider);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		public boolean hasSingleElement(Resource<?> resource) {
			return false;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
			return null;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalFormsResourceSerializer(property);
		}
	}

	/**
	 * Serializer for {@link Resources}
	 */
	static class HalFormsResourcesSerializer extends ContainerSerializer<Resources<?>> implements ContextualSerializer {

		private static final long serialVersionUID = -3601146866067500734L;

		private final BeanProperty property;
		private final Jackson2HalModule.EmbeddedMapper embeddedMapper;

		HalFormsResourcesSerializer(BeanProperty property, Jackson2HalModule.EmbeddedMapper embeddedMapper) {

			super(Resources.class, false);

			this.property = property;
			this.embeddedMapper = embeddedMapper;
		}

		HalFormsResourcesSerializer(Jackson2HalModule.EmbeddedMapper embeddedMapper) {
			this(null, embeddedMapper);
		}

		@Override
		public void serialize(Resources<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {

			Map<String, Object> embeddeds = embeddedMapper.map(value);

			HalFormsDocument<?> doc;

			if (value instanceof PagedResources) {

				doc = HalFormsDocument.empty() //
						.withEmbedded(embeddeds) //
						.withPageMetadata(((PagedResources<?>) value).getMetadata()) //
						.withLinks(value.getLinks()) //
						.withTemplates(findTemplates(value));

			} else {

				doc = HalFormsDocument.empty() //
						.withEmbedded(embeddeds) //
						.withLinks(value.getLinks()) //
						.withTemplates(findTemplates(value));
			}

			provider.findValueSerializer(HalFormsDocument.class, property).serialize(doc, gen, provider);
		}

		@Override
		public JavaType getContentType() {
			return null;
		}

		@Override
		public JsonSerializer<?> getContentSerializer() {
			return null;
		}

		@Override
		public boolean hasSingleElement(Resources<?> resources) {
			return resources.getContent().size() == 1;
		}

		@Override
		protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
			return null;
		}

		@Override
		public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
				throws JsonMappingException {
			return new HalFormsResourcesSerializer(property, embeddedMapper);
		}
	}

	/**
	 * Extract template details from a {@link ResourceSupport}'s {@link Affordance}s.
	 *
	 * @param resource
	 * @return
	 */
	private static Map<String, HalFormsTemplate> findTemplates(ResourceSupport resource) {

		Map<String, HalFormsTemplate> templates = new HashMap<String, HalFormsTemplate>();

		if (resource.hasLink(Link.REL_SELF)) {
			for (Affordance affordance : resource.getLink(Link.REL_SELF).map(Link::getAffordances)
					.orElse(Collections.emptyList())) {

				HalFormsAffordanceModel model = affordance.getAffordanceModel(MediaTypes.HAL_FORMS_JSON);

				if (!affordance.getHttpMethod().equals(HttpMethod.GET)) {

					validate(resource, affordance, model);

					HalFormsTemplate template = HalFormsTemplate.forMethod(affordance.getHttpMethod()) //
							.withProperties(model.getProperties());

					/**
					 * First template in HAL-FORMS is "default".
					 */
					templates.put(templates.isEmpty() ? "default" : affordance.getName(), template);
				}
			}
		}

		return templates;
	}

	/**
	 * Verify that the resource's self link and the affordance's URI have the same relative path.
	 * 
	 * @param resource
	 * @param affordance
	 * @param model
	 */
	private static void validate(ResourceSupport resource, Affordance affordance, HalFormsAffordanceModel model) {

		try {

			Optional<Link> selfLink = resource.getLink(Link.REL_SELF);
			URI selfLinkUri = new URI(selfLink.map(link -> link.expand().getHref()).orElse(""));

			if (!model.hasPath(selfLinkUri.getPath())) {
				throw new IllegalStateException("Affordance's URI " + model.getPath() + " doesn't match self link "
						+ selfLinkUri.getPath() + " as expected in HAL-FORMS");
			}

		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}
