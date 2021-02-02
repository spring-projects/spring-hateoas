/*
 * Copyright 2013-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.mediatype.vnderrors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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
 * @deprecated since 1.1, use {@link org.springframework.hateoas.mediatype.problem.Problem} to form vendor neutral error
 *             messages.
 */
@JsonPropertyOrder({ "message", "logref", "total", "_links", "_embedded" })
@JsonIgnoreProperties(ignoreUnknown = true)
@Deprecated
public class VndErrors extends CollectionModel<VndErrors.VndError> {

	/**
	 * @deprecated Use {@link org.springframework.hateoas.IanaLinkRelations#HELP}
	 */
	@Deprecated public static final String REL_HELP = "help";

	/**
	 * @deprecated Use {@link org.springframework.hateoas.IanaLinkRelations#DESCRIBES}
	 */
	@Deprecated public static final String REL_DESCRIBES = "describes";

	/**
	 * @deprecated Use {@link org.springframework.hateoas.IanaLinkRelations#ABOUT}
	 */
	@Deprecated public static final String REL_ABOUT = "about";

	private final List<VndError> errors;

	@JsonInclude(value = JsonInclude.Include.NON_EMPTY) //
	private final String message;

	@JsonInclude(value = JsonInclude.Include.NON_EMPTY) //
	private final Object logref;

	public VndErrors() {

		this.errors = new ArrayList<>();
		this.message = null;
		this.logref = null;
	}

	/**
	 * Creates a new {@link VndErrors} instance containing a single {@link VndError} with the given logref, message and
	 * optional {@link Link}s.
	 */
	public VndErrors(Object logref, String message, Link... links) {
		this(new VndError(message, null, logref, links));
	}

	/**
	 * Creates a new {@link VndErrors} wrapper for at least one {@link VndError}.
	 *
	 * @param errors must not be {@literal null}.
	 */
	public VndErrors(VndError error, VndError... errors) {

		Assert.notNull(error, "Error must not be null");

		this.errors = new ArrayList<>();
		this.errors.add(error);
		Collections.addAll(this.errors, errors);
		this.message = null;
		this.logref = null;
	}

	/**
	 * Creates a new {@link VndErrors} wrapper for the given {@link VndErrors}.
	 *
	 * @param errors must not be {@literal null} or empty.
	 */
	@JsonCreator
	public VndErrors(@JsonProperty("_embedded") List<VndError> errors, @JsonProperty("message") String message,
			@JsonProperty("logref") Object logref, @JsonProperty("_links") Links links) {

		Assert.notNull(errors, "Errors must not be null!"); // Retain for compatibility
		Assert.notEmpty(errors, "Errors must not be empty!");

		this.errors = errors;
		this.message = message;
		this.logref = logref;

		if (links != null && !links.isEmpty()) {
			add(links);
		}
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

	public VndErrors withLinks(Link... links) {

		add(links);
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
	 * Virtual attribute to generate JSON field of {@literal total}. Only generated when there are multiple errors.
	 */
	@Nullable
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Integer getTotal() {

		List<VndError> errors = this.errors;

		if (errors == null) {
			return null;
		}

		return this.errors.size() > 1 //
				? this.errors.size() //
				: null; //
	}

	/**
	 * Adds an additional {@link VndError} to the wrapper.
	 *
	 * @param error
	 * @deprecated Use {{@link #withError(VndError)}}
	 */
	@Deprecated
	public VndErrors add(VndError error) {
		return withError(error);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<VndErrors.VndError> iterator() {
		return this.errors.iterator();
	}

	@Override
	public String toString() {
		return String.format("VndErrors[%s]", StringUtils.collectionToCommaDelimitedString(this.errors));
	}

	public String getMessage() {
		return this.message;
	}

	public Object getLogref() {
		return this.logref;
	}

	@Override
	public boolean equals(Object o) {

		if (this == o) {
			return true;
		}
		if (!(o instanceof VndErrors)) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		VndErrors vndErrors = (VndErrors) o;
		return Objects.equals(this.errors, vndErrors.errors) && Objects.equals(this.message, vndErrors.message)
				&& Objects.equals(this.logref, vndErrors.logref);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), this.errors, this.message, this.logref);
	}

	/**
	 * A single {@link VndError}.
	 *
	 * @author Oliver Gierke
	 * @author Greg Turnquist
	 * @deprecated Use {@link org.springframework.hateoas.mediatype.problem.Problem} to form vendor neutral error
	 *             messages.
	 */
	@JsonPropertyOrder({ "message", "path", "logref" })
	@Relation(collectionRelation = "errors")
	@Deprecated
	public static class VndError extends RepresentationModel<VndError> {

		private final String message;

		private final @Nullable String path;

		private final Object logref;

		/**
		 * Creates a new {@link VndError} with a message and optional a path and a logref.
		 *
		 * @param message must not be {@literal null} or empty.
		 * @param path
		 * @param logref must not be {@literal null} or empty.
		 * @param links
		 */
		@JsonCreator
		public VndError(@JsonProperty("message") String message, @JsonProperty("path") @Nullable String path,
				@JsonProperty("logref") Object logref, @JsonProperty("_links") List<Link> links) {

			Assert.hasText(message, "Message must not be null or empty!");

			this.message = message;
			this.path = path;
			this.logref = logref;
			this.add(links);
		}

		public VndError(String message, @Nullable String path, Object logref, Link... link) {
			this(message, path, logref, Arrays.asList(link));
		}

		/**
		 * @deprecated Use {@link #VndError(String, String, Object, Link...)} (with proper ordering of arguments)
		 */
		@Deprecated
		public VndError(String logref, String message, Link... links) {
			this(message, null, logref, Arrays.asList(links));
		}

		public String getMessage() {
			return this.message;
		}

		@Nullable
		@JsonInclude(JsonInclude.Include.NON_EMPTY)
		public String getPath() {
			return this.path;
		}

		@JsonInclude(JsonInclude.Include.NON_EMPTY)
		public Object getLogref() {
			return this.logref;
		}

		@Override
		public boolean equals(Object o) {

			if (this == o) {
				return true;
			}
			if (!(o instanceof VndError)) {
				return false;
			}
			if (!super.equals(o)) {
				return false;
			}
			VndError vndError = (VndError) o;
			return Objects.equals(this.message, vndError.message) && Objects.equals(this.path, vndError.path)
					&& Objects.equals(this.logref, vndError.logref);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), this.message, this.path, this.logref);
		}

		@Override
		public String toString() {

			return String.format("VndError[logref: %s, message: %s, links: [%s]]", this.logref, this.message,
					getLinks().toString());
		}
	}
}
