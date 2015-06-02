/*
 * Copyright (c) 2015. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring.uber;

import org.springframework.web.bind.annotation.RequestMethod;

public enum UberAction {

	/** POST */
	APPEND(RequestMethod.POST),
	/** PATCH */
	PARTIAL(RequestMethod.PATCH),
	/** GET */
	READ(RequestMethod.GET),
	/** DELETE */
	REMOVE(RequestMethod.DELETE),
	/** PUT */
	REPLACE(RequestMethod.PUT);

	public final RequestMethod httpMethod;

	private UberAction(RequestMethod method) {
		this.httpMethod = method;
	}

	/**
	 * Maps given request method to uber action, GET will be mapped as null since it is the default.
	 * @param method to map
	 * @return action, or null for GET
	 */
	public static UberAction forRequestMethod(RequestMethod method) {
		if (RequestMethod.GET == method) {
			return null;
		}
		for (UberAction action : UberAction.values()) {
			if (action.httpMethod == method) {
				return action;
			}
		}
		throw new IllegalArgumentException("unsupported method: " + method);
	}
}
