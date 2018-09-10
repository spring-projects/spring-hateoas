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
package org.springframework.hateoas.mediatype.collectionjson;

import static org.assertj.core.api.Assertions.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mediatype.hal.SimplePojo;
import org.springframework.hateoas.support.MappingUtils;

import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Integration test for Jackson 2 JSON+Collection
 *
 * @author Greg Turnquist
 */
public class Jackson2CollectionJsonIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	static final Links PAGINATION_LINKS = new Links(
		new Link("localhost", Link.REL_SELF),
		new Link("foo", Link.REL_NEXT),
		new Link("bar", Link.REL_PREVIOUS));

	@Before
	public void setUpModule() {

		mapper.registerModule(new Jackson2CollectionJsonModule());
		mapper.setHandlerInstantiator(new Jackson2CollectionJsonModule.CollectionJsonHandlerInstantiator(null));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost").withSelfRel());

		assertThat(write(resourceSupport)).isEqualTo(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())));
	}

	@Test
	public void deserializeSingleLink() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));

		assertThat(read(MappingUtils.read(new ClassPathResource("resource-support.json", getClass())), ResourceSupport.class))
			.isEqualTo(expected);
	}

	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2").withRel("orders"));

		assertThat(write(resourceSupport)).isEqualTo(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())));
	}

	@Test
	public void rendersResourceSupportBasedObject() throws Exception {

		ResourceWithAttributes resource = new ResourceWithAttributes("test value");
		resource.add(new Link("localhost").withSelfRel());

		assertThat(write(resource)).isEqualTo(MappingUtils.read(new ClassPathResource("resource-support-3.json", getClass())));
	}

	@Test
	public void deserializeResourceSupportBasedObject() throws Exception {

		ResourceWithAttributes expected = new ResourceWithAttributes("test value");
		expected.add(new Link("localhost").withSelfRel());

		assertThat(read(MappingUtils.read(new ClassPathResource("resource-support-3.json", getClass())), ResourceWithAttributes.class))
			.isEqualTo(expected);
	}

	@Test
	public void deserializeMultipleLinks() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2").withRel("orders"));

		assertThat(read(MappingUtils.read(new ClassPathResource("resource-support-2.json", getClass())), ResourceSupport.class))
			.isEqualTo(expected);
	}

	@Test
	public void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> resources = new Resources<String>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources)).isEqualTo(MappingUtils.read(new ClassPathResource("resources.json", getClass())));
	}

	@Test
	public void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> expected = new Resources<String>(content);
		expected.add(new Link("localhost"));

		Resources<String> result = mapper.readValue(MappingUtils.read(new ClassPathResource("resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class, String.class));

		assertThat(result).isEqualTo(expected);
	}


	@Test
	public void renderResource() throws Exception {

		Resource<String> data = new Resource<String>("first", new Link("localhost"));

		assertThat(write(data)).isEqualTo(MappingUtils.read(new ClassPathResource("resource.json", getClass())));
	}

	@Test
	public void deserializeResource() throws Exception {

		Resource expected = new Resource<>("first", new Link("localhost"));

		String source = MappingUtils.read(new ClassPathResource("resource.json", getClass()));
		Resource<String> actual = mapper.readValue(source, mapper.getTypeFactory().constructParametricType(Resource.class, String.class));
		
		assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void renderComplexStructure() throws Exception {

		List<Resource<String>> data = new ArrayList<Resource<String>>();
		data.add(new Resource<String>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<String>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources<Resource<String>> resources = new Resources<Resource<String>>(data);
		resources.add(new Link("localhost"));
		resources.add(new Link("/page/2").withRel("next"));

		assertThat(write(resources)).isEqualTo(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())));
	}

	@Test
	public void deserializeResources() throws Exception {

		List<Resource<String>> data = new ArrayList<Resource<String>>();
		data.add(new Resource<String>("first", new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<String>("second", new Link("remotehost"), new Link("order").withRel("orders")));

		Resources expected = new Resources<Resource<String>>(data);
		expected.add(new Link("localhost"));
		expected.add(new Link("/page/2").withRel("next"));

		Resources<Resource<String>> actual = mapper.readValue(MappingUtils.read(new ClassPathResource("resources-with-resource-objects.json", getClass())),
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, String.class)));

		assertThat(actual).isEqualTo(expected);

	}

	@Test
	public void renderSimplePojos() throws Exception {

		List<Resource<SimplePojo>> data = new ArrayList<Resource<SimplePojo>>();
		data.add(new Resource<>(new SimplePojo("text", 1), new Link("localhost"), new Link("orders").withRel("orders")));
		data.add(new Resource<>(new SimplePojo("text2", 2), new Link("localhost")));

		Resources<Resource<SimplePojo>> resources = new Resources<Resource<SimplePojo>>(data);
		resources.add(new Link("localhost"));
		resources.add(new Link("/page/2").withRel("next"));

		assertThat(write(resources)).isEqualTo(MappingUtils.read(new ClassPathResource("resources-simple-pojos.json", getClass())));
	}

	@Test
	public void serializesPagedResource() throws Exception {

		String actual = write(setupAnnotatedPagedResources());
		assertThat(actual).isEqualTo(MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())));
	}

	@Test
	public void deserializesPagedResource() throws Exception {

		PagedResources<Resource<SimplePojo>> result = mapper.readValue(MappingUtils.read(new ClassPathResource("paged-resources.json", getClass())),
				mapper.getTypeFactory().constructParametricType(PagedResources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class)));

		assertThat(result).isEqualTo(setupAnnotatedPagedResources());
	}

	private static Resources<Resource<SimplePojo>> setupAnnotatedPagedResources() {

		List<Resource<SimplePojo>> content = new ArrayList<Resource<SimplePojo>>();
		content.add(new Resource<>(new SimplePojo("test1", 1), new Link("localhost")));
		content.add(new Resource<>(new SimplePojo("test2", 2), new Link("localhost")));

		return new PagedResources<>(content, null, PAGINATION_LINKS);
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ResourceWithAttributes extends ResourceSupport {
		
		private String attribute;
	}

}
