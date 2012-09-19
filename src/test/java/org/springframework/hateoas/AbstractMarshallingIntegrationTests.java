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
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Base class to eas integration tests for {@link ObjectMapper} marshalling.
 *
 * @author Oliver Gierke
 */
public abstract class AbstractMarshallingIntegrationTests {

    ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    protected String write(Object object) throws Exception {
        return mapper.writeValueAsString(object);
    }

    protected JsonNode readTree(String json) throws IOException {
        return mapper.readTree(json);
    }

    protected <T> T read(String source, Class<T> targetType) throws Exception {
   		return mapper.readValue(source, targetType);
   	}

    protected String getAsPrettyJSON(Object o) {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertHasValue(JsonNode jsonNode, String fieldName, String value) {
        assertThat(fieldName + " is missing", jsonNode.has(fieldName), is(true));
        assertThat(field(fieldName).inNode(jsonNode), is(value));
    }

    protected FieldGetter field(String fieldName) {
        return new FieldGetter(fieldName);
    }

    protected static class FieldGetter {
        private final String fieldName;

        private FieldGetter(String fieldName) {
            this.fieldName = fieldName;
        }

        private String inNode(JsonNode node) {
            return node.get(fieldName).asText();
        }
    }

}
