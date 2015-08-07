package org.springframework.hateoas.hal;

import org.springframework.hateoas.core.Relation;

@Relation(value = "person", collectionRelation = "people")
public class PersonAnnotatedPojo extends PersonPojo {

	public PersonAnnotatedPojo() {
	}

	public PersonAnnotatedPojo(String firstName, String lastName) {
		super(firstName, lastName);
	}
}
