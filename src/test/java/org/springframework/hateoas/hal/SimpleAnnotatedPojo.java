package org.springframework.hateoas.hal;

@HalRelation(value = "pojo", collectionRelation = "pojo")
public class SimpleAnnotatedPojo extends SimplePojo {

	public SimpleAnnotatedPojo() {
	}

	public SimpleAnnotatedPojo(String text, int number) {
		setText(text);
		setNumber(number);
	}

}
