/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring.sample.test;

import static de.escalon.hypermedia.spring.AffordanceBuilder.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.escalon.hypermedia.affordance.Affordance;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.*;


/**
 * Sample controller demonstrating the use of AffordanceBuilder and hydra-core annotations such as @Expose on
 * request parameters.
 * Created by dschulten on 11.09.2014.
 */
@Controller
@RequestMapping("/events")
public class DummyEventController {

	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<Void> addEvent(@RequestBody Event event) {
		return new ResponseEntity(HttpStatus.CREATED);
	}

	@RequestMapping(method = RequestMethod.GET)
	public
	@ResponseBody
	Resources<Resource<Event>> getResourcesOfResourceOfEvent() {
		List<Resource<Event>> eventResourcesList = new ArrayList<Resource<Event>>();
		// each resource has links
		for (Event event : getEvents()) {
			Resource<Event> eventResource = new Resource<Event>(event);
			eventResource.add(linkTo(methodOn(this.getClass())
					.getEvent(event.id))
					.and(linkTo(methodOn(this.getClass())
							.updateEventWithRequestBody(event.id, event)))
					.and(linkTo(methodOn(this.getClass())
							.deleteEvent(event.id)))
					.withSelfRel());
			event.getWorkPerformed()
					.add(linkTo(methodOn(ReviewController.class)
							.addReview(event.id, new Review(null, new Rating(3))))
							.withRel("review"));
			eventResourcesList.add(eventResource);
		}

		// the resources have templated links to methods
		// specify method by reflection
		final Method getEventMethod = ReflectionUtils.findMethod(this.getClass(), "findEventByName", String.class);
		final Affordance eventByNameAffordance = linkTo(getEventMethod, new Object[0])
				.withRel("hydra:search");
		final Affordance eventWithRegexAffordance = linkTo(
				methodOn(this.getClass()).getEventWithRegexPathVariableMapping(null)).withRel("ex:regex");
		final Affordance postEventAffordance = linkTo(methodOn(this.getClass()).addEvent(null)).withSelfRel();

		return new Resources<Resource<Event>>(eventResourcesList,
				eventByNameAffordance, eventWithRegexAffordance, postEventAffordance);
	}


	@RequestMapping("/list")
	public
	@ResponseBody
	List<Resource<Event>> getListOfResourceOfEvent() {
		List<Resource<Event>> eventResourcesList = new ArrayList<Resource<Event>>();
		for (Event event : getEvents()) {
			Resource<Event> eventResource = new Resource<Event>(event);
			eventResource.add(linkTo(this.getClass()).slash(event.id)
					.withSelfRel());
			eventResource.add(linkTo(methodOn(ReviewController.class)
					.getReviews(event.id, null))
					.withRel("review"));
			// TODO how to express that the same method getReviews
			// is also an IriTemplate if the variable is optional
			eventResourcesList.add(eventResource);
		}
		return eventResourcesList;
	}

	@RequestMapping(value = "/{eventId}", method = RequestMethod.GET)
	public
	@ResponseBody
	Resource<Event> getEvent(@PathVariable Integer eventId) {
		Resource<Event> resource = new Resource<Event>(getEvents().get(eventId));
		resource.add(linkTo(ReviewController.class).withRel("review"));
		return resource;
	}

	@RequestMapping(value = "/regex/{eventId:.+}", method = RequestMethod.GET)
	public
	@ResponseBody
	Resource<Event> getEventWithRegexPathVariableMapping(@PathVariable Integer eventId) {
		Resource<Event> resource = new Resource<Event>(getEvents().get(eventId));
		resource.add(linkTo(ReviewController.class).withRel("review"));
		return resource;
	}

	@RequestMapping(method = RequestMethod.GET, params = {"eventName"})
	public
	@ResponseBody
	Resource<Event> findEventByName(@RequestParam String eventName) {
		Resource<Event> ret = null;
		for (Event event : getEvents()) {
			if (event.getWorkPerformed()
					.getContent().name.startsWith(eventName)) {
				Resource<Event> resource = new Resource<Event>(event);
				resource.add(linkTo(ReviewController.class).withRel("review"));
				ret = resource;
				break;
			}
		}
		return ret;
	}

	@RequestMapping("/resourcesupport/{eventId}")
	public
	@ResponseBody
	EventResource getResourceSupportEvent(@PathVariable int eventId) {
		EventResource resource = getEventResources().get(eventId);
		resource.add(linkTo(ReviewController.class).withRel("review"));
		return resource;
	}

	@RequestMapping(value = "/{eventId}", method = RequestMethod.PUT)
	public ResponseEntity<Void> updateEventWithRequestBody(@PathVariable int eventId, @RequestBody Event event) {
		getEvents().set(eventId - 1, event);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	@RequestMapping(value = "/{eventId}", method = RequestMethod.DELETE)
	public ResponseEntity<Void> deleteEvent(@PathVariable int eventId) {
		getEvents().remove(eventId - 1);
		return new ResponseEntity<Void>(HttpStatus.OK);
	}


	protected List<Event> getEvents() {
		return Arrays.asList(new Event(1, "Walk off the Earth", new CreativeWork("Gang of Rhythm Tour"), "Wiesbaden", EventStatusType.EVENT_SCHEDULED),
				new Event(2, "Cornelia Bielefeldt", new CreativeWork("Mein letzter Film"), "Heilbronn", EventStatusType.EVENT_SCHEDULED));
	}

	protected List<? extends EventResource> getEventResources() {
		return Arrays.asList(new EventResource(1, "Walk off the Earth", "Gang of Rhythm Tour", "Wiesbaden"),
				new EventResource(2, "Cornelia Bielefeldt", "Mein letzter Film", "Heilbronn"));
	}
}
