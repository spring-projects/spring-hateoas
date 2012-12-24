package org.springframework.hateoas.sample;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.action.ActionDescriptor;
import org.springframework.hateoas.action.Hidden;
import org.springframework.hateoas.mvc.ControllerFormBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/people")
public class SamplePersonController {

	private static SamplePerson person = new SamplePerson();
	static {
		person.setId(1234L);
		person.setFirstname("Bilbo");
		person.setLastname("Baggins");
	}

	@RequestMapping(value = "/customer")
	public HttpEntity<ActionDescriptor> searchPersonForm() {
		long defaultPersonId = 1234L;
		ActionDescriptor form = ControllerFormBuilder.createFormFor(
				methodOn(SamplePersonController.class).showPerson(defaultPersonId), "searchPerson");
		return new HttpEntity<ActionDescriptor>(form);
	}

	@RequestMapping(value = "/customer", method = RequestMethod.GET, params = { "personId" })
	public HttpEntity<SamplePersonResource> showPerson(@RequestParam Long personId) {

		SamplePersonResourceAssembler assembler = new SamplePersonResourceAssembler();
		SamplePersonResource resource = assembler.toResource(person);

		return new HttpEntity<SamplePersonResource>(resource);
	}

	@RequestMapping(value = "/customer/editor")
	public HttpEntity<ActionDescriptor> changePersonForm() {

		ActionDescriptor descriptor = ControllerFormBuilder.createFormFor(methodOn(SamplePersonController.class)
				.changePerson(person.getId(), person.getFirstname(), person.getLastname()), "changePerson");

		return new HttpEntity<ActionDescriptor>(descriptor);
	}

	@RequestMapping(value = "/customer", method = RequestMethod.PUT, params = { "personId", "firstname", "lastname" })
	public HttpEntity<SamplePersonResource> changePerson(@RequestParam @Hidden Long personId,
			@RequestParam String firstname, @RequestParam String lastname) {

		person.setFirstname(firstname);
		person.setLastname(lastname);
		SamplePersonResourceAssembler assembler = new SamplePersonResourceAssembler();
		SamplePersonResource resource = assembler.toResource(person);

		return new HttpEntity<SamplePersonResource>(resource);
	}

}
