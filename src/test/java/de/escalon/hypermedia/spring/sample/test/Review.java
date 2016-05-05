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
 * Sample Review.
 * Created by dschulten on 16.09.2014.
 */
public class Review {

	private String reviewBody;
	private Rating reviewRating;

	@JsonCreator
	public Review(@JsonProperty("reviewBody") String reviewBody, @JsonProperty("reviewRating") Rating reviewRating) {
		this.reviewBody = reviewBody;
		this.reviewRating = reviewRating;
	}

	@SuppressWarnings("unused")
	public String getReviewBody() {
		return reviewBody;
	}

	public void setReviewBody(@Input(pattern = ".{10,}") String reviewBody) {
		this.reviewBody = reviewBody;
	}

	public Rating getReviewRating() {
		return reviewRating;
	}

	public void setReviewRating(Rating reviewRating) {
		this.reviewRating = reviewRating;
	}
}
