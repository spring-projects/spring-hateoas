/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.hal.forms;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;
import static org.springframework.hateoas.affordance.springmvc.AffordanceBuilder.*;
import static org.springframework.hateoas.hal.forms.HalFormsDocument.*;
import static org.springframework.hateoas.hal.forms.Jackson2HalFormsModule.*;
import static org.springframework.hateoas.support.MappingUtils.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.affordance.Affordance;
import org.springframework.hateoas.affordance.Select;
import org.springframework.hateoas.affordance.Suggestions;
import org.springframework.hateoas.affordance.SuggestionsProvider;
import org.springframework.hateoas.affordance.formaction.DTOParam;
import org.springframework.hateoas.affordance.formaction.Input;
import org.springframework.hateoas.affordance.springmvc.AffordanceBuilder;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.Relation;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 *  @author Dietrich Schulten
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
public class HalFormsSerializationTest {

	@Autowired
	private WebApplicationContext wac;

	private ObjectMapper objectMapper;
	private RelProvider relProvider;
	private CurieProvider curieProvider;
	private MockMvc mockMvc;


	@Before
	public void setUp() {
		
		this.mockMvc = webAppContextSetup(wac).build();

		this.relProvider = new DefaultRelProvider();

		this.curieProvider = new DefaultCurieProvider("test",
			new UriTemplate("http://localhost:8080/profile/{rel}"));

		this.objectMapper = new ObjectMapper();
		this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		this.objectMapper.registerModule(new Jackson2HalModule());
		this.objectMapper.registerModule(new Jackson2HalFormsModule());
		this.objectMapper.setHandlerInstantiator(
			new HalFormsHandlerInstantiator(this.relProvider, this.curieProvider, null, true));
	}

	@Test
	public void serializingHalFormsDocumentShouldWork() throws IOException {

		Property property = new Property("my-name", true, "my-value", "my-prompt",
			"my-regex", false, true, false, Suggestions.remote(new Link("/foo/bar")));
		Template template = new Template();
		template.setHttpMethod(HttpMethod.GET);
		template.setContentType(Collections.singletonList(MediaTypes.HAL_JSON));
		template.setTitle("HAL-Forms unit test");
		template.getProperties().add(property);

		HalFormsDocument halFormsDocument = halFormsDocument()
			.link(new Link("/employees").withRel("collection"))
			.link(new Link("/employees/1").withSelfRel())
			.template(template)
			.build();

		String actual = this.objectMapper.writeValueAsString(halFormsDocument);
		assertThat(actual, is(read(new ClassPathResource("reference.json", getClass()))));
	}

	@Test
	public void deserializingHalFormsDocumentShouldWork() throws IOException {

		HalFormsDocument halFormsDocument = this.objectMapper.readValue(
			read(new ClassPathResource("reference.json", getClass())), HalFormsDocument.class);

		assertThat(halFormsDocument.getLinks().size(), is(2));
		assertThat(halFormsDocument.getLinks().get(0).getHref(), is("/employees"));
		assertThat(halFormsDocument.getLinks().get(1).getHref(), is("/employees/1"));

		assertThat(halFormsDocument.getTemplates().size(), is(1));
		assertThat(halFormsDocument.getTemplates().get(0).getContentType(), is("application/hal+json"));
		assertThat(halFormsDocument.getTemplates().get(0).getKey(), is(Template.DEFAULT_KEY));
		assertThat(halFormsDocument.getTemplates().get(0).getHttpMethod(), is(HttpMethod.GET));
		assertThat(halFormsDocument.getTemplates().get(0).getMethod(), is(HttpMethod.GET.toString().toLowerCase()));
	}

	@Test
	public void serializingTemplatesWithRequestBodyShouldWork() throws JsonProcessingException {

		AffordanceBuilder builder = linkTo(
				methodOn(DummyOrderController.class).addOrderItems(42, new OrderItem(42, null, null, null, null)));
		Link link = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).and(builder)
				.withSelfRel();

		Order order = new Order();
		order.add(link);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.writeValueAsString(entity);

