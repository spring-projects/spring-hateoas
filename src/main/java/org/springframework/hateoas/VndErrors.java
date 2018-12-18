/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A representation model class to be rendered as specified for the media type {@code application/vnd.error+json}.
 * 
 * @see https://github.com/blongden/vnd.error
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
@JsonPropertyOrder({"message", "logref", "total", "_links", "_embedded"})
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode
@ToString
public class VndErrors extends Resources<VndErrors.VndError> {

	public static final String REL_HELP = "help";
	public static final String REL_DESCRIBES = "describes";
	public static final String REL_ABOUT = "about";

	private final List<VndError> errors;

	@Getter
	@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
	private final String message;

	@Getter
	@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
	private final Integer logref;

	/**
	 * Creates a new {@link VndErrors} wrapper for the given {@link VndErrors}.
	 * 
	 * @param errors must not be {@literal null} or empty.
	 */
	@JsonCreator
	public VndErrors(@JsonProperty("_embedded") List<VndError> errors, @JsonProperty("message") String message,
					 @JsonProperty("logref") Integer logref, @JsonProperty("_links") List<Link> links) {

		Assert.notNull(errors, "Errors must not be null!");
		Assert.notEmpty(errors, "Errors must not be empty!");
		
		this.errors = errors;
		this.message = message;
		this.logref = logref;
		if (links != null && !links.isEmpty()) {
			add(links);
		}
	}

	public VndErrors() {

		this.errors = new ArrayList<>();
		this.message = null;
		this.logref = null;
	}

	public VndErrors withMessage(String message) {
		return new VndErrors(this.errors, message, this.logref, this.getLinks());
	}

	public VndErrors withLogref(Integer logref) {
		return new VndErrors(this.errors, this.message, logref, this.getLinks());
	}

	public VndErrors withErrors(List<VndError> errors) {

		Assert.notNull(errors, "errors must not be null!");
		Assert.notEmpty(errors, "errors must not empty!");

		return new VndErrors(errors, this.message, this.logref, this.getLinks());
	}

	public VndErrors withError(VndError error) {

		this.errors.add(error);
		return new VndErrors(this.errors, this.message, this.logref, this.getLinks());
	}

	public VndErrors withLink(Link link) {
		
		add(link);
		return new VndErrors(this.errors, this.message, this.logref, this.getLinks());
	}

	/**
	 * Returns the underlying elements.
	 *
	 * @return the content will never be {@literal null}.
	 */
	@Override
	public Collection<VndError> getContent() {
		return this.errors;
	}

	/**
	 * Virtual attribute to generate JSON field of {@literal total}.
	 */
	public int getTotal() {
		return this.errors.size();
	}

	/**
	 * A single {@link VndError}.
	 * 
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 */
	@JsonPropertyOrder({"message", "path", "logref"})
	@Relation(collectionRelation = "errors")
	@EqualsAndHashCode
	public static class VndError extends ResourceSupport {

		@Getter
		private final String message;

		@Getter
		@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
		private final String path;

		@Getter
		@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
		private final Integer logref;

		/**
		 * Creates a new {@link VndError} with a message and optional a path and a logref.
		 * 
		 * @param message must not be {@literal null} or empty.
		 */
		@JsonCreator
		public VndError(@JsonProperty("message") String message, @JsonProperty("path") String path,
						@JsonProperty("logref") Integer logref, @JsonProperty("_links") List<Link> links) {

			Assert.hasText(message, "Message must not be null or empty!");

			this.message = message;
			this.path = path;
			this.logref = logref;

			this.add(links);
		}

		/**
		 * Convenience constructor
		 */
		public VndError(String message, String path, Integer logref, Link... links) {
			this(message, path, logref, Arrays.asList(links));
		}

		@Override
		public String toString() {
			return "VndError{" +
				"message='" + message + '\'' +
				", path='" + path + '\'' +
				", logref=" + logref +
				", links=" + getLinks() +
				'}';
		}
	}
}
