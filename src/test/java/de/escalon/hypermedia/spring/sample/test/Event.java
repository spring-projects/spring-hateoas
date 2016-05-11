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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.escalon.hypermedia.action.Select;
import org.springframework.hateoas.Resource;

/**
 * Sample Event.
 * Created by dschulten on 11.09.2014.
 */
public class Event {

	public final int id;
	public final String performer;
	public final String location;

	private EventStatusType eventStatus;
	private final Resource<CreativeWork> workPerformed;
	private String typicalAgeRange;

	public Event(int id, String performer, CreativeWork workPerformed, String location, EventStatusType eventStatus) {
		this.id = id;
		this.performer = performer;
		this.workPerformed = new Resource<CreativeWork>(workPerformed);
		this.location = location;
		this.eventStatus = eventStatus;
	}

	@JsonCreator
	public Event(@JsonProperty("performer") String performer,
				 @JsonProperty("workPerformed") CreativeWork workPerformed,
				 @JsonProperty("location") String location,
				 @JsonProperty("eventStatus") EventStatusType eventStatus,
				 @JsonProperty("typicalAgeRange") @Select({"7-10", "11-"}) String typicalAgeRange) {
		this.id = 0;
		this.performer = performer;
		this.location = location;
		this.workPerformed = new Resource<CreativeWork>(workPerformed);
		this.eventStatus = eventStatus;
		this.typicalAgeRange = typicalAgeRange;
	}

	public void setEventStatus(EventStatusType eventStatus) {
		this.eventStatus = eventStatus;
	}

	public Resource<CreativeWork> getWorkPerformed() {
		return workPerformed;
	}

	public EventStatusType getEventStatus() {
		return eventStatus;
	}

	public String getTypicalAgeRange() {
		return typicalAgeRange;
	}

	public void setTypicalAgeRange(@Select({"7-10", "11-"}) String typicalAgeRange) {
		this.typicalAgeRange = typicalAgeRange;
	}
}
