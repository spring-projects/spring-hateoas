package org.springframework.hateoas.hal;

import org.springframework.hateoas.core.Relation;

@Relation(value = "pojo", collectionRelation = "pojos")
public class SimpleAnnotatedPojo extends SimplePojo {

	public SimpleAnnotatedPojo() {
	}

	public SimpleAnnotatedPojo(String text, int number) {
		setText(text);
		setNumber(number);
	}

}
