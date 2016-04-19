package org.springframework.hateoas.hal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.forms.Template;
import org.springframework.hateoas.forms.TemplatedResources;
import org.springframework.hateoas.hal.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import org.springframework.hateoas.mvc.ControllerFormBuilderUnitTest.Item;
import org.springframework.web.bind.annotation.RequestMethod;

public class Jackson2HalFormsIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	private static final String RESOURCE_WITH_DEFAULT_TEMPLATE = "{\"_embedded\":{\"content\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\"}}}";

	private static final String RESOURCE_WITH_TWO_TEMPLATES = "{\"_embedded\":{\"content\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\"},\"update\":{\"method\":\"PUT\"}}}";

	@Before
	public void setUpModule() {

		mapper.registerModule(new Jackson2HalModule());
		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(new AnnotationRelProvider(), null, null, true));
	}

	@Test
	public void testDefaultTemplateAddsLinkSelfAndDefaultTemplate() throws Exception {
		List<Item> items = Arrays.asList(new Item(1L, "item1"), new Item(2L, "item2"));
		TemplatedResources<Item> resources = new TemplatedResources<Item>(items);

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });
		resources.add(template);
		assertThat(write(resources), is(RESOURCE_WITH_DEFAULT_TEMPLATE));
	}

	@Test
	public void testMultipleTemplateWithSameHrefAddsOneLinkSelf() throws Exception {
		List<Item> items = Arrays.asList(new Item(1L, "item1"), new Item(2L, "item2"));
		TemplatedResources<Item> resources = new TemplatedResources<Item>(items);

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });
		resources.add(template);

		Template template2 = new Template("localhost", "update");
		template2.setMethod(new RequestMethod[] { RequestMethod.PUT });
		resources.add(template2);

		assertThat(write(resources), is(RESOURCE_WITH_TWO_TEMPLATES));
	}

}
