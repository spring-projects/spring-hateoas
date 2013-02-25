package org.springframework.hateoas.hal;


@HateoasRelation("pojo")
public class SimpleAnnotatedPojo extends SimplePojo {

	public SimpleAnnotatedPojo() {
	}

	public SimpleAnnotatedPojo(String text, int number) {
		setText(text);
		setNumber(number);
	}

}
