package de.escalon.hypermedia.spring.xhtml;

import de.escalon.hypermedia.action.Select;
import de.escalon.hypermedia.spring.sample.test.DummyEventController;
import de.escalon.hypermedia.spring.sample.test.ReviewController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration
public class XhtmlResourceMessageConverterTest {

    public static final Logger LOG = LoggerFactory.getLogger(XhtmlResourceMessageConverterTest.class);

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;
    private static Map<String, String> namespaces = new HashMap<String, String>();

    static {
        namespaces.put("h", "http://www.w3.org/1999/xhtml");
    }

    @Configuration
    @EnableWebMvc
    static class WebConfig extends WebMvcConfigurerAdapter {


        @Bean
        public ReviewController reviewController() {
            return new ReviewController();
        }

        @Bean
        public DummyEventController eventController() {
            return new DummyEventController();
        }

        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            super.configureMessageConverters(converters);
            converters.add(new XhtmlResourceMessageConverter());
        }

        @Override
        public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
            final ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
            resolver.setWarnLogCategory(resolver.getClass()
                    .getName());
            exceptionResolvers.add(resolver);
        }

    }


    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();

    }

    @Test
    public void testCreatesHtmlFormForGet() throws Exception {
        MvcResult result = this.mockMvc.perform(get("http://localhost/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(xpath("//h:form[@action='http://localhost/events' and @method='GET' and " +
                        "@name='findEventByName']", namespaces).exists())
                .andExpect(xpath("//h:form[@action='http://localhost/events' and @method='GET' and " +
                                "@name='findEventByName']/h:div/h:input/@name",
                        namespaces).string("eventName"))
                .andReturn();
        LOG.debug(result.getResponse()
                .getContentAsString());
    }

    @Test
    public void testCreatesHtmlFormForPost() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(xpath("//h:form[@name='addEvent']/@action", namespaces).string("http://localhost/events"))
                .andExpect(xpath("//h:form[@name='addEvent']/@method", namespaces).string("POST"))
                .andExpect(xpath("//h:form[@name='addEvent']//h:select[@name='eventStatus']", namespaces).exists())
                .andExpect(xpath("//h:form[@name='addEvent']//h:select[@name='typicalAgeRange']", namespaces)
                        .exists())
                .andExpect(xpath("//h:form[@name='addEvent']//h:input[@name='workPerformed.name']",
                        namespaces)
                        .exists())
                .andReturn();
        LOG.debug(result.getResponse()
                .getContentAsString());

    }

    @Test
    public void testCreatesSimpleLinkForGetAffordanceWithoutRequestParams() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(xpath("//h:a[@href='http://localhost/events/1']", namespaces).exists())
                .andExpect(xpath("//h:a[@href='http://localhost/events/2']", namespaces).exists())
                .andReturn();

        LOG.debug(result.getResponse()
                .getContentAsString());
    }


    @Test
    public void testCreatesHtmlFormForPut() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(xpath("//h:form[@name='updateEventWithRequestBody']/@action", namespaces).string
                        ("http://localhost/events/1"))
                .andExpect(xpath("//h:form[@name='updateEventWithRequestBody']/h:input[@name='_method']/@value",
                        namespaces).string("PUT"))
                .andReturn();

        LOG.debug(result.getResponse()
                .getContentAsString());
    }

    @Test
    public void testCreatesInputFieldWithMinMaxNumber() throws Exception {

        MvcResult result = this.mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(xpath("//h:input[@name='reviewRating.ratingValue']", namespaces).exists())
                .andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@type", namespaces).string("number"))
                .andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@min", namespaces).string("1"))
                .andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@max", namespaces).string("5"))
                .andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@value", namespaces).string("3"))
                .andReturn();
        LOG.debug(result.getResponse()
                .getContentAsString());
    }

    @Test
    public void testCreatesInputFieldWithDefaultText() throws Exception {

        MvcResult result = this.mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(xpath("//h:input[@name='reviewRating.ratingValue']/@value", namespaces).string("3"))
                .andReturn();
        LOG.debug(result.getResponse()
                .getContentAsString());
    }

    /**
     * Tests if the form contains a select field.
     *
     * @throws Exception
     */
    @Test
    public void testCreatesSelectFieldForEnum() throws Exception {

        String statusSelect = "//h:form[@name='updateEventWithRequestBody']//h:select[@name='eventStatus']";
        String option = statusSelect + "/h:option";

        MvcResult result = this.mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(xpath(statusSelect, namespaces).exists())
                .andExpect(xpath(option + "[1]/text()", namespaces).string("EVENT_CANCELLED"))
                .andExpect(xpath(option + "[2]/text()", namespaces).string("EVENT_POSTPONED"))
                .andExpect(xpath(option + "[3]/text()", namespaces).string("EVENT_SCHEDULED"))
                .andExpect(xpath(option + "[4]/text()", namespaces).string("EVENT_RESCHEDULED"))
                .andExpect(xpath("(" + option + ")[@selected]/text()", namespaces).string("EVENT_SCHEDULED"))
                .andReturn();

        LOG.debug(result.getResponse()
                .getContentAsString());
    }

    /**
     * Tests a list of possible values defined with {@link Select#options()} annotation.
     *
     * @throws Exception
     */
    @Test
    public void testCreatesSelectFieldForSelectOptionsBasedPossibleValues() throws Exception {
        MvcResult result = this.mockMvc.perform(get("/events").accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_HTML))
                .andExpect(xpath("//h:form[@name='updateEventWithRequestBody']/h:div/h:select[@name='typicalAgeRange" +
                        "']", namespaces).exists())
                .andExpect(xpath("//h:form[@name='updateEventWithRequestBody']/h:div/h:select[@name='typicalAgeRange" +
                        "']/h:option[1]", namespaces).string("7-10"))
                .andExpect(xpath("//h:form[@name='updateEventWithRequestBody']/h:div/h:select[@name='typicalAgeRange" +
                        "']/h:option[2]", namespaces).string("11-"))
                .andReturn();
        LOG.debug(result.getResponse()
                .getContentAsString());
    }


    /**
     * Tests if the form contains a multiselect field with three preselected items, matching the person having id 123.
     *
     * @throws Exception
     */
