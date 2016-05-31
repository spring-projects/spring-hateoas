package de.escalon.hypermedia.spring.siren;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;
import static de.escalon.hypermedia.spring.AffordanceBuilder.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.Relation;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

/**
 * Created by Dietrich on 18.04.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class)
public class SirenMessageConverterTest {

	public static final Logger LOG = LoggerFactory.getLogger(SirenMessageConverterTest.class);
	private ObjectMapper objectMapper = new ObjectMapper();

	SirenUtils sirenUtils = new SirenUtils();

	@Relation("customer")
	class Customer {

		private final String customerId = "pj123";
		private final String name = "Peter Joseph";

		public String getCustomerId() {
			return customerId;
		}

		public String getName() {
			return name;
		}
	}

	@RequestMapping("/customers")
	static class DummyCustomersController {

		@RequestMapping("/{customerId}")
		public ResponseEntity<Resource<Customer>> getCustomer(@PathVariable String customerId) {
			return null;
		}
	}


	public static class OrderItem {

		private int orderNumber;
		private String productCode;
		private Integer quantity;

		@JsonCreator
		public OrderItem(@JsonProperty("orderNumber") int orderNumber,
						 @JsonProperty("productCode") String productCode,
						 @JsonProperty("quantity") Integer quantity) {
			this.orderNumber = orderNumber;
			this.productCode = productCode;
			this.quantity = quantity;
		}

		public int getOrderNumber() {
			return orderNumber;
		}

		public String getProductCode() {
			return productCode;
		}

		public Integer getQuantity() {
			return quantity;
		}
	}

	class Order extends ResourceSupport {

		private final int orderNumber = 42;
		private final int itemCount = 3;
		private final String status = "pending";

		private final Resource<Customer> customer =
				new Resource<Customer>(new Customer());

		public Order() {
			customer.add(linkTo(methodOn(DummyCustomersController.class)
					.getCustomer("pj123"))
					.withSelfRel());
		}


		public int getOrderNumber() {
			return orderNumber;
		}

		public int getItemCount() {
			return itemCount;
		}

		public String getStatus() {
			return status;
		}

		public Resource<Customer> getCustomer() {
			return customer;
		}
	}


	@RequestMapping("/orders")
	static class DummyOrderController {

		@RequestMapping("/{orderNumber}")
		public ResponseEntity<Resource<Order>> getOrder(@PathVariable int orderNumber) {
			return null;
		}

		@RequestMapping("/{orderNumber}/items")
		public ResponseEntity<Resource<OrderItem>> getOrderItems(@PathVariable int orderNumber) {
			return null;
		}

		@RequestMapping(value = "/{orderNumber}/items", method = RequestMethod.POST)
		public ResponseEntity<Void> addOrderItems(@PathVariable int orderNumber, @RequestBody OrderItem orderItem) {
			return null;
		}

		@RequestMapping
		public ResponseEntity<Resources<Order>> getOrders(@RequestParam List<String> attr) {
			return null;
		}
	}

	@Configuration
	@EnableWebMvc
	static class WebConfig extends WebMvcConfigurerAdapter {


		@Bean
		public DummyOrderController orderController() {
			return new DummyOrderController();
		}

		@Bean
		public DummyCustomersController customersController() {
			return new DummyCustomersController();
		}

		@Override
		public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
			super.configureMessageConverters(converters);
			converters.add(new SirenMessageConverter());
		}

		@Override
		public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
			final ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
			resolver.setWarnLogCategory(resolver.getClass()
					.getName());
			exceptionResolvers.add(resolver);
		}
	}

	@Autowired
	private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = webAppContextSetup(this.wac).build();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	@Test
	public void testActions() throws JsonProcessingException {

		Order order = new Order();
		order.add(linkTo(methodOn(DummyOrderController.class)
				.addOrderItems(42, new OrderItem(42, null, null)))
				.withRel("order-items"));
		order.add(linkTo(methodOn(DummyOrderController.class)
				.getOrder(42))
				.withSelfRel());
		order.add(linkTo(methodOn(DummyOrderController.class)
				.getOrder(43))
				.withRel("next"));
		order.add(linkTo(methodOn(DummyOrderController.class)
				.getOrder(41))
				.withRel("previous"));
		order.add(linkTo(methodOn(DummyOrderController.class)
				.getOrders(null)).withRel("orders"));
		// no support for non-query links
		order.add(new Link("http://example.com/{foo}", "foo"));
		order.add(new Link("http://example.com{?bar}", "bar"));

		SirenEntity entity = new SirenEntity();
		sirenUtils.toSirenEntity(entity, order);
		String json = objectMapper.valueToTree(entity).toString();

		assertThat(json, hasJsonPath("$.actions", hasSize(3)));
		assertThat(json, hasJsonPath("$.actions[0].fields", hasSize(3)));
		assertThat(json, hasJsonPath("$.actions[0].fields[0].name", equalTo("orderNumber")));
		assertThat(json, hasJsonPath("$.actions[0].fields[0].type", equalTo("number")));
		assertThat(json, hasJsonPath("$.actions[0].fields[0].value", equalTo("42")));
		assertThat(json, hasJsonPath("$.actions[0].method", equalTo("POST")));

		// TODO list query parameter: do something smarter
		assertThat(json, hasJsonPath("$.actions[1].fields[0].name", equalTo("attr")));
		assertThat(json, hasJsonPath("$.actions[1].fields[0].type", equalTo("text")));

		assertThat(json, hasJsonPath("$.actions[2].fields[0].name", equalTo("bar")));
		assertThat(json, hasJsonPath("$.actions[2].fields[0].type", equalTo("text")));
	}
}
