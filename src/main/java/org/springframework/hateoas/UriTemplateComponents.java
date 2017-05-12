/*
 * Copyright 2014-2017 the original author or authors.
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

import java.util.List;

import lombok.Value;

import org.springframework.util.StringUtils;

/**
 * Represents components of a Uri Template with variables. Created by dschulten on 04.12.2014.
 */
@Value
public class UriTemplateComponents {

	/**
	 * May be relative or absolute, and may contain {xxx} or {/xxx} style variables.
	 */
	String baseUri;

	/**
	 * Start of query containing expanded key-value pairs (no variables), beginning with ?, may be empty.
	 */
	String queryHead;

	/**
	 * Comma-separated list of unexpanded query keys, may be empty.
	 */
	String queryTail;

	/**
	 * Beginning with #, may contain a fragment variable, may also be empty.
	 */
	String fragmentIdentifier;

	/**
	 * Names of template variables
	 */
	List<String> variableNames;

	/**
	 * Returns whether the base URI is templated.
	 * 
	 * @return
	 */
	public boolean isBaseUriTemplated() {
		return baseUri.matches(".*\\{.+\\}.*");
	}

	/**
	 * Query consisting of expanded parameters and unexpanded parameters.
	 *
	 * @return query, may be empty
	 */
	public String getQuery() {

		StringBuilder query = new StringBuilder();

		if (queryTail.length() > 0) {

			if (queryHead.length() == 0) {
				query.append("{?").append(queryTail).append("}");
			} else if (queryHead.length() > 0) {
				query.append(queryHead).append("{&").append(queryTail).append("}");
			}

		} else {
			query.append(queryHead);
		}

		return query.toString();
	}

	/**
	 * Returns whether the components contain a variable.
	 * 
	 * @return
	 */
	public boolean hasVariables() {
		return baseUri.contains("{") || !StringUtils.isEmpty(queryTail) || fragmentIdentifier.contains("{");
	}

	/**
	 * Concatenates all components to uri String.
	 *
	 * @return uri String
	 */
	public String toString() {
		return baseUri + getQuery() + fragmentIdentifier;
	}
}
