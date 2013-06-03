package org.springframework.hateoas.sample;

import org.springframework.hateoas.Resource;

public class SamplePersonResource extends Resource<SamplePerson> {

	String firstname;
	String lastname;
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
}
