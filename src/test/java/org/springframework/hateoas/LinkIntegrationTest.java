/*
 * Copyright 2012 the original author or authors.
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

import org.codehaus.jackson.JsonNode;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration tests for {@link Link} marshaling.
 *
 * @author Oliver Gierke
 * @author Daniel Sawano
 */
public class LinkIntegrationTest extends AbstractMarshallingIntegrationTests {

	private static final String REFERENCE = "{\"rel\":\"something\",\"href\":\"location\"}";

	/**
	 * @see #14
	 */
	@Test
	public void writesLinkCorrectly() throws Exception {
		assertThat(write(new Link("location", "something")), is(REFERENCE));
	}

	/**
	 * @see #14
	 */
	@Test
	public void readsLinkCorrectly() throws Exception {

		Link result = read(REFERENCE, Link.class);
		assertThat(result.getHref(), is("location"));
		assertThat(result.getRel(), is("something"));
	}

    @Test
    public void serializeToJSON() throws Exception {
        Link link = new Link("http://localhost", Link.REL_SELF, HttpMethod.GET);

        String json = write(link);
        JsonNode jsonNode = readTree(json);

        assertHasValue(jsonNode, "href", "http://localhost");
        assertHasValue(jsonNode, "rel", Link.REL_SELF);
        assertHasValue(jsonNode, "method", HttpMethod.GET.toString());
    }

    @Test
    public void nullMethodValueShouldNotBeSerializedToJSON() throws Exception {
        Link link = new Link("http://localhost", Link.REL_SELF, null);

        String json = write(link);
        JsonNode jsonNode = readTree(json);

        assertHasValue(jsonNode, "href", "http://localhost");
        assertHasValue(jsonNode, "rel", Link.REL_SELF);
        assertThat("method should not be serialized when null", jsonNode.has("method"), is(false));
    }

    @Test
    public void deserializeFromJSON() throws Exception {
        String json = "{\"rel\": \"self\", \"href\": \"http://localhost\", \"method\": \"POST\"}";

        Link link = read(json, Link.class);

        assertThat(link.getHref(), is("http://localhost"));
        assertThat(link.getMethod(), is(HttpMethod.POST));
        assertThat(link.getRel(), is(Link.REL_SELF));
    }
}
