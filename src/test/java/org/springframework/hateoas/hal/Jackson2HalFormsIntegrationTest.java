package org.springframework.hateoas.hal;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.hateoas.AbstractJackson2MarshallingIntegrationTest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.forms.LinkSuggest;
import org.springframework.hateoas.forms.Property;
import org.springframework.hateoas.forms.Template;
import org.springframework.hateoas.forms.TemplatedResources;
import org.springframework.hateoas.forms.ValueSuggest;
import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;
import org.springframework.hateoas.hal.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import org.springframework.hateoas.mvc.ControllerFormBuilderUnitTest.Item;
import org.springframework.hateoas.mvc.ControllerFormBuilderUnitTest.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Jackson2HalFormsIntegrationTest extends AbstractJackson2MarshallingIntegrationTest {

	private static final String DEFAULT_TEMPLATE = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\"}}}";

	private static final String TWO_TEMPLATES = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\"},\"update\":{\"method\":\"PUT\"}}}";

	private static final String TEMPLATE_AND_CONTENT_TYPE = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\",\"contentType\":\"application/x-www-form-urlencoded\"}}}";

	private static final String TEMPLATE_AND_TITLE = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\",\"contentType\":\"application/x-www-form-urlencoded\",\"title\":\"Default's title!\"}}}";

	private static final String TEMPLATE_AND_SIMPLE_PROPERTY = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\",\"properties\":[{\"name\":\"name\",\"readOnly\":true,\"prompt\":\"Item Name\"}]}}}";

	private static final String TEMPLATE_AND_PROPERTY_WITH_DIRECT_SUGGEST = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\",\"properties\":[{\"name\":\"name\",\"suggest\":[{\"value\":\"big\",\"prompt\":\"Big size\"},{\"value\":\"small\",\"prompt\":\"Small size\"}]}]}}}";

	private static final String TEMPLATE_AND_PROPERTY_WITH_EMBEDDED_SUGGEST = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\",\"properties\":[{\"name\":\"name\",\"suggest\":{\"embedded\":\"sizeList\",\"prompt-field\":\"desc\",\"value-field\":\"id\"}}]}}}";

	private static final String TEMPLATE_AND_PROPERTY_WITH_LINK_SUGGEST = "{\"_embedded\":{\"itemList\":[{\"id\":1,\"name\":\"item1\"},{\"id\":2,\"name\":\"item2\"}]},\"_links\":{\"self\":{\"href\":\"localhost\"}},\"_templates\":{\"default\":{\"method\":\"POST\",\"properties\":[{\"name\":\"name\",\"suggest\":{\"href\":\"localhost\",\"value-field\":\"id\",\"prompt-field\":\"name\"}}]}}}";

	private TemplatedResources<Item> resources;

	@Before
	public void setUpModule() {

		mapper.registerModule(new Jackson2HalModule());
		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(new DefaultRelProvider(), null, null, true));

		List<Item> items = Arrays.asList(new Item(1L, "item1"), new Item(2L, "item2"));
		resources = new TemplatedResources<Item>(items);
	}

	@Test
	public void testDefaultTemplateAddsLinkSelfAndDefaultTemplate() throws Exception {

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });
		resources.add(template);
		assertThat(write(resources), is(DEFAULT_TEMPLATE));
	}

	@Test
	public void testMultipleTemplateWithSameHrefAddsOneLinkSelf() throws Exception {

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });
		resources.add(template);

		Template template2 = new Template("localhost", "update");
		template2.setMethod(new RequestMethod[] { RequestMethod.PUT });
		resources.add(template2);

		assertThat(write(resources), is(TWO_TEMPLATES));
	}

	@Test
	public void testTemplateWithContentType() throws Exception {

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });
		template.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		resources.add(template);

		assertThat(write(resources), is(TEMPLATE_AND_CONTENT_TYPE));
	}

	@Test
	public void testTemplateWithTitle() throws Exception {

		LocaleContextHolder.setLocale(Locale.US);

		StaticMessageSource messageSource = new StaticMessageSource();
		messageSource.addMessage("_templates.default.title", Locale.US, "Default's title!");

		ObjectMapper objectMapper = getCuriedObjectMapper(null, messageSource);

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });
		template.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		resources.add(template);

		Writer writer = new StringWriter();
		objectMapper.writeValue(writer, resources);

		assertThat(writer.toString(), is(TEMPLATE_AND_TITLE));
	}

	@Test
	public void testTemplateWithPropertyWithNoSuggest() throws Exception {

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });

		List<Property> properties = Arrays.asList(new Property("name", true, false, null, "Item Name", null, null));
		template.setProperties(properties);

		resources.add(template);

		assertThat(write(resources), is(TEMPLATE_AND_SIMPLE_PROPERTY));
	}

	@Test
	public void testTemplateWithPropertyWithValueDirectSuggest() throws Exception {

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });

		ValueSuggest<Size> suggest = new ValueSuggest<Size>(
				Arrays.asList(new Size("big", "Big size"), new Size("small", "Small size")), "desc", "id");
		List<Property> properties = Arrays.asList(new Property("name", null, false, null, null, null, suggest));
		template.setProperties(properties);

		resources.add(template);

		assertThat(write(resources), is(TEMPLATE_AND_PROPERTY_WITH_DIRECT_SUGGEST));
	}

	@Test
	public void testTemplateWithPropertyWithValueEmbeddedSuggest() throws Exception {

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });

		ValueSuggest<Size> suggest = new ValueSuggest<Size>(
				Arrays.asList(new Size("big", "Big size"), new Size("small", "Small size")), "desc", "id",
				ValueSuggestType.EMBEDDED);
		List<Property> properties = Arrays.asList(new Property("name", null, false, null, null, null, suggest));
		template.setProperties(properties);

		resources.add(template);

		assertThat(write(resources), is(TEMPLATE_AND_PROPERTY_WITH_EMBEDDED_SUGGEST));
	}

	@Test
	public void testTemplateWithPropertyWithLinkSuggest() throws Exception {

		Template template = new Template("localhost", "default");
		template.setMethod(new RequestMethod[] { RequestMethod.POST });

		LinkSuggest suggest = new LinkSuggest(new Link("localhost", "sizes"), "name", "id");
		List<Property> properties = Arrays.asList(new Property("name", null, false, null, null, null, suggest));
		template.setProperties(properties);

		resources.add(template);

		assertThat(write(resources), is(TEMPLATE_AND_PROPERTY_WITH_LINK_SUGGEST));
	}

	private static ObjectMapper getCuriedObjectMapper(CurieProvider provider, MessageSource messageSource) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new Jackson2HalModule());
		mapper.registerModule(new Jackson2HalFormsModule());
		mapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(new DefaultRelProvider(), provider,
				messageSource == null ? null : new MessageSourceAccessor(messageSource), true));

		return mapper;
	}
}
