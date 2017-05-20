/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package org.springframework.hateoas.affordance.springmvc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.springframework.hateoas.affordance.ActionDescriptor;
import org.springframework.hateoas.affordance.ActionInputParameter;
import org.springframework.hateoas.affordance.Affordance;
import org.springframework.hateoas.affordance.Suggest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Dietrich Schulten
 * @author Greg Turnquist
 */
public class AffordanceBuilderTest {

	@Before
	public void setUp() {

		MockHttpServletRequest request = MockMvcRequestBuilders.get("http://example.com/")
			.buildRequest(new MockServletContext());

		RequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);
	}

	@Test
	public void testWithSingleRel() throws Exception {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.rel("next")
			.build();

		assertThat(affordance.toString(), is("Link: <http://example.com/things>; rel=\"next\""));
	}

	@Test
	public void testWithTitle() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.withTitle("my-title")
			.rel("next")
			.build();

		assertThat(affordance.toString(), is("Link: <http://example.com/things>; rel=\"next\"; title=\"my-title\""));
	}

	@Test
	public void testWithTitleStar() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.withTitleStar("UTF-8'de'n%c3%a4chstes%20Kapitel")
			.rel("next")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things>; rel=\"next\"; title*=\"UTF-8'de'n%c3%a4chstes%20Kapitel\""));
	}

	@Test
	public void testWithAnchor() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.withAnchor("http://api.example.com/api")
			.rel("next")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things>; rel=\"next\"; anchor=\"http://api.example.com/api\""));
	}

	@Test
	public void testWithType() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.withType("application/pdf")
			.rel("next")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things>; rel=\"next\"; type=\"application/pdf\""));
	}

	@Test
	public void testWithMedia() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.withMedia("qhd")
			.rel("next")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things>; rel=\"next\"; media=\"qhd\""));
	}

	@Test
	public void testWithHreflang() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.withHreflang("en-us")
			.withHreflang("de")
			.rel("next")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things>; rel=\"next\"; hreflang=\"en-us\"; hreflang=\"de\""));
	}

	@Test
	public void testWithLinkParam() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.withLinkParam("param1", "foo")
			.withLinkParam("param1", "bar")
			.withLinkParam("param2", "baz")
			.rel("next")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things>; rel=\"next\"; param1=\"foo\"; param1=\"bar\"; param2=\"baz\""));
	}

	@Test
	public void testActionDescriptorForRequestParams() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).updateThing(1, (EventStatusType) null))
			.rel("eventStatus")
			.build();

		assertThat(affordance.toString(),
			is("Link-Template: <http://example.com/things/1/eventStatus{?eventStatus}>; rel=\"eventStatus\""));

		assertThat(affordance.getActionDescriptors().size(), is(1));
		ActionDescriptor actionDescriptor = affordance.getActionDescriptors().get(0);

		assertThat(actionDescriptor.getActionName(), is("updateThing"));

		List<EventStatusType> eventStatusTypes = new ArrayList<EventStatusType>();

		List<Suggest<EventStatusType>> values =
			actionDescriptor.getActionInputParameter("eventStatus").getPossibleValues(actionDescriptor);
		for (Suggest<EventStatusType> eventStatusTypeSuggestion : values) {
			eventStatusTypes.add(eventStatusTypeSuggestion.<EventStatusType> getUnwrappedValue());
		}

		assertThat(eventStatusTypes, containsInAnyOrder(EventStatusType.EVENT_CANCELLED, EventStatusType.EVENT_POSTPONED,
			EventStatusType.EVENT_RESCHEDULED, EventStatusType.EVENT_SCHEDULED));
	}

	@Test
	public void testActionDescriptorForRequestBody() {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).updateThing(1, (Thing) null))
			.rel("event")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things/1>; rel=\"event\""));

		assertThat(affordance.getActionDescriptors().size(), is(1));
		ActionDescriptor actionDescriptor = affordance.getActionDescriptors().get(0);

		assertThat(actionDescriptor.getActionName(), is("updateThing"));

		ActionInputParameter thingParameter = actionDescriptor.getRequestBody();

		assertThat(((Class) thingParameter.getGenericParameterType()).getSimpleName(), is("Thing"));
		assertThat(thingParameter.isRequestBody(), is(true));
	}

	@Test
	public void testBuild() throws Exception {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.rel("next")
			.rel("thing")
			.build();

		assertThat(affordance.toString(), is("Link: <http://example.com/things>; rel=\"next thing\""));
	}

	@Test
	public void testBuildNoArgs() throws Exception {

		Affordance affordance = AffordanceBuilder
			.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing()))
			.rel("next")
			.rel("thing")
			.reverseRel("reverted", "for-hal")
			.build();

		assertThat(affordance.toString(),
			is("Link: <http://example.com/things>; rel=\"next thing for-hal\"; rev=\"reverted\""));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRejectsEmptyRel() throws Exception {
		AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing())).rel("").build();
	}

	@Test(expected = IllegalStateException.class)
	public void testRejectsMissingRel() throws Exception {
		AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing())).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRejectsNullRel() throws Exception {
		AffordanceBuilder.linkTo(AffordanceBuilder.methodOn(DummyController.class).createThing(new Thing())).rel(null).build();
	}

	private static class Thing {
	}

	private enum EventStatusType {
		EVENT_POSTPONED, EVENT_RESCHEDULED, EVENT_SCHEDULED, EVENT_CANCELLED
	}

	static class DummyController {

		@RequestMapping("/things")
		public ResponseEntity<?> createThing(@RequestBody Thing thing) {
			return new ResponseEntity<Void>(HttpStatus.CREATED);
		}

		@RequestMapping(value = "/things/{id}/eventStatus", method = RequestMethod.PUT)
		public ResponseEntity<?> updateThing(@PathVariable int id, @RequestParam EventStatusType eventStatus) {
			return new ResponseEntity<Void>(HttpStatus.OK);
		}

		@RequestMapping(value = "/things/{id}", method = RequestMethod.PUT)
		public ResponseEntity<?> updateThing(@PathVariable int id, @RequestBody Thing thing) {
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
	}
}
