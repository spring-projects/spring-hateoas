/*
 * Copyright 2002-2015 the original author or authors.
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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ResourcesLinksVisible} marker interface.
 * 
 * The marker face is used to enable JSON serialization filter annotations applied to {@Resource}s
 * as intended by providing a reasonable base marker interface for those annotations.
 * 
 * The {@link JsonView} annotation supports filtering class trees in order to provide custom views
 * on specific partial data
 * 
 * The main purpose of {@link ResourcesLinksVisible} is to provide a means of views including the
 * links properties of REST models using {@link Resource}/{@link Resources} support.
 * 
 * <b>Usage</b> extend {@link ResourcesLinksVisible} to denote your view properties or use a common
 * subinterface including {@link ResourcesLinksVisible}.
 *
 * @see https://spring.io/blog/2014/12/02/latest-jackson-integration-improvements-in-spring
 * @author Karsten Tinnefeld
 */
public class ResourceLinksVisibleTest {

	private ObjectMapper mapper;
	private Link someLink;

	@Before
	public void setUp() {

		mapper = new ObjectMapper();
		mapper.setSerializationInclusion(NON_NULL);

		someLink = new Link("localhost");
	}

	/**
	 * Under normal processing, {@link Resource}s just map to a JSON object with a links field
	 * added.
	 * 
	 * @throws JsonProcessingException Treat JSON processing errors as test failures.
	 */
	@Test
	public void resourceSerializationHasVisibleFieldsAndLinks()
			throws JsonProcessingException {

		SomeBean someBean = new SomeBean();
		String serializedBean = mapper.writeValueAsString(someBean);

		assertContains("text field missing", serializedBean, "content");
		assertContains("number field missing", serializedBean, "1.5");
		assertDoesNotContains("invisible field found", serializedBean, "true");
		assertDoesNotContains("where do these links come from", serializedBean,
				"localhost");

		String serializedResource =
				mapper.writeValueAsString(new Resource<SomeBean>(someBean, someLink));

		assertContains("text field missing", serializedResource, "content");
		assertContains("number field missing", serializedResource, "1.5");
		assertDoesNotContains("invisible field found", serializedResource, "true");
		assertContains("links missing", serializedResource, "localhost");
	}

	/**
	 * Under normal processing, {@link Resources} map to a JSON array with a links field added.
	 * 
	 * @throws JsonProcessingException Treat JSON processing errors as test failures.
	 */
	@Test
	public void resourcesSerializationHasVisibleFieldsAndLinks()
			throws JsonProcessingException {

		SomeBean someBean = new SomeBean();
		Collection<SomeBean> someBeans = Arrays.asList(someBean, someBean, someBean);

		String serializedResources =
				mapper.writeValueAsString(new Resources<SomeBean>(someBeans, someLink));

		assertContains("text field missing", serializedResources, "content");
		assertContains("number field missing", serializedResources, "1.5");
		assertDoesNotContains("invisible field found", serializedResources, "true");

		assertHasSeveralOf("text field expected three times", serializedResources, 3,
				"number");
		assertContains("links missing", serializedResources, "localhost");
	}

	/**
	 * Under processing with a view, however, {@link Resource}s just map to the empty object and are
	 * thus not very useful.
	 * 
	 * Annotation based usage would be something like
	 * 
	 * <pre>
	 * &#64;RestController
	 * &#64;JsonView(NewJsonView.class)
	 * public Resource<BeanWithView> viewToABean() { ... }
	 * </pre>
	 * 
	 * @throws JsonProcessingException Treat JSON processing errors as test failures.
	 */
	@Test
	public void resourceSerializationOfViewMissesLinks() throws JsonProcessingException {

		ObjectWriter newJsonViewBasedWriter = mapper.writerWithView(NewJsonView.class);

		BeanWithView someBean = new BeanWithView();
		String serializedBean = newJsonViewBasedWriter.writeValueAsString(someBean);

		assertContains("text field missing", serializedBean, "content");
		assertContains("number field missing", serializedBean, "1.5");
		assertDoesNotContains("invisible field found", serializedBean, "true");
		assertDoesNotContains("where do these links come from", serializedBean,
				"localhost");

		String serializedResource = newJsonViewBasedWriter
				.writeValueAsString(new Resource<BeanWithView>(someBean, someLink));
		assertEquals("Empty JSON object expected", "{}", serializedResource);
	}

