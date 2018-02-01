/*
 * Copyright 2013 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A representation model class to be rendered as specified for the media type {@code application/vnd.error}.
 * 
 * @see https://github.com/blongden/vnd.error
 * @author Oliver Gierke
 */
public class VndErrors implements Iterable<VndErrors.VndError> {

	private final List<VndError> vndErrors;

	/**
	 * Creates a new {@link VndErrors} instance containing a single {@link VndError} with the given logref, message and
	 * optional {@link Link}s.
	 * 
	 * @param logref must not be {@literal null} or empty.
	 * @param message must not be {@literal null} or empty.
	 * @param links
	 */
	public VndErrors(String logref, String message, Link... links) {
		this(new VndError(logref, message, links));
	}

	/**
	 * Creates a new {@link VndErrors} wrapper for at least one {@link VndError}.
	 * 
	 * @param errors must not be {@literal null}.
	 * @param errors
	 */
	public VndErrors(VndError error, VndError... errors) {

		Assert.notNull(error, "Error must not be null");

		this.vndErrors = new ArrayList<VndError>(errors.length + 1);
		this.vndErrors.add(error);
		this.vndErrors.addAll(Arrays.asList(errors));
	}

	/**
	 * Creates a new {@link VndErrors} wrapper for the given {@link VndErrors}.
	 * 
	 * @param errors must not be {@literal null} or empty.
	 */
	@JsonCreator
	public VndErrors(List<VndError> errors) {

		Assert.notNull(errors, "Errors must not be null!");
		Assert.isTrue(!errors.isEmpty(), "Errors must not be empty!");
		this.vndErrors = errors;
	}

	/**
	 * Protected default constructor to allow JAXB marshalling.
	 */
	protected VndErrors() {
		this.vndErrors = new ArrayList<VndError>();
	}

	/**
	 * Adds an additional {@link VndError} to the wrapper.
	 * 
	 * @param errors
	 */
	public VndErrors add(VndError error) {
		this.vndErrors.add(error);
		return this;
	}

	/**
	 * Dummy method to allow {@link JsonValue} to be configured.
	 * 
	 * @return the vndErrors
	 */
	@JsonValue
	private List<VndError> getErrors() {
		return vndErrors;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<VndErrors.VndError> iterator() {
		return this.vndErrors.iterator();
	}

	/* 
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("VndErrors[%s]", StringUtils.collectionToCommaDelimitedString(vndErrors));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return vndErrors.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}

		if (!(obj instanceof VndErrors)) {
			return false;
		}

		VndErrors that = (VndErrors) obj;
		return this.vndErrors.equals(that.vndErrors);
	}

	/**
	 * A single {@link VndError}.
	 * 
	 * @author Oliver Gierke
	 */
	public static class VndError extends ResourceSupport {

		@JsonProperty private final String logref;
		@JsonProperty private final String message;

		/**
		 * Creates a new {@link VndError} with the given logref, a message as well as some {@link Link}s.
		 * 
		 * @param logref must not be {@literal null} or empty.
		 * @param message must not be {@literal null} or empty.
		 * @param links
		 */
		public VndError(String logref, String message, Link... links) {

			Assert.hasText(logref, "Logref must not be null or empty!");
			Assert.hasText(message, "Message must not be null or empty!");

			this.logref = logref;
			this.message = message;
			this.add(Arrays.asList(links));
		}

		/**
		 * Protected default constructor to allow JAXB marshalling.
		 */
		protected VndError() {

			this.logref = null;
			this.message = null;
		}

		/**
		 * Returns the logref of the error.
		 * 
		 * @return the logref
		 */
		public String getLogref() {
			return logref;
		}

		/**
		 * Returns the message of the error.
		 * 
		 * @return the message
		 */
		public String getMessage() {
			return message;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.ResourceSupport#toString()
		 */
		@Override
		public String toString() {
			return String.format("VndError[logref: %s, message: %s, links: [%s]]", logref, message,
					StringUtils.collectionToCommaDelimitedString(getLinks()));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.ResourceSupport#hashCode()
		 */
		@Override
		public int hashCode() {

			int result = 17;

			result += 31 * logref.hashCode();
			result += 31 * message.hashCode();

			return result;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.ResourceSupport#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {

			if (obj == this) {
				return true;
			}

			if (!(obj instanceof VndError)) {
				return false;
			}

			VndError that = (VndError) obj;

			return this.logref.equals(that.logref) && this.message.equals(that.message);
		}
	}
}