//    @Test
//    public void testCreatesMultiSelectFieldForEnumArray() throws Exception {
//
//        this.mockMvc.perform(get("/people/customer/123/editor").accept(MediaType.TEXT_HTML)).andExpect(status()
// .isOk())
//                .andExpect(content().contentType(MediaType.TEXT_HTML))
//                .andExpect(xpath("//h:select[@name='sports' and @multiple]", namespaces).exists())
//                .andExpect(xpath("//h:select[@name='sports']/h:option", namespaces).nodeCount(Sport.values().length))
//                .andExpect(xpath("(//h:select[@name='sports']/h:option)[@selected]", namespaces).nodeCount(3));
//    }
//
//    /**
//     * Tests List<Enum> parameter.
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testCreatesMultiSelectFieldForEnumList() throws Exception {
//
//        this.mockMvc.perform(get("/people/customer/123/editor").accept(MediaType.TEXT_HTML)).andExpect(status()
// .isOk())
//                .andExpect(content().contentType(MediaType.TEXT_HTML))
//                .andExpect(xpath("//h:select[@name='gadgets' and @multiple]", namespaces).exists())
//                .andExpect(xpath("//h:select[@name='gadgets']/h:option", namespaces).nodeCount(Gadget.values()
// .length))
//                .andExpect(xpath("(//h:select[@name='gadgets']/h:option)[@selected]", namespaces).nodeCount(0));
//    }
//    /**
//     * Tests List<String> parameter with a list of possible values.
//     *
//     * @throws Exception
//     */
//    @Test
//    @Ignore
//    public void testCreatesMultiSelectFieldForListOfPossibleValuesFixed() throws Exception {
//
//        this.mockMvc.perform(get("/people/customerByAttribute").accept(MediaType.TEXT_HTML))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.TEXT_HTML))
//                .andExpect(xpath("//h:select[@name='attr' and @multiple]", namespaces).exists())
//                .andExpect(xpath("//h:select[@name='attr']/h:option", namespaces).nodeCount(3))
//                .andExpect(xpath("(//h:select[@name='attr']/h:option)[@selected]", namespaces).string("hungry"));
//
//    }
//
//    /**
//     * Tests List<String> parameter with a list of possible values.
//     *
//     * @throws Exception
//     */
//    @Test
//    @Ignore
//    public void testCreatesMultiSelectFieldForListOfPossibleValuesFromSpringBean() throws Exception {
//
//        this.mockMvc.perform(get("/people/customer/123/details").accept(MediaType.TEXT_HTML))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.TEXT_HTML))
//                .andExpect(xpath("//h:select[@name='detail' and @multiple]", namespaces).exists())
//                .andExpect(xpath("//h:select[@name='detail']/h:option", namespaces).nodeCount(3))
//                .andExpect(xpath("(//h:select[@name='detail']/h:option)[1]", namespaces).string("beard"))
//                .andExpect(xpath("(//h:select[@name='detail']/h:option)[2]", namespaces).string("afterShave"))
//                .andExpect(xpath("(//h:select[@name='detail']/h:option)[3]", namespaces).string("noseHairTrimmer"));
//
//    }
//
//
//    /**
//     * Tests List<String> parameter with a list of numbers.
//     *
//     * @throws Exception
//     */
//    @Test
//    @Ignore("implement code on demand")
//    public void testCreatesOneInputForIntegerListWithInputUpToAny() throws Exception {
//
//        this.mockMvc.perform(get("/people/customer/123/numbers").accept(MediaType.TEXT_HTML))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.TEXT_HTML))
//                .andExpect(xpath("//h:input[@name='number']", namespaces).nodeCount(1));
//        // expect code-on-demand here
//
//    }

}
