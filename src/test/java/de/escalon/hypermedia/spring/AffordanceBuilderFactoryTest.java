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

import de.escalon.hypermedia.affordance.Affordance;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AffordanceBuilderFactoryTest {

    AffordanceBuilderFactory factory = new AffordanceBuilderFactory();

    private MockHttpServletRequest request;

    /**
     * Sample controller.
     * Created by dschulten on 11.09.2014.
     */
    @Controller
    @RequestMapping("/events")
    class EventControllerSample {
        @RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
        public
        @ResponseBody
        Resource<Object> getEvent(@PathVariable String eventId) {
            return null;
        }

    }

    @Before
    public void setUp() {
        request = MockMvcRequestBuilders.get("http://example.com/")
                .buildRequest(new MockServletContext());
        final RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Test
    public void testLinkToMethod() throws Exception {
        final Method getEventMethod = ReflectionUtils.findMethod(EventControllerSample.class, "getEvent", String.class);
        final Affordance affordance = factory.linkTo(getEventMethod, new Object[0])
                .build("foo");
        assertEquals("http://example.com/events/{eventId}", affordance.getHref());
    }

    @Test
    public void testLinkToMethodInvocation() throws Exception {
        final Method getEventMethod = ReflectionUtils.findMethod(EventControllerSample.class, "getEvent", String.class);
        final Affordance affordance = factory.linkTo(AffordanceBuilder.methodOn(EventControllerSample.class)
                .getEvent((String) null))
                .build("foo");
        assertEquals("http://example.com/events/{eventId}", affordance.getHref());
    }

    @Test
    public void testLinkToControllerClass() throws Exception {
        final Affordance affordance = factory.linkTo(EventControllerSample.class, new Object[0])
                .build("foo");
        assertEquals("http://example.com/events", affordance.getHref());
    }

    @Test
    public void testLinkToMethodNoArgsBuild() throws Exception {
        final Method getEventMethod = ReflectionUtils.findMethod(EventControllerSample.class, "getEvent", String.class);
        final Affordance affordance = factory.linkTo(getEventMethod, new Object[0])
                .rel("foo")
                .build();
        assertEquals("http://example.com/events/{eventId}", affordance.getHref());
        assertEquals("foo", affordance.getRel());
    }

    @Test
    public void testLinkToMethodInvocationNoArgsBuild() throws Exception {
        final Method getEventMethod = ReflectionUtils.findMethod(EventControllerSample.class, "getEvent", String.class);
        final Affordance affordance = factory.linkTo(AffordanceBuilder.methodOn(EventControllerSample.class)
                .getEvent((String) null))
                .rel("foo")
                .build();
        assertEquals("http://example.com/events/{eventId}", affordance.getHref());
        assertEquals("foo", affordance.getRel());
    }

    @Test
    public void testLinkToControllerClassNoArgsBuild() throws Exception {
        final Affordance affordance = factory.linkTo(EventControllerSample.class, new Object[0])
                .rel("foo")
                .build();
        assertEquals("http://example.com/events", affordance.getHref());
        assertEquals("foo", affordance.getRel());
    }

    @Test
    public void testLinkToMethodInvocationReverseRel() throws Exception {
        final Method getEventMethod = ReflectionUtils.findMethod(EventControllerSample.class, "findEventByName", String.class);
        final Affordance affordance = factory.linkTo(AffordanceBuilder.methodOn(EventControllerSample.class)
                .getEvent((String) null))
                .reverseRel("schema:parent", "ex:children")
                .build();
        assertEquals("http://example.com/events/{eventId}", affordance.getHref());
        assertEquals("schema:parent", affordance.getRev());
        assertEquals("ex:children", affordance.getRel());
    }


}