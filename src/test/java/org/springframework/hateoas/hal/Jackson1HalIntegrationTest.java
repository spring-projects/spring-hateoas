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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.AbstractMarshallingIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.hal.Jackson1HalModule.HalHandlerInstantiator;

/**
 * Integration tests for Jackson 1 based HAL integration.
 * 
 * @author Alexander Baetz
 * @author Oliver Gierke
 */
@SuppressWarnings("deprecation")
public class Jackson1HalIntegrationTest extends AbstractMarshallingIntegrationTest {

	static final String SINGLE_LINK_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}}}";
	static final String LIST_LINK_REFERENCE = "{\"_links\":{\"self\":[{\"href\":\"localhost\"},{\"href\":\"localhost2\"}]}}";

	static final String SIMPLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"content\":[\"first\",\"second\"]}}";
	static final String SINGLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"content\":{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}}}}";
	static final String LIST_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"content\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]}}";

	static final String ANNOTATED_EMBEDDED_RESOURCE_REFERENCE = "{\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_embedded\":{\"pojo\":{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}}}}";
	static final String ANNOTATED_EMBEDDED_RESOURCES_REFERENCE = "{\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]}}";

	static final String ANNOTATED_PAGED_RESOURCES = "{\"_links\":{\"next\":{\"href\":\"foo\"},\"prev\":{\"href\":\"bar\"}},\"_embedded\":{\"pojos\":[{\"text\":\"test1\",\"number\":1,\"_links\":{\"self\":{\"href\":\"localhost\"}}},{\"text\":\"test2\",\"number\":2,\"_links\":{\"self\":{\"href\":\"localhost\"}}}]},\"page\":{\"size\":2,\"totalElements\":4,\"totalPages\":2,\"number\":0}}";

	static final Links PAGINATION_LINKS = new Links(new Link("foo", Link.REL_NEXT), new Link("bar", Link.REL_PREVIOUS));

	@Before
	public void setUpModule() {

		mapper.registerModule(new Jackson1HalModule());
		mapper.setHandlerInstantiator(new HalHandlerInstantiator(new AnnotationRelProvider()));
	}

	/**
	 * @see #29
	 */
	@Test
	public void rendersSingleLinkAsObject() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport), is(SINGLE_LINK_REFERENCE));
	}

	@Test
	public void deserializeSingleLink() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));

		assertThat(read(SINGLE_LINK_REFERENCE, ResourceSupport.class), is(expected));
	}

	/**
	 * @see #29
	 */
	@Test
	public void rendersMultipleLinkAsArray() throws Exception {

		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport), is(LIST_LINK_REFERENCE));
	}

	@Test
	public void deserializeMultipleLinks() throws Exception {

		ResourceSupport expected = new ResourceSupport();
		expected.add(new Link("localhost"));
		expected.add(new Link("localhost2"));

		assertThat(read(LIST_LINK_REFERENCE, ResourceSupport.class), is(expected));
	}

	@Test
	public void rendersSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> resources = new Resources<String>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources), is(SIMPLE_EMBEDDED_RESOURCE_REFERENCE));
	}

	@Test
	public void deserializesSimpleResourcesAsEmbedded() throws Exception {

		List<String> content = new ArrayList<String>();
		content.add("first");
		content.add("second");

		Resources<String> expected = new Resources<String>(content);
		expected.add(new Link("localhost"));

		Resources<String> result = mapper.readValue(SIMPLE_EMBEDDED_RESOURCE_REFERENCE, mapper.getTypeFactory()
				.constructParametricType(Resources.class, String.class));

		assertThat(result, is(expected));
	}

	@Test
	public void rendersSingleResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimplePojo>> content = new ArrayList<Resource<SimplePojo>>();
		content.add(new Resource<SimplePojo>(new SimplePojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimplePojo>> resources = new Resources<Resource<SimplePojo>>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources), is(SINGLE_EMBEDDED_RESOURCE_REFERENCE));
	}

	@Test
	public void deserializesSingleResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimplePojo>> content = new ArrayList<Resource<SimplePojo>>();
		content.add(new Resource<SimplePojo>(new SimplePojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimplePojo>> expected = new Resources<Resource<SimplePojo>>(content);
		expected.add(new Link("localhost"));

		Resources<Resource<SimplePojo>> result = mapper.readValue(
				SINGLE_EMBEDDED_RESOURCE_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class)));

		assertThat(result, is(expected));
	}

	@Test
	public void rendersMultipleResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimplePojo>> resources = setupResources();
		resources.add(new Link("localhost"));

		assertThat(write(resources), is(LIST_EMBEDDED_RESOURCE_REFERENCE));
	}

	@Test
	public void deserializeMultipleResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimplePojo>> expected = setupResources();
		expected.add(new Link("localhost"));

		Resources<Resource<SimplePojo>> result = mapper.readValue(
				LIST_EMBEDDED_RESOURCE_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimplePojo.class)));

		assertThat(result, is(expected));
	}

	@Test
	public void rendersAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimpleAnnotatedPojo>> resources = new Resources<Resource<SimpleAnnotatedPojo>>(content);
		resources.add(new Link("localhost"));

		assertThat(write(resources), is(ANNOTATED_EMBEDDED_RESOURCE_REFERENCE));
	}

	@Test
	public void deserializesAnnotatedResourceResourcesAsEmbedded() throws Exception {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));

		Resources<Resource<SimpleAnnotatedPojo>> expected = new Resources<Resource<SimpleAnnotatedPojo>>(content);
		expected.add(new Link("localhost"));

		Resources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(
				ANNOTATED_EMBEDDED_RESOURCE_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result, is(expected));
	}

	/**
	 * @see #63
	 */
	@Test
	public void serializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {
		assertThat(write(setupAnnotatedResources()), is(ANNOTATED_EMBEDDED_RESOURCES_REFERENCE));
	}

	/**
	 * @see #63
	 */
	@Test
	public void deserializesMultipleAnnotatedResourceResourcesAsEmbedded() throws Exception {

		Resources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(
				ANNOTATED_EMBEDDED_RESOURCES_REFERENCE,
				mapper.getTypeFactory().constructParametricType(Resources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result, is(setupAnnotatedResources()));
	}

	@Test
	public void serializesPagedResource() throws Exception {
		assertThat(write(setupAnnotatedPagedResources()), is(ANNOTATED_PAGED_RESOURCES));
	}

	@Test
	public void deserializesPagedResource() throws Exception {
		PagedResources<Resource<SimpleAnnotatedPojo>> result = mapper.readValue(
				ANNOTATED_PAGED_RESOURCES,
				mapper.getTypeFactory().constructParametricType(PagedResources.class,
						mapper.getTypeFactory().constructParametricType(Resource.class, SimpleAnnotatedPojo.class)));

		assertThat(result, is(setupAnnotatedPagedResources()));
	}

	private static Resources<Resource<SimpleAnnotatedPojo>> setupAnnotatedPagedResources() {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new PagedResources<Resource<SimpleAnnotatedPojo>>(content, new PageMetadata(2, 0, 4), PAGINATION_LINKS);
	}

	private static Resources<Resource<SimpleAnnotatedPojo>> setupAnnotatedResources() {

		List<Resource<SimpleAnnotatedPojo>> content = new ArrayList<Resource<SimpleAnnotatedPojo>>();
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test1", 1), new Link("localhost")));
		content.add(new Resource<SimpleAnnotatedPojo>(new SimpleAnnotatedPojo("test2", 2), new Link("localhost")));

		return new Resources<Resource<SimpleAnnotatedPojo>>(content);
	}

	private static Resources<Resource<SimplePojo>> setupResources() {

		List<Resource<SimplePojo>> content = new ArrayList<Resource<SimplePojo>>();
		content.add(new Resource<SimplePojo>(new SimplePojo("test1", 1), new Link("localhost")));
		content.add(new Resource<SimplePojo>(new SimplePojo("test2", 2), new Link("localhost")));

		return new Resources<Resource<SimplePojo>>(content);
	}
}
