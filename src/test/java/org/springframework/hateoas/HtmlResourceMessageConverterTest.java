package org.springframework.hateoas;

import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.xpath;
import static org.springframework.test.web.server.setup.MockMvcBuilders.webApplicationContextSetup;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.sample.SamplePersonController;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests html form message creation for /customer resource on {@link SamplePersonController}.
 * 
 * @author Dietrich Schulten
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
// for Spring 3.2, delete GenericWebContextLoader and WebContextLoader
// adjust the package names of static imports to
// org.springframework.test.web.servlet
// then use WebAppConfiguration and ContextConfiguration like this:
// @WebAppConfiguration
// @ContextConfiguration
@ContextConfiguration(loader = WebContextLoader.class)
public class HtmlResourceMessageConverterTest {
	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;
	private static Map<String, String> namespaces = new HashMap<String, String>();

	static {
		namespaces.put("h", "http://www.w3.org/1999/xhtml");
	}

	@Before
	public void setup() {
		this.mockMvc = webApplicationContextSetup(this.wac).build();

	}

	@Test
	public void testCreatesHtmlForm() throws Exception {
		this.mockMvc.perform(get("/people/customer").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(content().mimeType(MediaType.TEXT_HTML))
				.andExpect(xpath("h:html/h:body/h:form/@action", namespaces).string("/people/customer"))
				.andExpect(xpath("//h:form/@name", namespaces).string("searchPerson"))
				.andExpect(xpath("//h:form/@method", namespaces).string("GET"));
	}

	@Test
	public void testCreatesInputFieldWithDefaultNumber() throws Exception {

		this.mockMvc.perform(get("/people/customer").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().mimeType(MediaType.TEXT_HTML))
				.andExpect(xpath("//h:input/@name", namespaces).string("personId"))
				.andExpect(xpath("//h:input/@type", namespaces).string("number"))
				.andExpect(xpath("//h:input/@value", namespaces).string("1234"));
	}

	@Test
	public void testCreatesInputFieldWithDefaultText() throws Exception {

		this.mockMvc.perform(get("/people/customerByName").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().mimeType(MediaType.TEXT_HTML))
				.andExpect(xpath("//h:input/@name", namespaces).string("name"))
				.andExpect(xpath("//h:input/@type", namespaces).string("text"))
				.andExpect(xpath("//h:input/@value", namespaces).string("Bombur"));
	}

	/**
	 * Tests if the form contains a personId input field with default value.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreatesHiddenInputField() throws Exception {

		this.mockMvc.perform(get("/people/customer/editor").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().mimeType(MediaType.TEXT_HTML))
				.andExpect(xpath("//h:input[@name='personId']/@name", namespaces).string("personId"))
				.andExpect(xpath("//h:input[@name='personId']/@type", namespaces).string("hidden"))
				.andExpect(xpath("//h:input[@name='personId']/@value", namespaces).string("1234"))
				.andExpect(xpath("//h:input[@name='firstname']/@value", namespaces).string("Bilbo"));
	}

}
