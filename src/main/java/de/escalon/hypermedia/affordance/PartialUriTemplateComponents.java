/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.affordance;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Represents uri template components which might contain template variables.
 * Created by dschulten on 04.12.2014.
 */
public class PartialUriTemplateComponents {

	private String baseUri;
	private String queryHead;
	private String queryTail;
	private String fragmentIdentifier;

	/**
	 * Represents components of a Uri Template with variables.
	 *
	 * @param baseUri may be relative or absolute, and may contain {xxx} or {/xxx} style variables
	 * @param queryHead start of query containing expanded key-value pairs (no variables), beginning with ?, may be empty
	 * @param queryTail comma-separated list of unexpanded query keys, may be empty
	 * @param fragmentIdentifier, beginning with #, may contain a fragment variable, may also be empty
	 */
	public PartialUriTemplateComponents(String baseUri, String queryHead, String queryTail, String fragmentIdentifier) {
		Assert.notNull(baseUri);
		Assert.notNull(queryHead);
		Assert.notNull(queryTail);
		Assert.notNull(fragmentIdentifier);
		this.baseUri = baseUri;
		this.queryHead = queryHead;
		this.queryTail = queryTail;
		this.fragmentIdentifier = fragmentIdentifier;
	}

	public boolean isBaseUriTemplated() {
		return hasVariables(baseUri);
	}

	private boolean hasVariables(String component) {
		return component.matches(".*\\{.+\\}.*");
	}

	public String getBaseUri() {
		return baseUri;
	}

	public String getQueryHead() {
		return queryHead;
	}

	public String getQueryTail() {
		return queryTail;
	}

	public String getQuery() {
		StringBuilder query = new StringBuilder();
		if (queryTail.length() > 0) {
			if (queryHead.length() == 0) {
				query.append("{?")
						.append(queryTail)
						.append("}");
			} else if (queryHead.length() > 0) {
				query.append(queryHead)
						.append("{&")
						.append(queryTail)
						.append("}");
			}
		} else {
			query.append(queryHead);
		}
		return query.toString();
	}


	public String getFragmentIdentifier() {
		return fragmentIdentifier;
	}

	/**
	 * Concatenates all components to uri String.
	 *
	 * @return uri String
	 */
	public String toString() {
		return baseUri + getQuery() + fragmentIdentifier;
	}

	public boolean hasVariables() {
		return hasVariables(baseUri) || !StringUtils.isEmpty(queryTail) || hasVariables(fragmentIdentifier);
	}
}
