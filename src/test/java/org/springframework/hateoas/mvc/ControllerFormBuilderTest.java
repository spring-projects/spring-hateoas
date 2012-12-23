package org.springframework.hateoas.mvc;

import static org.junit.Assert.assertEquals;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.action.ActionDescriptor;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

public class ControllerFormBuilderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void createsLinkToFormWithMethodLevelAndTypeLevelVariables() throws Exception {
		ActionDescriptor formDescriptor = ControllerFormBuilder.createForm("searchPerson",
				methodOn(PersonControllerForForm.class, "region1").showPerson("mike", null));
		assertEquals("/region/region1/person/mike", formDescriptor.getActionLink());
	}

	@RequestMapping("/region/{regionId}")
	static class PersonControllerForForm {
		@RequestMapping(value = "/person/{personName}", method = RequestMethod.POST)
		public HttpEntity<? extends Object> showPerson(@PathVariable("personName") String bar,
				@RequestParam(value = "personId") Long personId) {
			return null;
		}
	}

}
