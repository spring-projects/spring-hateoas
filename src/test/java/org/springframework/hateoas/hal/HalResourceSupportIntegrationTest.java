package org.springframework.hateoas.hal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.springframework.hateoas.AbstractMarshallingIntegrationTests;
import org.springframework.hateoas.AbstractResourceSupport;
import org.springframework.hateoas.Link;

public class HalResourceSupportIntegrationTest extends AbstractMarshallingIntegrationTests {

	static final String SINGLE_LINK_REFERENCE = "{\"_links\":{\"self\":{\"rel\":\"self\",\"href\":\"localhost\"}}}";
	static final String LIST_LINK_REFERENCE = "{\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";
	static final String SINGLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"test\":{}},\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";
	static final String LIST_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"test\":[{},{}]},\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";

	@Test
	public void rendersSingleLinkAsObject() throws Exception {
		HalResourceSupport resourceSupport = new HalResourceSupport();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport), is(SINGLE_LINK_REFERENCE));
	}

	@Test
	public void rendersMultipleLinkAsArray() throws Exception {
		AbstractResourceSupport resourceSupport = new HalResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport), is(LIST_LINK_REFERENCE));
	}

	@Test
	public void rendersSingleEmbeddedResourceAsObject() throws Exception {
		HalResourceSupport resourceSupport = new HalResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));
		resourceSupport.addEmbeddedResource("test", new HalResourceSupport());

		assertThat(write(resourceSupport), is(SINGLE_EMBEDDED_RESOURCE_REFERENCE));

	}

	@Test
	public void rendersMultipleEmbeddedResourcesAsArray() throws Exception {
		HalResourceSupport resourceSupport = new HalResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));
		resourceSupport.addEmbeddedResource("test", new HalResourceSupport());
		resourceSupport.addEmbeddedResource("test", new HalResourceSupport());

		assertThat(write(resourceSupport), is(LIST_EMBEDDED_RESOURCE_REFERENCE));

	}

}
