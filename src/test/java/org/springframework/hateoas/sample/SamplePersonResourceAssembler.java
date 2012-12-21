package org.springframework.hateoas.sample;

import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

public class SamplePersonResourceAssembler extends ResourceAssemblerSupport<SamplePerson, SamplePersonResource> {

	public SamplePersonResourceAssembler() {
		super(SamplePersonController.class, SamplePersonResource.class);
	}

	public SamplePersonResource toResource(SamplePerson person) {
		SamplePersonResource resource = createResource(person);
		resource.setFirstname(person.getFirstname());
		resource.setLastname(person.getLastname());
		return resource;
	}

}
