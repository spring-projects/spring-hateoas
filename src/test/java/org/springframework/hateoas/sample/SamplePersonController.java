package org.springframework.hateoas.sample;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.action.ActionDescriptor;
import org.springframework.hateoas.action.Input;
import org.springframework.hateoas.action.Type;
import org.springframework.hateoas.mvc.ControllerActionBuilder;
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
		ActionDescriptor form = ControllerActionBuilder.createActionFor(
				methodOn(SamplePersonController.class).showPerson(defaultPersonId), "searchPerson");
		return new HttpEntity<ActionDescriptor>(form);
	}

	@RequestMapping(value = "/customer", method = RequestMethod.GET, params = { "personId" })
	public HttpEntity<SamplePersonResource> showPerson(@RequestParam Long personId) {

		SamplePersonResourceAssembler assembler = new SamplePersonResourceAssembler();
		SamplePersonResource resource = assembler.toResource(person);

		return new HttpEntity<SamplePersonResource>(resource);
	}

	@RequestMapping(value = "/customerByName")
	public HttpEntity<ActionDescriptor> searchPersonByNameForm() {
		String defaultName = "Bombur";
		ActionDescriptor form = ControllerActionBuilder.createActionFor(
				methodOn(SamplePersonController.class).showPerson(defaultName), "searchPerson");
		return new HttpEntity<ActionDescriptor>(form);
	}

	@RequestMapping(value = "/customerByName", method = RequestMethod.GET, params = { "name" })
	public HttpEntity<SamplePersonResource> showPerson(@RequestParam String name) {

		SamplePersonResourceAssembler assembler = new SamplePersonResourceAssembler();
		SamplePersonResource resource = assembler.toResource(person);

		return new HttpEntity<SamplePersonResource>(resource);
	}

	@RequestMapping(value = "/customer/editor")
	public HttpEntity<ActionDescriptor> editPersonForm() {
		// PUT is allowed for forms as of HTML 5, programmatic clients and new browsers can handle it
		ActionDescriptor descriptor = ControllerActionBuilder.createActionFor(methodOn(SamplePersonController.class)
				.editPerson(person.getId(), person.getFirstname(), person.getLastname()), "changePerson");

		return new HttpEntity<ActionDescriptor>(descriptor);
	}

	@RequestMapping(value = "/customer", method = RequestMethod.PUT, params = { "personId", "firstname", "lastname" })
	public HttpEntity<SamplePersonResource> editPerson(@RequestParam @Input(Type.HIDDEN) Long personId,
			@RequestParam String firstname, @RequestParam String lastname) {

		person.setFirstname(firstname);
		person.setLastname(lastname);
		SamplePersonResourceAssembler assembler = new SamplePersonResourceAssembler();
		SamplePersonResource resource = assembler.toResource(person);

		return new HttpEntity<SamplePersonResource>(resource);
	}

}
