/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.forms.FieldNotFoundException;
import org.springframework.hateoas.forms.LinkSuggest;
import org.springframework.hateoas.forms.Property;
import org.springframework.hateoas.forms.PropertyBuilder;
import org.springframework.hateoas.forms.Template;
import org.springframework.hateoas.forms.ValueSuggest;
import org.springframework.hateoas.forms.ValueSuggest.ValueSuggestType;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ControllerFormBuilderUnitTest {

	private static final String CREATE_ITEM_FORM_KEY = "create-item";

	protected MockHttpServletRequest request;

	protected List<Size> sizes;

	@Before
	public void setUp() {

		request = new MockHttpServletRequest();
		ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
		RequestContextHolder.setRequestAttributes(requestAttributes);

		sizes = Arrays.asList(new Size("big"), new Size("small"));
	}

	@Test
	public void testRequestBodyField() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));
		PropertyBuilder propertyBuilder = formBuilder.property("size");

		assertEquals(propertyBuilder.getDeclaringClass(), Size.class);
	}

	@Test
	public void testRequestBodyFieldNested() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));
		PropertyBuilder propertyBuilder = formBuilder.property("size.id");

		assertTrue(propertyBuilder.getDeclaringClass().isAssignableFrom(String.class));
	}

	@Test
	public void testRequestBodyFieldNestedList() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(OrderController.class).create(new Order()));
		PropertyBuilder propertyBuilder = formBuilder.property("lineItems.milk");

		assertEquals(Milk.class, propertyBuilder.getDeclaringClass());
	}

	@Test
	public void testRequestBodyFieldNotFound() {
		try {
			ControllerFormBuilder formBuilder = ControllerFormBuilder
					.formTo(ControllerFormBuilder.methodOn(OrderController.class).create(new Order()));
			formBuilder.property("lineItems.missing");
		}
		catch (FieldNotFoundException e) {
			assertEquals(LineItem.class, e.getTargetClass());
			assertThat(e.getField(), is("missing"));
		}
	}

	@Test
	public void testFormToMethod() throws NoSuchMethodException{
		ControllerFormBuilder formBuilder = ControllerFormBuilder.formTo(ItemController.class,
				ItemController.class.getMethod("create", Item.class), new Item());

		Template createItemForm = formBuilder.withKey(CREATE_ITEM_FORM_KEY);

		assertThat(createItemForm.getMethod()[0], is(RequestMethod.POST));

		// TODO: identify @RequestBody from parameters
		// assertNotNull(createItemForm.getBody());
	}

	@Test
	public void testFormContentType() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(OrderController.class).create(new Order()));

		assertThat(formBuilder.getContentType(), is(MediaType.APPLICATION_FORM_URLENCODED_VALUE));

		ControllerFormBuilder formBuilder2 = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));

		assertNull(formBuilder2.getContentType());
	}

	@Test
	public void testWithKey() {

		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));

		Template createItemForm = formBuilder.withKey(CREATE_ITEM_FORM_KEY);

		assertThat(createItemForm.getMethod().length, is(1));
		assertThat(createItemForm.getMethod()[0], is(RequestMethod.POST));
		assertThat(createItemForm.getRel(), is(CREATE_ITEM_FORM_KEY));
	}

	@Test
	public void testPropertyAttributes() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));

		formBuilder.property("name").readonly(false).regex("^(true|false)$").prompt("Item name");
		Template createItemForm = formBuilder.withKey(CREATE_ITEM_FORM_KEY);

		assertThat(createItemForm.getProperties().size(), is(1));

		Property name = createItemForm.getProperty("name");
		assertNotNull(name);

		assertFalse(name.isReadOnly());
		assertThat(name.getRegex(), is("^(true|false)$"));
		assertThat(name.getPrompt(), is("Item name"));
	}

	@Test
	public void testPropertyTemplated() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));

		formBuilder.property("name").value("{propertyFromResource}");
		Template createItemForm = formBuilder.withKey(CREATE_ITEM_FORM_KEY);

		assertThat(createItemForm.getProperties().size(), is(1));

		Property name = createItemForm.getProperty("name");
		assertNotNull(name);

		assertTrue(name.isTemplated());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPropertySuggestDirectValues() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));

		formBuilder.property("size").suggest().values(sizes).textField("desc").valueField("name");

		Template createItemForm = formBuilder.withDefaultKey();

		Property size = createItemForm.getProperty("size");

		assertNotNull(size.getSuggest());
		assertThat(size.getSuggest().getTextField(), is("desc"));
		assertThat(size.getSuggest().getValueField(), is("name"));

		assertThat(size.getSuggest(), instanceOf(ValueSuggest.class));

		ValueSuggest<Size> suggest = (ValueSuggest<Size>) size.getSuggest();
		assertThat(suggest.getType(), is(ValueSuggestType.DIRECT));
		assertThat(suggest.getValues().iterator().next().getId(), is("big"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPropertySuggestEmbeddedValues() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));

		formBuilder.property("size").suggest().embedded(sizes).textField("desc").valueField("name");

		Template createItemForm = formBuilder.withDefaultKey();

		Property size = createItemForm.getProperty("size");

		assertNotNull(size.getSuggest());
		assertThat(size.getSuggest().getTextField(), is("desc"));
		assertThat(size.getSuggest().getValueField(), is("name"));

		assertThat(size.getSuggest(), instanceOf(ValueSuggest.class));

		ValueSuggest<Size> suggest = (ValueSuggest<Size>) size.getSuggest();
		assertThat(suggest.getType(), is(ValueSuggestType.EMBEDDED));
		assertThat(suggest.getValues().iterator().next().getId(), is("big"));
	}

	@Test
	public void testPropertySuggestLinkValue() {
		ControllerFormBuilder formBuilder = ControllerFormBuilder
				.formTo(ControllerFormBuilder.methodOn(ItemController.class).create(new Item()));

		Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(SizeController.class).get())
				.withRel("sizes");
		formBuilder.property("size").suggest().link(link).textField("desc").valueField("name");

		Template createItemForm = formBuilder.withDefaultKey();

		Property size = createItemForm.getProperty("size");

		assertNotNull(size.getSuggest());
		assertThat(size.getSuggest().getTextField(), is("desc"));
		assertThat(size.getSuggest().getValueField(), is("name"));

		assertThat(size.getSuggest(), instanceOf(LinkSuggest.class));

		LinkSuggest suggest = (LinkSuggest) size.getSuggest();
		assertThat(suggest.getLink(), is(link));
	}

	@RequestMapping("/items")
	private interface ItemController {

		@RequestMapping("")
		public HttpEntity<Item> list();

		@RequestMapping(method = RequestMethod.POST)
		public HttpEntity<Item> create(@RequestBody Item item);

		@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
		public HttpEntity<Item> update(@PathVariable String id, @RequestBody Item item);

	}

	@RequestMapping("/sizes")
	private interface SizeController {
		public Resources<Resource<Size>> get();
	}

	@RequestMapping("/orders")
	private interface OrderController {
		@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
		public Resource<Order> create(@RequestBody Order order);
	}

	public static class Size implements Identifiable<String> {

		private String name;

		private String desc;

		public Size() {
		}

		public Size(String name) {
			this(name, null);
		}

		public Size(String name, String desc) {
			this.name = name;
			this.desc = desc;
		}

		public String getId() {
			return name;
		}

		public String getDesc() {
			return desc;
		}

	}

	@SuppressWarnings("unused")
	public static class Item implements Identifiable<Long> {

		private Long id;

		private String name;

		private Size size;

		public Item() {
		}

		public Item(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

	@SuppressWarnings("unused")
	public class LineItem {

		private String name;

		private int quantity;

		private Milk milk;

		private Size size;

	}

	public enum Milk {
		WHOLE, SEMI;
	}

	@SuppressWarnings("unused")
	public class Order {
		private final Set<LineItem> lineItems = new HashSet<LineItem>();

	}

}
