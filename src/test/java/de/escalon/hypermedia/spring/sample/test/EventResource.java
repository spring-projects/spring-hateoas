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

import org.springframework.hateoas.ResourceSupport;

/**
 * Sample Event derived from ResourceSupport.
 * Created by dschulten on 13.09.2014.
 */
public class EventResource extends ResourceSupport {

	public int id;
	public String performer;
	public String name;
	public String location;

	public EventResource(int id, String performer, String name, String location) {
		this.id = id;
		this.performer = performer;
		this.name = name;
		this.location = location;
	}
}
