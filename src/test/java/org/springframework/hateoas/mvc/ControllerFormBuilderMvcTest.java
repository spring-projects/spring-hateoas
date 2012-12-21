package org.springframework.hateoas.mvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class ControllerFormBuilderMvcTest {
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

	/**
	 * Tests if requests to the /people/customer resource without params get a form as response
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreatesForm() throws Exception {
		this.mockMvc.perform(get("/people/customer").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.TEXT_HTML))
				.andExpect(xpath("h:html/h:body/h:form/@action", namespaces).string("/people/customer"))
				.andExpect(xpath("h:html/h:body/h:form/@name", namespaces).string("searchPerson"))
				.andExpect(xpath("h:html/h:body/h:form/@method", namespaces).string("GET"));
	}

	/**
	 * Tests if the form contains a personId input field with default value.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreatesInputFieldWithDefaultValue() throws Exception {

		this.mockMvc.perform(get("/people/customer").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType(MediaType.TEXT_HTML))
				.andExpect(xpath("h:html/h:body/h:form/h:label/h:input/@name", namespaces).string("personId"))
				.andExpect(xpath("h:html/h:body/h:form/h:label/h:input/@type", namespaces).string("text"))
				.andExpect(xpath("h:html/h:body/h:form/h:label/h:input/@value", namespaces).string("1234"));
	}

}
