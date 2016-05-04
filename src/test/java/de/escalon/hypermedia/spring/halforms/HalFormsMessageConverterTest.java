package de.escalon.hypermedia.spring.halforms;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static de.escalon.hypermedia.spring.AffordanceBuilder.linkTo;
import static de.escalon.hypermedia.spring.AffordanceBuilder.methodOn;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.RelProvider;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.core.DefaultRelProvider;
import org.springframework.hateoas.core.Relation;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.escalon.hypermedia.action.Input;
import de.escalon.hypermedia.spring.halforms.Jackson2HalFormsModule.HalFormsHandlerInstantiator;
import de.escalon.hypermedia.spring.siren.SirenMessageConverterTest;
import de.escalon.hypermedia.spring.siren.SirenUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(loader = AnnotationConfigWebContextLoader.class)
public class HalFormsMessageConverterTest {

	public static final Logger LOG = LoggerFactory.getLogger(SirenMessageConverterTest.class);
	private ObjectMapper objectMapper = new ObjectMapper();
	private RelProvider relProvider = new DefaultRelProvider();

	private CurieProvider curieProvider = new DefaultCurieProvider("test",
			new UriTemplate("http://localhost:8080/profile/{rel}"));

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
		public OrderItem(@Input(required = true) @JsonProperty("orderNumber") int orderNumber,
				@Input(required = true) @JsonProperty("productCode") String productCode,
				@Input(editable = true, pattern = "%d") @JsonProperty("quantity") Integer quantity) {
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

		private final Resource<Customer> customer = new Resource<Customer>(new Customer());

		public Order() {
			customer.add(linkTo(methodOn(DummyCustomersController.class).getCustomer("pj123")).withSelfRel());
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

		@RequestMapping(value = "/{orderNumber}/items", method = RequestMethod.POST,
				consumes = MediaType.APPLICATION_JSON_VALUE)
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
	class WebConfig extends WebMvcConfigurerAdapter {

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

			converters.add(new HalFormsMessageConverter(objectMapper, relProvider, curieProvider, null));
		}

		@Override
		public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
			final ExceptionHandlerExceptionResolver resolver = new ExceptionHandlerExceptionResolver();
			resolver.setWarnLogCategory(resolver.getClass().getName());
			exceptionResolvers.add(resolver);
		}

	}

	@Autowired private WebApplicationContext wac;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		this.mockMvc = webAppContextSetup(this.wac).build();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		objectMapper.registerModule(new Jackson2HalModule());
		objectMapper.registerModule(new Jackson2HalFormsModule());
		objectMapper.setHandlerInstantiator(new HalFormsHandlerInstantiator(relProvider, curieProvider, null, true));

	}

	@Test
	public void testTemplates() throws JsonProcessingException {

		Order order = new Order();
		order.add(linkTo(methodOn(DummyOrderController.class).addOrderItems(42, new OrderItem(42, null, null)))
				.withRel("order-items"));
				// order.add(linkTo(methodOn(DummyOrderController.class).getOrder(42)).withSelfRel());
				// order.add(linkTo(methodOn(DummyOrderController.class).getOrder(43)).withRel("next"));
				// order.add(linkTo(methodOn(DummyOrderController.class).getOrder(41)).withRel("previous"));
				// order.add(linkTo(methodOn(DummyOrderController.class).getOrders(null)).withRel("orders"));

		// no support for non-query links
		// order.add(new Link("http://example.com/{foo}", "foo"));
		// order.add(new Link("http://example.com{?bar}", "bar"));

		Object entity = HalFormsUtils.toHalFormsDocument(order);
		String json = objectMapper.valueToTree(entity).toString();

		assertThat(json, hasJsonPath("$._templates"));
		assertThat(json, hasJsonPath("$._templates.default"));
		assertThat(json, hasJsonPath("$._templates.default.method", equalTo("POST")));
		assertThat(json, hasJsonPath("$._templates.default.contentType", equalTo("application/json")));
		assertThat(json, hasJsonPath("$._templates.default.properties", hasSize(3)));
	}
}