	/**
	 * Under processing with a view, however, {@link Resources} just map to the empty object and are
	 * thus not very useful.
	 * 
	 * Annotation based usage would be something like
	 * 
	 * <pre>
	 * &#64;RestController
	 * &#64;JsonView(NewJsonView.class)
	 * public Resources<BeanWithView> viewToSomeBean() { ... }
	 * </pre>
	 * 
	 * @throws JsonProcessingException Treat JSON processing errors as test failures.
	 */
	@Test
	public void resourcesSerializationOfViewMissesLinks() throws JsonProcessingException {

		ObjectWriter newJsonViewBasedWriter = mapper.writerWithView(NewJsonView.class);

		BeanWithView someBean = new BeanWithView();
		Collection<BeanWithView> someBeans = Arrays.asList(someBean, someBean, someBean);

		String serializedResources = newJsonViewBasedWriter
				.writeValueAsString(new Resources<BeanWithView>(someBeans, someLink));

		assertEquals("Empty JSON object expected", "{}", serializedResources);
	}

	/**
	 * Using views which inherit from the {@link ResourcesLinksVisible} marker interface, however,
	 * views to objects are processed in more a sensible way.
	 * 
	 * @throws JsonProcessingException Treat JSON processing errors as test failures.
	 */
	@Test
	public void resourceSerializationWithResourcesLinksVisibleSeesLinks()
			throws JsonProcessingException {

		ObjectWriter jsonViewWithLinksBasedWriter =
				mapper.writerWithView(JSonViewWithLinks.class);

		BeanWithView someBean = new BeanWithView();
		String serializedBean = jsonViewWithLinksBasedWriter.writeValueAsString(someBean);

		assertContains("text field missing", serializedBean, "content");
		assertContains("number field missing", serializedBean, "1.5");
		assertDoesNotContains("invisible field found", serializedBean, "true");
		assertDoesNotContains("where do these links come from", serializedBean,
				"localhost");

		String serializedResource = jsonViewWithLinksBasedWriter
				.writeValueAsString(new Resource<BeanWithView>(someBean, someLink));

		assertContains("text field missing", serializedResource, "content");
		assertContains("number field missing", serializedResource, "1.5");
		assertDoesNotContains("invisible field found", serializedResource, "true");
		assertContains("links missing", serializedResource, "localhost");
	}

	/**
	 * Using views which inherit from the {@link ResourcesLinksVisible} marker interface, however,
	 * views to objects are processed in more a sensible way.
	 * 
	 * @throws JsonProcessingException Treat JSON processing errors as test failures.
	 */
	@Test
	public void resourcesSerializationWithResourcesLinksVisibleSeesLinks()
			throws JsonProcessingException {

		ObjectWriter jsonViewWithLinksBasedWriter =
				mapper.writerWithView(JSonViewWithLinks.class);

		BeanWithView someBean = new BeanWithView();
		Collection<BeanWithView> someBeans = Arrays.asList(someBean, someBean, someBean);

		String serializedResources = jsonViewWithLinksBasedWriter
				.writeValueAsString(new Resources<BeanWithView>(someBeans, someLink));

		assertContains("text field missing", serializedResources, "content");
		assertContains("number field missing", serializedResources, "1.5");
		assertDoesNotContains("invisible field found", serializedResources, "true");

		assertHasSeveralOf("text field expected three times", serializedResources, 3,
				"number");
		assertContains("links missing", serializedResources, "localhost");
	}

	public static interface NewJsonView {}
	public static interface JSonViewWithLinks
			extends ResourcesLinksVisible, NewJsonView {}

	public static class SomeBean {
		public double getNumber() {
			return 1.5d;
		}

		public String getText() {
			return "content";
		}

		@JsonIgnore
		public boolean isInvisible() {
			return true;
		}
	}

	public static class BeanWithView {
		@JsonView(NewJsonView.class)
		public double getNumber() {
			return 1.5d;
		}

		@JsonView(NewJsonView.class)
		public String getText() {
			return "content";
		}

		@JsonIgnore
		public boolean isInvisible() {
			return true;
		}
	}

	private void assertContains(String message, String fullstring, String part) {
		assertNotNull(message, fullstring);
		assertNotNull(message, part);
		assertTrue(message, fullstring.contains(part));
	}

	private void assertDoesNotContains(String message, String fullstring, String part) {
		assertNotNull(message, part);
		assertTrue(message, fullstring == null || !fullstring.contains(part));
	}

	private void assertHasSeveralOf(String message, String fullstring, int number,
			String part) {
		assertNotNull(message, fullstring);
		assertNotNull(message, part);
		String quotedPart = Pattern.quote(part);
		String[] fullstringParts = fullstring.split(quotedPart);
		assertEquals(message, number + 1, fullstringParts.length);
	}
}
