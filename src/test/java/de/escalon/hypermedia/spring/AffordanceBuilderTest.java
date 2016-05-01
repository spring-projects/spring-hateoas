/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring;

import de.escalon.hypermedia.affordance.ActionDescriptor;
import de.escalon.hypermedia.affordance.Affordance;
import de.escalon.hypermedia.affordance.ActionInputParameter;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AffordanceBuilderTest {

    @Before
    public void setUp() {
        MockHttpServletRequest request = MockMvcRequestBuilders.get("http://example.com/")
                .buildRequest(new MockServletContext());
        final RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    public static class Thing {

    }

    enum EventStatusType {
        EVENT_POSTPONED, EVENT_RESCHEDULED, EVENT_SCHEDULED, EVENT_CANCELLED

    }

    public static class DummyController {

        @RequestMapping("/things")
        public ResponseEntity createThing(@RequestBody Thing thing) {
            return new ResponseEntity(HttpStatus.CREATED);
        }

        @RequestMapping(value = "/things/{id}/eventStatus", method = RequestMethod.PUT)
        public ResponseEntity updateThing(@PathVariable int id, @RequestParam EventStatusType eventStatus) {
            return new ResponseEntity(HttpStatus.OK);
        }

        @RequestMapping(value = "/things/{id}", method = RequestMethod.PUT)
        public ResponseEntity updateThing(@PathVariable int id, @RequestBody Thing thing) {
            return new ResponseEntity(HttpStatus.OK);
        }

    }

    @Test
    public void testWithSingleRel() throws Exception {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"", affordance.toString());
    }

    @Test
    public void testWithTitle() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .withTitle("my-title")
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"; title=\"my-title\"",
                affordance.toString());
    }

    @Test
    public void testWithTitleStar() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .withTitleStar("UTF-8'de'n%c3%a4chstes%20Kapitel")
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"; title*=\"UTF-8'de'n%c3%a4chstes%20Kapitel\"",
                affordance.toString());
    }

    @Test
    public void testWithAnchor() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .withAnchor("http://api.example.com/api")
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"; anchor=\"http://api.example.com/api\"",
                affordance.toString());
    }

    @Test
    public void testWithType() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .withType("application/pdf")
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"; type=\"application/pdf\"",
                affordance.toString());
    }

    @Test
    public void testWithMedia() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .withMedia("qhd")
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"; media=\"qhd\"",
                affordance.toString());
    }

    @Test
    public void testWithHreflang() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .withHreflang("en-us")
                .withHreflang("de")
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"; hreflang=\"en-us\"; hreflang=\"de\"",
                affordance.toString());
    }

    @Test
    public void testWithLinkParam() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .withLinkParam("param1", "foo")
                .withLinkParam("param1", "bar")
                .withLinkParam("param2", "baz")
                .rel("next")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next\"; param1=\"foo\"; param1=\"bar\"; param2=\"baz\"",
                affordance.toString());
    }

    @Test
    public void testActionDescriptorForRequestParams() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .updateThing(1, (EventStatusType) null))
                .rel("eventStatus")
                .build();
        Assert.assertEquals("Link-Template: <http://example.com/things/1/eventStatus{?eventStatus}>; rel=\"eventStatus\"",
                affordance.toString());
        final ActionDescriptor actionDescriptor = affordance.getActionDescriptors()
                .get(0);
        Assert.assertThat((EventStatusType[]) actionDescriptor.getActionInputParameter("eventStatus")
                        .getPossibleValues(actionDescriptor),
                Matchers.arrayContainingInAnyOrder(
                        EventStatusType.EVENT_CANCELLED,
                        EventStatusType.EVENT_POSTPONED,
                        EventStatusType.EVENT_RESCHEDULED,
                        EventStatusType.EVENT_SCHEDULED));
        Assert.assertEquals("updateThing", actionDescriptor.getActionName());
    }

    @Test
    public void testActionDescriptorForRequestBody() {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .updateThing(1, (Thing) null))
                .rel("event")
                .build();
        Assert.assertEquals("Link: <http://example.com/things/1>; rel=\"event\"",
                affordance.toString());
        final ActionDescriptor actionDescriptor = affordance.getActionDescriptors()
                .get(0);
        final ActionInputParameter thingParameter = actionDescriptor.getRequestBody();
        Assert.assertEquals("Thing", ((Class) thingParameter.getGenericParameterType()).getSimpleName());
        Assert.assertThat(thingParameter.isRequestBody(), Matchers.is(true));
        Assert.assertEquals("updateThing", actionDescriptor.getActionName());
    }


    @Test
    public void testBuild() throws Exception {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .rel("next").rel("thing")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next thing\"", affordance.toString());
    }

    @Test
    public void testBuildNoArgs() throws Exception {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .rel("next").rel("thing")
                .reverseRel("reverted", "for-hal")
                .build();
        Assert.assertEquals("Link: <http://example.com/things>; rel=\"next thing for-hal\"; rev=\"reverted\"",
                affordance.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsEmptyRel() throws Exception {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .rel("").build();
    }


    @Test(expected = IllegalStateException.class)
    public void testRejectsMissingRel() throws Exception {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .build();
    }


    @Test(expected = IllegalArgumentException.class)
    public void testRejectsNullRel() throws Exception {
        final Affordance affordance = AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class)
                .createThing(new Thing()))
                .rel(null).build();
    }

}