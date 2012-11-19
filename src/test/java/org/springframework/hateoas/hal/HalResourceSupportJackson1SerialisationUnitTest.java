package org.springframework.hateoas.hal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTests;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.hal.jackson2.HalJackson2Module;

public class HalResourceSupportJackson1SerialisationUnitTest extends AbstractJackson2MarshallingIntegrationTests {

	static final String SINGLE_LINK_REFERENCE = "{\"_links\":{\"self\":{\"rel\":\"self\",\"href\":\"localhost\"}}}";
	static final String LIST_LINK_REFERENCE = "{\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";
	static final String SINGLE_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"test\":{}},\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";
	static final String LIST_EMBEDDED_RESOURCE_REFERENCE = "{\"_embedded\":{\"test\":[{},{}]},\"_links\":{\"self\":[{\"rel\":\"self\",\"href\":\"localhost\"},{\"rel\":\"self\",\"href\":\"localhost2\"}]}}";

	@Before
	public void setUpModule() {
		mapper.registerModule(new HalJackson2Module());
	}

	@Test
	public void rendersSingleLinkAsObject() throws Exception {
		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));

		assertThat(write(resourceSupport), is(SINGLE_LINK_REFERENCE));
	}

	@Test
	public void rendersMultipleLinkAsArray() throws Exception {
		ResourceSupport resourceSupport = new ResourceSupport();
		resourceSupport.add(new Link("localhost"));
		resourceSupport.add(new Link("localhost2"));

		assertThat(write(resourceSupport), is(LIST_LINK_REFERENCE));
	}
}
