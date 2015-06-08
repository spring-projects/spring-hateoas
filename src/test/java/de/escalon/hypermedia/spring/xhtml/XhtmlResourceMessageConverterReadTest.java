package de.escalon.hypermedia.spring.xhtml;

import de.escalon.hypermedia.spring.sample.test.Event;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

import java.util.ArrayDeque;

import static org.junit.Assert.*;

/**
 * Created by Dietrich on 07.06.2015.
 */
public class XhtmlResourceMessageConverterReadTest {

    XhtmlResourceMessageConverter converter = new XhtmlResourceMessageConverter();

    @Test
    public void testRecursivelyCreateObjectNestedBean() throws Exception {
        LinkedMultiValueMap<String, String> formValues = new LinkedMultiValueMap<String, String>();
        formValues.add("workPerformed.name", "foo");
        Event event = (Event)converter.recursivelyCreateObject(new ArrayDeque<String>(), Event.class, formValues);
        assertEquals("foo", event.getWorkPerformed().getContent().name);
    }

    @Test
    @Ignore
    public void testRecursivelyCreateObjectCollection() throws Exception {
        throw new UnsupportedOperationException();
    }
}