package de.escalon.hypermedia.spring.xhtml;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.affordance.Affordance;
import de.escalon.hypermedia.spring.AffordanceBuilder;
import de.escalon.hypermedia.spring.sample.test.*;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Created by Dietrich on 06.06.2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class XhtmlWriterTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    Writer writer = new StringWriter();
    XhtmlWriter xhtml = new XhtmlWriter(writer);


    @Configuration
    @EnableWebMvc
    static class WebConfig extends WebMvcConfigurerAdapter {

    }

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();

    }

    @Test
    public void testPutBodyComplete() throws Exception {

        @RequestMapping("/")
        class DummyController {
            @RequestMapping(method = RequestMethod.PUT)
            public ResponseEntity<Void> putMultiplePossibleValues(@RequestBody Event event) {
                return null;
            }
        }


        Link affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .putMultiplePossibleValues(new Event(0, null, new CreativeWork(null), null, EventStatusType
                        .EVENT_SCHEDULED)))
                .withSelfRel();

        xhtml.writeLinks(Arrays.asList(affordance));

        String xml = writer.toString();
        System.out.println(xml);

        // renders writable event bean properties only
        XMLAssert.assertXpathEvaluatesTo("EVENT_CANCELLED", "//select[@name='eventStatus']/option[1]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_POSTPONED", "//select[@name='eventStatus']/option[2]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[3]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_RESCHEDULED", "//select[@name='eventStatus']/option[4]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[@selected]", xml);
        XMLAssert.assertXpathEvaluatesTo("7-10", "//select[@name='typicalAgeRange']/option[1]", xml);
        XMLAssert.assertXpathEvaluatesTo("11-", "//select[@name='typicalAgeRange']/option[2]", xml);

        XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
        XMLAssert.assertXpathNotExists("//select[@multiple]", xml);
    }

    @Test
    public void testPostBodyComplete() throws Exception {

        @RequestMapping("/")
        class DummyController {
            @RequestMapping(method = RequestMethod.POST)
            public ResponseEntity<Void> postMultiplePossibleValues(@RequestBody Event event) {
                return null;
            }
        }


        Link affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .postMultiplePossibleValues(new Event(0, null, new CreativeWork(null), null, EventStatusType
                        .EVENT_SCHEDULED)))
                .withSelfRel();

        xhtml.writeLinks(Arrays.asList(affordance));

        String xml = writer.toString();
        System.out.println(xml);

        // renders event bean constructor arguments
        XMLAssert.assertXpathEvaluatesTo("EVENT_CANCELLED", "//select[@name='eventStatus']/option[1]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_POSTPONED", "//select[@name='eventStatus']/option[2]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[3]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_RESCHEDULED", "//select[@name='eventStatus']/option[4]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[@selected]", xml);
        XMLAssert.assertXpathEvaluatesTo("7-10", "//select[@name='typicalAgeRange']/option[1]", xml);
        XMLAssert.assertXpathEvaluatesTo("11-", "//select[@name='typicalAgeRange']/option[2]", xml);
        XMLAssert.assertXpathExists("//input[@name='performer']", xml);
        XMLAssert.assertXpathExists("//input[@name='location']", xml);
        XMLAssert.assertXpathExists("//input[@name='workPerformed.name']", xml);
        XMLAssert.assertXpathNotExists("//select[@multiple]", xml);
    }

    @Test
    public void testPostBodyReadOnlyEventStatus() throws Exception {

        @RequestMapping("/")
        class DummyController {
            @RequestMapping(method = RequestMethod.POST)
            public ResponseEntity<Void> postEventStatusOnly(
                    @RequestBody @Input(readOnly = "eventStatus") Event event) {
                return null;
            }
        }

        Link affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .postEventStatusOnly(new Event(0, null, new CreativeWork(null), null, EventStatusType
                        .EVENT_SCHEDULED)))
                .withSelfRel();

        xhtml.writeLinks(Arrays.asList(affordance));

        String xml = writer.toString();
        System.out.println(xml);

        XMLAssert.assertXpathEvaluatesTo("EVENT_CANCELLED", "//select[@name='eventStatus']/option[1]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_POSTPONED", "//select[@name='eventStatus']/option[2]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[3]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_RESCHEDULED", "//select[@name='eventStatus']/option[4]", xml);
        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//select[@name='eventStatus']/option[@selected]", xml);

        XMLAssert.assertXpathNotExists("//select[@name='typicalAgeRange']", xml);
        XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
        XMLAssert.assertXpathNotExists("//input[@name='location']", xml);
        XMLAssert.assertXpathNotExists("//input[@name='workPerformed.name']", xml);

    }

    @Test
    public void testPostBodyHiddenEventStatus() throws Exception {

        @RequestMapping("/")
        class DummyController {
            @RequestMapping(method = RequestMethod.POST)
            public ResponseEntity<Void> postEventStatusOnly(
                    @RequestBody @Input(hidden = "eventStatus") Event event) {
                return null;
            }
        }


        Link affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .postEventStatusOnly(new Event(0, null, new CreativeWork(null), null, EventStatusType
                        .EVENT_SCHEDULED)))
                .withSelfRel();

        xhtml.writeLinks(Arrays.asList(affordance));

        String xml = writer.toString();
        System.out.println(xml);

        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//option[@selected='selected']/text()", xml);

        XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
        XMLAssert.assertXpathNotExists("//select[@name='typicalAgeRange']", xml);
        XMLAssert.assertXpathNotExists("//input[@name='location']", xml);
        XMLAssert.assertXpathNotExists("//input[@name='workPerformed.name']", xml);

    }

    @Test
    public void testPostBodyHiddenEventStatusWithWorkPerformedName() throws Exception {

        @RequestMapping("/")
        class DummyController {
            @RequestMapping(method = RequestMethod.POST)
            public ResponseEntity<Void> postEventStatusOnly(
                    @RequestBody @Input(hidden = "eventStatus", include = "name") Event event) {
                return null;
            }
        }


        Link affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .postEventStatusOnly(new Event(0, null, new CreativeWork(null), null, EventStatusType
                        .EVENT_SCHEDULED)))
                .withSelfRel();

        xhtml.writeLinks(Arrays.asList(affordance));

        String xml = writer.toString();
        System.out.println(xml);

        XMLAssert.assertXpathEvaluatesTo("EVENT_SCHEDULED", "//option[@selected='selected']/text()", xml);
        XMLAssert.assertXpathExists("//input[@name='workPerformed.name']", xml);

        XMLAssert.assertXpathNotExists("//input[@name='performer']", xml);
        XMLAssert.assertXpathNotExists("//select[@name='typicalAgeRange']", xml);
        XMLAssert.assertXpathNotExists("//input[@name='location']", xml);

    }
}