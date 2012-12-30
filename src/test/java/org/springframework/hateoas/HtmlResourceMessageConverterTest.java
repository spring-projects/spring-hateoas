package org.springframework.hateoas;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

/**
 * Tests html form message creation for /customer resource on {@link SamplePersonController}.
 * 
 * @author Dietrich Schulten
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
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
		this.mockMvc = webAppContextSetup(this.wac).build();

	}

	@Test
	public void testCreatesHtmlForm() throws Exception {
		this.mockMvc.perform(get("/people/customer").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_HTML))
				.andExpect(xpath("h:html/h:body/h:form/@action", namespaces).string("/people/customer"))
				.andExpect(xpath("//h:form/@name", namespaces).string("searchPerson"))
				.andExpect(xpath("//h:form/@method", namespaces).string("GET"));
	}

	@Test
	public void testCreatesInputFieldWithDefaultValue() throws Exception {

		this.mockMvc.perform(get("/people/customer").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))
				.andExpect(xpath("//h:input/@name", namespaces).string("personId"))
				.andExpect(xpath("//h:input/@type", namespaces).string("text"))
				.andExpect(xpath("//h:input/@value", namespaces).string("1234"));
	}

	/**
	 * Tests if the form contains a personId input field with default value.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreatesHiddenInputField() throws Exception {

		this.mockMvc.perform(get("/people/customer/editor").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))
				.andExpect(xpath("//h:input[@name='personId']/@name", namespaces).string("personId"))
				.andExpect(xpath("//h:input[@name='personId']/@type", namespaces).string("hidden"))
				.andExpect(xpath("//h:input[@name='personId']/@value", namespaces).string("1234"))
				.andExpect(xpath("//h:input[@name='firstname']/@value", namespaces).string("Bilbo"));
	}

}
