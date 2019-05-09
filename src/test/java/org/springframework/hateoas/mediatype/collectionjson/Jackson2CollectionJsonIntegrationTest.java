/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.hal.SimplePojo;
import org.springframework.hateoas.support.MappingUtils;

import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Integration test for Jackson 2 JSON+Collection
 *
 * @author Greg Turnquist
 * @author Oliver Drotbohm
 */
class Jackson2CollectionJsonIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final Links PAGINATION_LINKS = Links.of( //
			new Link("localhost", IanaLinkRelations.SELF), //
			new Link("foo", IanaLinkRelations.NEXT), //
			new Link("bar", IanaLinkRelations.PREV));

	@BeforeEach
	void setUpModule() {

		mapper.registerModule(new Jackson2CollectionJsonModule());
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	void rendersSingleLinkAsObject() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())));
	}

	@Test
	void deserializeSingleLink() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(new Link("localhost"));

		assertThat(
				read(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())), RepresentationModel.class))
						.isEqualTo(expected);
	}

	@Test
	void rendersMultipleLinkAsArray() throws Exception {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2").withRel("orders"));

		assertThat(write(resourceSupport))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())));
	}

	@Test
	void rendersResourceSupportBasedObject() throws Exception {

		ResourceWithAttributes resource = new ResourceWithAttributes("test value");
		resource.add(new Link("localhost").withSelfRel());

		assertThat(write(resource))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resource-support-3.json", getClass())));
	}

	@Test
	void deserializeResourceSupportBasedObject() throws Exception {

		ResourceWithAttributes expected = new ResourceWithAttributes("test value");
		expected.add(new Link("localhost").withSelfRel());

		assertThat(read(MappingUtils.read(new ClassPathResource("resource-support-3.json", getClass())),
				ResourceWithAttributes.class)).isEqualTo(expected);
	}

	@Test
	void deserializeMultipleLinks() throws Exception {

		RepresentationModel<?> expected = new RepresentationModel<>();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2").withRel("orders"));

		String read = MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass()));
		RepresentationModel<?> readResourceSupport = read(read, RepresentationModel.class);

		assertThat(readResourceSupport.getLinks()).containsAll(expected.getLinks());
	}

	@Test
	void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> resources = new CollectionModel<>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources)).isEqualTo(MappingUtils.read(new ClassPathResource("resources.json", getClass())));
	}

	@Test
	void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<>();
		content.add("first");
		content.add("second");

		CollectionModel<String> expected = new CollectionModel<>(content);
		expected.add(new Link("localhost"));

		CollectionModel<String> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class, String.class));

		assertThat(result).isEqualTo(expected);
	}

	@Test
	void renderResource() throws Exception {

		EntityModel<String> data = new EntityModel<>("first", new Link("localhost"));

		assertThat(write(data)).isEqualTo(MappingUtils.read(new ClassPathResource("resource.json", getClass())));
	}

	@Test
	void deserializeResource() throws Exception {

		EntityModel<?> expected = new EntityModel<>("first", new Link("localhost"));

		String source = MappingUtils.read(new ClassPathResource("resource.json", getClass()));
		EntityModel<String> actual = mapper.readValue(source,
				mapper.getTypeFactory().constructParametricType(EntityModel.class, String.class));

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void renderComplexStructure() throws Exception {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(new EntityModel<>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new EntityModel<>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		CollectionModel<EntityModel<String>> resources = new CollectionModel<>(
				data);
		resources.add(new Link("localhost"));
		resources.add(new Link("/page/2").withRel("next"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())));
	}

	@Test
	void deserializeResources() throws Exception {

		List<EntityModel<String>> data = new ArrayList<>();
		data.add(new EntityModel<>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new EntityModel<>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		CollectionModel<?> expected = new CollectionModel<>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		CollectionModel<EntityModel<String>> actual = mapper.readValue(
				MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
				mapper.getTypeFactory().constructParametricType(CollectionModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, String.class)));

		assertThat(actual).isEqualTo(expected);

	}

	@Test
	void renderSimplePojos() throws Exception {

		List<EntityModel<SimplePojo>> data = new ArrayList<>();
		data.add(new EntityModel<>(new SimplePojo("text", 1), new Link("localhost"),
				new Link("orders").withRel("orders")));
		data.add(new EntityModel<>(new SimplePojo("text2", 2), new Link("localhost")));

		CollectionModel<EntityModel<SimplePojo>> resources = new CollectionModel<>(
				data);
		resources.add(new Link("localhost"));
		resources.add(new Link("/page/2").withRel("next"));

		assertThat(write(resources))
				.isEqualTo(MappingUtils.read(new ClassPathResource("resources-simple-pojos.json", getClass())));
	}

	@Test
	void serializesPagedResource() throws Exception {

		String actual = write(setupAnnotatedPagedResources());
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())));
	}

	@Test
	void deserializesPagedResource() throws Exception {

		PagedModel<EntityModel<SimplePojo>> result = mapper.readValue(
				MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedModel.class,
						mapper.getTypeFactory().constructParametricType(EntityModel.class, SimplePojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	private static CollectionModel<EntityModel<SimplePojo>> setupAnnotatedPagedResources() {

		List<EntityModel<SimplePojo>> content = new ArrayList<>();
		content.add(new EntityModel<>(new SimplePojo("test1", 1), new Link("localhost")));
		content.add(new EntityModel<>(new SimplePojo("test2", 2), new Link("localhost")));

		return new PagedModel<>(content, null, PAGINATION_LINKS);
	}

	@Data
	@EqualsAndHashCode(callSuper = true)
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ResourceWithAttributes extends RepresentationModel<ResourceWithAttributes> {

		private String attribute;
	}

}