		assertThat(json, hasJsonPath("$._templates"));
		assertThat(json, hasJsonPath("$._templates.default"));
		assertThat(json, hasJsonPath("$._templates.default.method", equalTo("post")));
		assertThat(json, hasJsonPath("$._templates.default.contentType", equalTo("application/json")));
		assertThat(json, hasJsonPath("$._templates.default.properties", hasSize(4)));
	}

	@Test
	public void serializingTemplatesFromRequestParamSimpleShouldWork() throws JsonProcessingException {

		Order order = new Order();
		order.add(linkTo(methodOn(DummyOrderController.class).getOrders(null)).withRel("orders"));

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.writeValueAsString(entity);

		assertThat(json, hasJsonPath("$._links['test:orders'].href", equalTo("http://localhost/orders{?attr}")));

		assertThat(json, hasNoJsonPath("$._templates"));
	}

	@Test
	public void testTemplatesFromRequestParamComplexWithoutRequestParamAnnotation() throws JsonProcessingException {

		Order order = new Order();
		Affordance affordance = linkTo(methodOn(DummyOrderController.class).getOrdersFiltered(new OrderFilter()))
				.withRel("orders");
		Assert.assertArrayEquals(new String[] { "count", "status" },
				affordance.getActionDescriptors().get(0).getRequestParamNames().toArray(new String[0]));

		order.add(affordance);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.writeValueAsString(entity);

		// If there are no @RequestParams, AffordanceBuilder doesn't declare a UriTemplate variable
		assertThat(json,
				hasJsonPath("$._links['test:orders'].href", equalTo("http://localhost/orders/filtered{?count,status}")));
	}

	@Test
	public void testTemplatesFromRequestParamComplexWithRequestParamAnnotation() throws JsonProcessingException {

		Order order = new Order();
		order.add(linkTo(methodOn(DummyOrderController.class).getOrdersFilteredWithRequestParam(null)).withRel("orders"));

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.writeValueAsString(entity);

		assertThat(json,
				hasJsonPath("$._links['test:orders'].href", equalTo("http://localhost/orders/filteredWithRP{?filter}")));

	}

	@Test
	public void testRequestWithStatusRequestParamNotFound() throws Exception {

		try {

			mockMvc.perform(get("http://localhost/orders/filteredWithRP?status=accepted"))//
					.andExpect(status().is4xxClientError());

			// Spring awaits a @RequestParam called "filter"
		} catch (MissingServletRequestParameterException e) {
			assertThat(e.getParameterName(), equalTo("filter"));
		}

	}

	@Test
	public void testRequestWithStatusFound() throws Exception {

		// If @RequestParam annotation is not present the request is correct
		mockMvc.perform(get("http://localhost/orders/filtered?status=accepted").accept(MediaType.APPLICATION_JSON))//
				.andExpect(status().isOk());

	}

	@Test
	public void testReadHalFormDocument() throws IOException {

		AffordanceBuilder builder = linkTo(
				methodOn(DummyOrderController.class).addOrderItems(42, new OrderItem(42, null, null, null, null)));
		Link link = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).and(builder)
				.withSelfRel();

		Order order = new Order();
		order.add(link);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.writeValueAsString(entity);

		HalFormsDocument doc = objectMapper.readValue(json, HalFormsDocument.class);

		assertThat(doc.getTemplates().size(), equalTo(1));
		assertThat(doc.getTemplate().getProperty("size").getSuggest(), notNullValue());
	}

	@Test
	public void testReadHalFormDocumentWithLinkArrays() throws IOException {

		Link link = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).withRel("orders");
		Link link2 = linkTo(methodOn(DummyOrderController.class).addOrderItemsPrepareForm(42, null)).withRel("orders");

		Order order = new Order();
		order.add(link, link2);

		Object entity = HalFormsUtils.toHalFormsDocument(order, objectMapper);
		String json = objectMapper.writeValueAsString(entity);

		HalFormsDocument doc = objectMapper.readValue(json, HalFormsDocument.class);

		assertThat(doc.getLinks().size(), is(3));
		assertThat(doc.getLinks().get(0).getRel(), equalTo(doc.getLinks().get(1).getRel()));
	}

	/**
	 * Support for this test suite
	 */

	@Value
	@Relation("customer")
	static class Customer {

		String customerId = "pj123";
		String name = "Peter Joseph";
	}

	@RequestMapping("/customers")
	static class DummyCustomersController {

		@RequestMapping("/{customerId}")
		public ResponseEntity<Resource<Customer>> getCustomer(@PathVariable final String customerId) {
			return null;
		}
	}

	@Value
	static class Size {

		String value, text;

		@JsonCreator
		public Size(@JsonProperty("value") final String value, @JsonProperty("text") final String text) {

			this.value = value;
			this.text = text;
		}
	}

	static class SizeOptions implements SuggestionsProvider {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.SuggestionsProvider#getSuggestions()
		 */
		@Override
		public Suggestions getSuggestions() {
			return Suggestions.values(new Size("small", "Small"), new Size("big", "Big")).withValueField("value")
				.withPromptField("text");
		}
	}

	static class RemoteOptions implements SuggestionsProvider {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.hateoas.affordance.SuggestionsProvider#getSuggestions()
		 */
		@Override
		public Suggestions getSuggestions() {
			return Suggestions.remote("http://localhost/orders/countries").withValueField("value").withPromptField("text");
		}
	}

	static class Country {

		int id;
		String name;
	}

	@Value
	static class OrderItem {

		int orderNumber;
		String productCode;
		Integer quantity;
		String size;
		Country country;

		@JsonCreator
		public OrderItem(@Input(required = true) @JsonProperty("orderNumber") final int orderNumber,
						 @Input(required = true) @JsonProperty("productCode") final String productCode,
						 @Input(editable = true, pattern = "%d") @JsonProperty("quantity") final Integer quantity,
						 @Select(provider = SizeOptions.class /*, type = SuggestType.EXTERNAL*/) @JsonProperty("size") final String size,
						 @Select(provider = RemoteOptions.class) @JsonProperty("country") final Country country) {
			this.orderNumber = orderNumber;
			this.productCode = productCode;
			this.quantity = quantity;
			this.size = size;
			this.country = country;
		}
	}

	@Value
	@EqualsAndHashCode(callSuper = false)
	static class Order extends ResourceSupport {

		int orderNumber = 42;
		int itemCount = 3;
		String status = "pending";
		Resource<Customer> customer = new Resource<Customer>(new Customer());

		public Order() {
			customer.add(linkTo(methodOn(DummyCustomersController.class).getCustomer("pj123")).withSelfRel());
		}
	}

	@Value
	public static class OrderFilter {

		String status;
		Integer count;

		@JsonCreator
		public OrderFilter(@Input @JsonProperty("count") Integer count, @Input @JsonProperty("status") String status) {

			this.status = status;
			this.count = count;
		}

		public OrderFilter() {
			this(null, null);
		}
	}

	@RequestMapping("/orders")
	static class DummyOrderController {

		@RequestMapping("/{orderNumber}")
		public ResponseEntity<Resource<Order>> getOrder(@PathVariable final int orderNumber) {
			return null;
		}

		@RequestMapping("/{orderNumber}/items")
		public ResponseEntity<Resource<OrderItem>> getOrderItems(@PathVariable final int orderNumber) {
			return null;
		}

		@RequestMapping(value = "/{orderNumber}/items", method = RequestMethod.GET, params = "rel",
			consumes = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<Void> addOrderItemsPrepareForm(@PathVariable final int orderNumber,
															 @RequestParam final String rel) {
			return null;
		}

		@RequestMapping(value = "/{orderNumber}/items", method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<Void> addOrderItems(@PathVariable final int orderNumber,
												  @RequestBody final OrderItem orderItem) {
			return null;
		}

		@RequestMapping
		public ResponseEntity<Resources<Order>> getOrders(@RequestParam List<String> attr) {
			return null;
		}

		@RequestMapping("/filtered")
		public ResponseEntity<Resources<Order>> getOrdersFiltered(@DTOParam OrderFilter filter) {
			return null;
		}

		@RequestMapping("/filteredWithRP")
		public ResponseEntity<Resources<Order>> getOrdersFilteredWithRequestParam(@RequestParam OrderFilter filter) {
			return null;
		}
	}

	@Configuration
	@EnableWebMvc
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class WebConfig extends WebMvcConfigurerAdapter {

		@Bean
		public DummyOrderController orderController() {
			return new DummyOrderController();
		}

		@Bean
		public DummyCustomersController customersController() {
			return new DummyCustomersController();
		}
	}


}
