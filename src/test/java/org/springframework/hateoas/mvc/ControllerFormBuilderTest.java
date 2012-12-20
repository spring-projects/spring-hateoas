package org.springframework.hateoas.mvc;

import static org.junit.Assert.assertEquals;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.FormDescriptor;
import org.springframework.hateoas.mvc.ControllerLinkBuilderUnitTest.PersonControllerForForm;

public class ControllerFormBuilderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void createsLinkToFormWithPathVariable() throws Exception {
		FormDescriptor formDescriptor = ControllerFormBuilder.createForm("searchPerson",
				methodOn(PersonControllerForForm.class).showPerson("mike", null));
		// TODO the linkTemplate field should not contain the expanded template
		assertEquals("/person/mike", formDescriptor.getLinkTemplate());
	}

}
