package org.springframework.hateoas.sample;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.springframework.hateoas.action.ActionDescriptor;
import org.springframework.hateoas.mvc.ControllerFormBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/people")
public class SamplePersonController {
	@RequestMapping(value = "/customer", method = RequestMethod.GET)
	public HttpEntity<ActionDescriptor> searchPersonForm() {
		long defaultPersonId = 1234L;
		ActionDescriptor form = ControllerFormBuilder.createForm("searchPerson", methodOn(SamplePersonController.class)
				.showPerson(defaultPersonId));
		return new HttpEntity<ActionDescriptor>(form);
	}

	@RequestMapping(value = "/customer", method = RequestMethod.GET, params = { "personId" })
	public HttpEntity<SamplePersonResource> showPerson(@RequestParam(value = "personId") Long personId) {

		SamplePerson person = new SamplePerson();
		person.setId(1L);
		person.setFirstname("Bilbo");
		person.setLastname("Baggins");
		SamplePersonResourceAssembler assembler = new SamplePersonResourceAssembler();
		SamplePersonResource resource = assembler.toResource(person);

		return new HttpEntity<SamplePersonResource>(resource);
	}
}
