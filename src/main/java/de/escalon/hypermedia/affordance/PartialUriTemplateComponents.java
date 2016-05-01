/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.affordance;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Represents components of a Uri Template with variables.
 *
 * Created by dschulten on 04.12.2014.
 */
public class PartialUriTemplateComponents {

    private String baseUri;
    private String queryHead;
    private String queryTail;
    private String fragmentIdentifier;
    private List<String> variableNames;

    /**
     * Represents components of a Uri Template with variables.
     *
     * @param fragmentIdentifier,
     *         beginning with #, may contain a fragment variable, may also be empty
     * @param baseUri
     *         may be relative or absolute, and may contain {xxx} or {/xxx} style variables
     * @param queryHead
     *         start of query containing expanded key-value pairs (no variables), beginning with ?, may
     *         be empty
     * @param queryTail
     *         comma-separated list of unexpanded query keys, may be empty
     * @param variableNames names of template variables
     */
    public PartialUriTemplateComponents(String baseUri, String queryHead, String queryTail, String
            fragmentIdentifier, List<String> variableNames) {
        Assert.notNull(baseUri);
        Assert.notNull(queryHead);
        Assert.notNull(queryTail);
        Assert.notNull(fragmentIdentifier);
        Assert.notNull(variableNames);
        this.baseUri = baseUri;
        this.queryHead = queryHead;
        this.queryTail = queryTail;
        this.fragmentIdentifier = fragmentIdentifier;
        this.variableNames = variableNames;
    }

    public List<String> getVariableNames() {
        return variableNames;
    }

    public boolean isBaseUriTemplated() {
        return baseUri.matches(".*\\{.+\\}.*");
    }

    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Query head starting with ? continued by expanded query parameters, separated by &amp;
     *
     * @return query head, may be empty
     */
    public String getQueryHead() {
        return queryHead;
    }

    /**
     * Query tail containing unexpanded query parameters as comma-separated list.
     *
     * @return query tail, may be empty
     */
    public String getQueryTail() {
        return queryTail;
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
        return baseUri.contains("{") || !StringUtils.isEmpty(queryTail) || fragmentIdentifier.contains("{");
    }


}
