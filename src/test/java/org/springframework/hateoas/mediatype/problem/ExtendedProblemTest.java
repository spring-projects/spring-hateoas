package org.springframework.hateoas.mediatype.problem;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExtendedProblemTest {

    @Test
    public void testWithStatusKeepsAdditionalProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("apples", "don't taste like oranges");

        Problem.ExtendedProblem<Map<String, String>> problem = Problem.create().withProperties(properties).withStatus(HttpStatus.OK);

        Map<String, Object> resultMap = problem.getPropertiesAsMap();

        assertNotNull(resultMap);
        assertThat(resultMap.values(), iterableWithSize(1));
        assertThat(resultMap.get("apples"), is("don't taste like oranges"));
    }

    @Test
    public void testWithDetailKeepsAdditionalProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("apples", "don't taste like oranges");

        Problem.ExtendedProblem<Map<String, String>> problem = Problem.create().withProperties(properties).withDetail("Apples don't taste like oranges");

        Map<String, Object> resultMap = problem.getPropertiesAsMap();

        assertNotNull(resultMap);
        assertThat(resultMap.values(), iterableWithSize(1));
        assertThat(resultMap.get("apples"), is("don't taste like oranges"));
    }

    @Test
    public void testWithInstanceKeepsAdditionalProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("apples", "don't taste like oranges");

        Problem.ExtendedProblem<Map<String, String>> problem = Problem.create().withProperties(properties).withInstance(URI.create("https://github.com/spring-projects/spring-hateoas"));

        Map<String, Object> resultMap = problem.getPropertiesAsMap();

        assertNotNull(resultMap);
        assertThat(resultMap.values(), iterableWithSize(1));
        assertThat(resultMap.get("apples"), is("don't taste like oranges"));
    }

    @Test
    public void testWithTitleKeepsAdditionalProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("apples", "don't taste like oranges");

        Problem.ExtendedProblem<Map<String, String>> problem = Problem.create().withProperties(properties).withTitle("Spring HATEOAS");

        Map<String, Object> resultMap = problem.getPropertiesAsMap();

        assertNotNull(resultMap);
        assertThat(resultMap.values(), iterableWithSize(1));
        assertThat(resultMap.get("apples"), is("don't taste like oranges"));
    }

    @Test
    public void testWithTypeKeepsAdditionalProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("apples", "don't taste like oranges");

        Problem.ExtendedProblem<Map<String, String>> problem = Problem.create().withProperties(properties).withType(URI.create("http://example.com/problem-details"));

        Map<String, Object> resultMap = problem.getPropertiesAsMap();

        assertNotNull(resultMap);
        assertThat(resultMap.values(), iterableWithSize(1));
        assertThat(resultMap.get("apples"), is("don't taste like oranges"));
    }
}
