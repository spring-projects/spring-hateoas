package de.escalon.hypermedia.spring.xhtml;

import static org.junit.Assert.*;

import de.escalon.hypermedia.spring.sample.test.Event;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

public class XhtmlResourceMessageConverterReadTest {

	XhtmlResourceMessageConverter converter = new XhtmlResourceMessageConverter();

	@Test
	public void testRecursivelyCreateObjectNestedBean() throws Exception {
		LinkedMultiValueMap<String, String> formValues = new LinkedMultiValueMap<String, String>();
		formValues.add("workPerformed.name", "foo");
		formValues.add("location", "Harmonie Heilbronn");
		Event event = (Event) converter.recursivelyCreateObject(Event.class, formValues, "");
		assertEquals("foo", event.getWorkPerformed()
				.getContent().name);
		assertEquals("Harmonie Heilbronn", event.location);
	}
}