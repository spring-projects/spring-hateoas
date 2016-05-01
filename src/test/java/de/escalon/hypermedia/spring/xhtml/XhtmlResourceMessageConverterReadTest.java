package de.escalon.hypermedia.spring.xhtml;

import de.escalon.hypermedia.spring.sample.test.Event;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.wiring.BeanWiringInfo;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayDeque;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

public class XhtmlResourceMessageConverterReadTest {

    XhtmlResourceMessageConverter converter = new XhtmlResourceMessageConverter();

    @Test
    public void testRecursivelyCreateObjectNestedBean() throws Exception {
        LinkedMultiValueMap<String, String> formValues = new LinkedMultiValueMap<String, String>();
        formValues.add("workPerformed.name", "foo");
        formValues.add("location", "Harmonie Heilbronn");
        Event event = (Event) converter.recursivelyCreateObject(Event.class, formValues);
        assertEquals("foo", event.getWorkPerformed()
                .getContent().name);
        assertEquals("Harmonie Heilbronn", event.location);
    }

}