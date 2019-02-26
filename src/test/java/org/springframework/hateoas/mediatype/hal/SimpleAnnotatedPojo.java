package org.springframework.hateoas.mediatype.hal;

import org.springframework.hateoas.server.core.Relation;

@Relation(value = "pojo", collectionRelation = "pojos")
public class SimpleAnnotatedPojo extends SimplePojo {

	public SimpleAnnotatedPojo() {
	}

	public SimpleAnnotatedPojo(String text, int number) {
		setText(text);
		setNumber(number);
	}

}
