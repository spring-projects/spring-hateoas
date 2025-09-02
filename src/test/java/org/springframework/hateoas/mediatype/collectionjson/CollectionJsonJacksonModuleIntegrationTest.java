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

import static org.assertj.core.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tools.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.MappingTestUtils;
import org.springframework.hateoas.MappingTestUtils.ContextualMapper;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.SimplePojo;

/**
 * Integration test for Jackson 2 JSON+Collection
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class CollectionJsonJacksonModuleIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			Link.of("localhost", IanaLinkRelations.SELF), //
			Link.of("foo", IanaLinkRelations.NEXT), //
			Link.of("bar", IanaLinkRelations.PREV));

	ContextualMapper $ = MappingTestUtils.createMapper(mapper -> mapper.addModule(new CollectionJsonJacksonModule())
			.enable(SerializationFeature.INDENT_OUTPUT));

	@Test
	void rendersSingleLinkAsObject() {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(Link.of("localhost").withSelfRel());

		assertThat($.writeObject(resourceSupport)).isEqualTo($.readFileContent("resource-support.json"));
	}

	@Test
	void deserializeSingleLink() {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(Link.of("localhost"));

		assertThat($.readFile("resource-support.json")).isEqualTo(expected);
	}

	@Test
	void rendersMultipleLinkAsArray() {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(Link.of("localhost"));
		resourceSupport.add(Link.of("localhost2").withRel("orders"));

		assertThat($.writeObject(resourceSupport)).isEqualTo($.readFileContent("resource-support-2.json"));
	}

	@Test
	void rendersResourceSupportBasedObject() {

		ResourceWithAttributes resource = new ResourceWithAttributes("test value");
		resource.add(Link.of("localhost").withSelfRel());

		assertThat($.writeObject(resource)).isEqualTo($.readFileContent("resource-support-3.json"));
	}

	@Test
	void deserializeResourceSupportBasedObject() {

		ResourceWithAttributes expected = new ResourceWithAttributes("test value");
		expected.add(Link.of("localhost").withSelfRel());

		assertThat($.readFile("resource-support-3.json", ResourceWithAttributes.class)).isEqualTo(expected);
	}

	@Test
	void deserializeMultipleLinks() {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(Link.of("localhost"));
		expected.add(Link.of("localhost2").withRel("orders"));

		assertThat($.readFile("resource-support-2.json").getLinks()).containsAll(expected.getLinks());
	}

	@Test
	void rendersSimpleResourcesAsEmbedded() {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> resources = CollectionModel.of(content);
		resources.add(Link.of("localhost"));

		assertThat($.writeObject(resources)).isEqualTo($.readFileContent("resources.json"));
	}

	@Test
	void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> expected = CollectionModel.of(content);
		expected.add(Link.of("localhost"));

		CollectionModel<String> result = $.readFile("resources.json", CollectionModel.class, String.class);

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void renderResource() {

		assertThat($.writeObject(EntityModel.of("first", Link.of("localhost")))) //
				.isEqualTo($.readFileContent("resource.json"));
	}

	@Test
	void deserializeResource() {

		EntityModel<String> actual = $.readFile("resource.json", EntityModel.class, String.class);

		assertThat(actual).isEqualTo(EntityModel.of("first", Link.of("localhost")));
	}

	@Test
	void renderComplexStructure() throws Exception {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		CollectionModel<EntityModel<String>> resources = CollectionModel.of(data);
		resources.add(Link.of("localhost"));
		resources.add(Link.of("/page/2").withRel("next"));

		assertThat($.writeObject(resources)).isEqualTo($.readFileContent("resources-with-resource-objects.json"));
	}

	@Test
	void deserializeResources() {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(EntityModel.of("first", Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of("second", Link.of("remotehost"), Link.of("order").withRel("orders")));

		CollectionModel<?> expected = CollectionModel.of(data);
		expected.add(Link.of("localhost"));
		expected.add(Link.of("/page/2").withRel("next"));

		CollectionModel<?> actual = $.readFile("resources-with-resource-objects.json", CollectionModel.class,
				EntityModel.class, String.class);

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void renderSimplePojos() throws Exception {

		List<EntityModel<SimplePojo>> data = new ArrayList<>();
		data.add(EntityModel.of(new SimplePojo("text", 1), Link.of("localhost"), Link.of("orders").withRel("orders")));
		data.add(EntityModel.of(new SimplePojo("text2", 2), Link.of("localhost")));

		CollectionModel<EntityModel<SimplePojo>> resources = CollectionModel.of(data);
		resources.add(Link.of("localhost"));
		resources.add(Link.of("/page/2").withRel("next"));

		assertThat($.writeObject(resources)).isEqualTo($.readFileContent("resources-simple-pojos.json"));
	}

	@Test
	void serializesPagedResource() throws Exception {

		assertThat($.writeObject(setupAnnotatedPagedResources())) //
				.isEqualTo($.readFileContent("paged-resources.json"));
	}

	@Test
	void deserializesPagedResource() throws Exception {

		PagedModel<?> result = $.readFile("paged-resources.json", PagedModel.class, EntityModel.class,
				SimplePojo.class);

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	private static CollectionModel<EntityModel<SimplePojo>> setupAnnotatedPagedResources() {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(EntityModel.of(new SimplePojo("test1", 1), Link.of("localhost")));
		content.add(EntityModel.of(new SimplePojo("test2", 2), Link.of("localhost")));

		return PagedModel.of(content, null, PAGINATION_LINKS);
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ResourceWithAttributes extends RepresentationModel<ResourceWithAttributes> {

		private String attribute;
	}

}
