/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring;

import org.springframework.http.MediaType;

/**
 * Pre-instantiated hypermedia types.
 * Created by dschulten on 12.11.2014.
 */
public final class HypermediaTypes {

    private HypermediaTypes() {
        // prevent instantiation
    }

    public static final String APPLICATION_JSONLD_STR = "application/ld+json";
    public static final MediaType APPLICATION_JSONLD = MediaType.parseMediaType(APPLICATION_JSONLD_STR);
    public static final String UBER_XML_STR="application/vnd.uber+xml";
    public static final MediaType UBER_XML = MediaType.parseMediaType(UBER_XML_STR);
    public static final String UBER_JSON_STR="application/vnd.uber+json";
    public static final MediaType UBER_JSON = MediaType.parseMediaType(UBER_JSON_STR);

}
