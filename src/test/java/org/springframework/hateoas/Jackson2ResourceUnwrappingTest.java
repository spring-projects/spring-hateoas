package org.springframework.hateoas;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test checks whether the produced JSON by {@linkplain ObjectMapper} has unwrapped content of {@linkplain Resource} class.
 *
 * @author Przemek Nowak
 */
public class Jackson2ResourceUnwrappingTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE);
    }

    /**
     * @see #414
     *
     * @throws Exception
     */
    @Test
    public void shouldSerializeUnwrappedJson() throws Exception {
        Resource<Person> personResource = new Resource<Person>(new Person("Harry"));
        String value = mapper.writeValueAsString(personResource);
        MatcherAssert.assertThat(value, Matchers.is("{\"firstName\":\"Harry\",\"links\":[]}"));
    }

    private static class Person {
        private String firstName;

        private Person(String firstName) {
            this.firstName = firstName;
        }
    }

}
