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
import de.escalon.hypermedia.action.Input;

/**
 * Created by dschulten on 09.12.2014.
 */
public class Rating {

	public final String bestRating = "5";
	public final String worstRating = "1";
	private Integer ratingValue;

	@JsonCreator
	public Rating(@JsonProperty("ratingValue") @Input(min = 1, max = 5, step = 1) Integer ratingValue) {
		this.ratingValue = ratingValue;
	}

	public void setRatingValue(@Input(min = 1, max = 5, step = 1) Integer ratingValue) {
		this.ratingValue = ratingValue;
	}

	public Integer getRatingValue() {
		return ratingValue;
	}
}
