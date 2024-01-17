/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.server.mvc;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.NonComposite;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.hateoas.TestUtils;
import org.springframework.hateoas.server.core.MethodParameters;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilderUnitTest.Sample.SampleConverter;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

/**
 * Unit tests for {@link ControllerLinkBuilder}.
 *
 * @author Oliver Gierke
 * @author Dietrich Schulten
 * @author Kamill Sokol
 * @author Oemer Yildiz
 * @author Greg Turnquist
 * @author Kevin Conaway
 * @author Oliver Trosien
 * @author Greg Turnquist
 * @author Réda Housni Alaoui
 */
class WebMvcLinkBuilderUnitTest extends TestUtils {

	@Test
	void createsLinkToControllerRoot() {

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people");
	}

	@Test
	void createsLinkToParameterizedControllerRoot() {

		Link link = linkTo(PersonsAddressesController.class, 15).withSelfRel();

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people/15/addresses");
	}

	/**
	 * @see #70
	 */
	@Test
	void createsLinkToMethodOnParameterizedControllerRoot() {

		Link link = linkTo(methodOn(PersonsAddressesController.class, 15).getAddressesForCountry("DE")).withSelfRel();
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people/15/addresses/DE");
	}

	@Test
	void createsLinkToSubResource() {

		Link link = linkTo(PersonControllerImpl.class).slash("something").withSelfRel();
		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/people/something");
	}

	@Test
	void createsLinkWithCustomRel() {

		Link link = linkTo(PersonControllerImpl.class).withRel(IanaLinkRelations.NEXT);

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.NEXT);
		assertThat(link.getHref()).endsWith("/people");
	}

	/**
	 * @see #186
	 */
	@Test
	void usesFirstMappingInCaseMultipleOnesAreDefined() {
		assertThat(linkTo(InvalidController.class).withSelfRel().getHref()).endsWith("/persons");
	}

	@Test
	void createsLinkToUnmappedController() {

		Link link = linkTo(UnmappedController.class).withSelfRel();
		assertThat(link.getHref()).isEqualTo("http://localhost");
	}

	@Test
	void appendingNullIsANoOp() {

		Link link = linkTo(PersonControllerImpl.class).slash(null).withSelfRel();
		assertThat(link.getHref()).endsWith("/people");

		link = linkTo(PersonControllerImpl.class).slash((Object) null).withSelfRel();
		assertThat(link.getHref()).endsWith("/people");
	}

	@Test
	void linksToMethod() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).myMethod(null)).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref()).endsWith("/something/else");
	}

	@Test
	void linksToMethodWithPathVariable() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("1")).withSelfRel();
		assertPointsToMockServer(link);
		assertThat(link.getHref()).endsWith("/something/1/foo");
	}

	/**
	 * @see #33
	 */
	@Test
	void usesForwardedHostAsHostIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Host", "somethingDifferent");

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("http://somethingDifferent");
	}

	/**
	 * @see #112
	 */
	@Test
	void usesForwardedSslIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Ssl", "on");

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("https://");
	}

	/**
	 * @see #112
	 */
	@Test
	void usesForwardedSslIfHeaderIsSetOff() {

		request.addHeader("X-Forwarded-Ssl", "off");

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("http://");
	}

	/**
	 * @see #112
	 */
	@Test
	void usesForwardedSslAndHostIfHeaderIsSet() {

		request.addHeader("X-Forwarded-Host", "somethingDifferent");
		request.addHeader("X-Forwarded-Ssl", "on");

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("https://somethingDifferent");
	}

	/**
	 * @see #26, #39
	 */
	@Test
	void addsRequestParametersHandedIntoSlashCorrectly() {

		Link link = linkTo(PersonController.class).slash("?foo=bar").withSelfRel();

		UriComponents components = toComponents(link);
		assertThat(components.getQuery()).isEqualTo("foo=bar");
	}

	/**
	 * @see #26, #39
	 */
	@Test
	void linksToMethodWithPathVariableAndRequestParams() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForNextPage("1", 10, 5)).withSelfRel();

		UriComponents components = toComponents(link);
		assertThat(components.getPath()).isEqualTo("/something/1/foo");

		MultiValueMap<String, String> queryParams = components.getQueryParams();
		assertThat(queryParams.get("limit")).containsExactly("5");
		assertThat(queryParams.get("offset")).containsExactly("10");
	}

	/**
	 * @see #26, #39
	 */
	@Test
	void linksToMethodWithPathVariableAndMultiValueRequestParams() {

		Link link = linkTo(
				methodOn(ControllerWithMethods.class).methodWithMultiValueRequestParams("1", Arrays.asList(3, 7), 5))
						.withSelfRel();

		UriComponents components = toComponents(link);
		assertThat(components.getPath()).isEqualTo("/something/1/foo");

		MultiValueMap<String, String> queryParams = components.getQueryParams();
		assertThat(queryParams.get("limit")).containsExactly("5");
		assertThat(queryParams.get("items")).containsExactlyInAnyOrder("3", "7");
	}

	/**
	 * @see #26, #39
	 */
	@Test
	void returnsUriComponentsBuilder() {

		UriComponents components = linkTo(PersonController.class).slash("something?foo=bar").toUriComponentsBuilder()
				.build();

		assertThat(components.getPath()).isEqualTo("/people/something");
		assertThat(components.getQuery()).isEqualTo("foo=bar");
	}

	/**
	 * @see #90
	 */
	@Test
	void usesForwardedHostAndPortFromHeader() {

		request.addHeader("X-Forwarded-Host", "foobar:8088");

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("http://foobar:8088");
	}

	/**
	 * @see #90
	 */
	@Test
	void usesFirstHostOfXForwardedHost() {

		request.addHeader("X-Forwarded-Host", "barfoo:8888, localhost:8088");

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("http://barfoo:8888");
	}

	/**
	 * @see #122, #169
	 */
	@Test
	void appendsOptionalParameterVariableForUnsetParameter() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForOptionalNextPage(null)).withSelfRel();

		assertThat(link.getVariables()).containsExactly(new TemplateVariable("offset", VariableType.REQUEST_PARAM));
		assertThat(link.expand().getHref()).endsWith("/foo");
	}

	/**
	 * @see #170
	 */
	@Test
	void usesForwardedPortFromHeader() {

		request.addHeader("X-Forwarded-Host", "foobarhost");
		request.addHeader("X-Forwarded-Port", "9090");
		request.setServerPort(8080);

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();

		assertThat(link.getHref()).startsWith("http://foobarhost:9090/");
	}

	/**
	 * @see #170
	 */
	@Test
	void usesForwardedHostFromHeaderWithDefaultPort() {

		request.addHeader("X-Forwarded-Host", "foobarhost");
		request.setServerPort(8080);

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("http://foobarhost/");
	}

	/**
	 * @see #114
	 */
	@Test
	void discoversParentClassTypeMappingForInvocation() {

		Link link = linkTo(methodOn(ChildController.class).myMethod()).withSelfRel();
		assertThat(link.getHref()).endsWith("/parent/child");
	}

	/**
	 * @see #114
	 */
	@Test
	void includesTypeMappingFromChildClass() {

		Link link = linkTo(methodOn(ChildWithTypeMapping.class).myMethod()).withSelfRel();
		assertThat(link.getHref()).endsWith("/child/parent");
	}

	/**
	 * @see #96
	 */
	@Test
	void linksToMethodWithPathVariableContainingBlank() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable("with blank")).withSelfRel();

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/something/with%20blank/foo");
	}

	/**
	 * @see #192
	 */
	@Test
	void usesRootMappingOfTargetClassForMethodsOfParentClass() {

		Link link = linkTo(methodOn(ChildControllerWithRootMapping.class) //
				.someEmptyMappedMethod()) //
						.withSelfRel();

		assertThat(link.getHref()).endsWith("/root");
	}

	/**
	 * @see #192
	 */
	@Test
	void usesRootMappingOfTargetClassForMethodsOfParent() throws Exception {

		Method method = ParentControllerWithoutRootMapping.class.getMethod("someEmptyMappedMethod");

		Link link = linkTo(ChildControllerWithRootMapping.class, method).withSelfRel();
		assertThat(link.getHref()).endsWith("/root");
	}

	/**
	 * @see #257, #107
	 */
	@Test
	void usesXForwardedProtoHeaderAsLinkSchema() {

		for (String proto : Arrays.asList("http", "https")) {

			setUp();
			request.addHeader("X-Forwarded-Proto", proto);

			adaptRequestFromForwardedHeaders();

			Link link = linkTo(PersonControllerImpl.class).withSelfRel();
			assertThat(link.getHref()).startsWith(proto + "://");
		}
	}

	/**
	 * @see #257, #107
	 */
	@Test
	void usesProtoValueFromForwardedHeaderAsLinkSchema() {

		for (String proto : Arrays.asList("http", "https")) {

			setUp();
			request.addHeader("Forwarded", new String[] { "proto=" + proto });

			adaptRequestFromForwardedHeaders();

			Link link = linkTo(PersonControllerImpl.class).withSelfRel();
			assertThat(link.getHref()).startsWith(proto.concat("://"));
		}
	}

	/**
	 * @see #257, #107
	 */
	@Test
	void favorsStandardForwardHeaderOverXForwardedProto() {

		request.addHeader("X-Forwarded-Proto", "foo");
		request.addHeader("Forwarded", "proto=bar");

		adaptRequestFromForwardedHeaders();

		Link link = linkTo(PersonControllerImpl.class).withSelfRel();
		assertThat(link.getHref()).startsWith("bar://");
	}

	/**
	 * @see #331, #545
	 */
	@Test
	void linksToMethodWithRequestParamImplicitlySetToFalse() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForOptionalSizeWithDefaultValue(null)).withSelfRel();

		assertThat(link.getHref()).endsWith("/bar{?size}");
	}

	/**
	 * @see #398
	 */
	@Test
	void encodesRequestParameterWithSpecialValue() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithRequestParam("Spring#\n")).withSelfRel();

		assertThat(link.getRel()).isEqualTo(IanaLinkRelations.SELF);
		assertThat(link.getHref()).endsWith("/something/foo?id=Spring%23%0A");
	}

	/**
	 * @see #169
	 */
	@Test
	void createsPartiallyExpandedLink() {

		Link link = linkTo(methodOn(PersonsAddressesController.class, "some id").getAddressesForCountry(null))
				.withSelfRel();

		assertThat(link.isTemplated()).isTrue();
		assertThat(link.getHref()).contains("some%20id");
	}

	/**
	 * @see #169
	 */
	@Test
	void addsOptionalRequestParameterTemplateForMissingValue() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForNextPage("1", null, 5)).withSelfRel();

		assertThat(link.getVariables())
				.containsExactly(new TemplateVariable("offset", VariableType.REQUEST_PARAM_CONTINUED));

		UriComponents components = toComponents(link);

		assertThat(components.getQueryParams().get("query")).isNull();
	}

	/**
	 * @see #509
	 */
	@Test
	void supportsTwoProxiesAddingXForwardedPort() {

		request.addHeader("X-Forwarded-Port", "1443,8443");
		request.addHeader("X-Forwarded-Host", "proxy1,proxy2");

		adaptRequestFromForwardedHeaders();

		assertThat(linkTo(PersonControllerImpl.class).withSelfRel().getHref()).startsWith("http://proxy1:1443");
	}

	/**
	 * @see #509
	 */
	@Test
	void resolvesAmbiguousXForwardedHeaders() {

		request.addHeader("X-Forwarded-Proto", "http");
		request.addHeader("X-Forwarded-Ssl", "on");

		assertThat(linkTo(PersonControllerImpl.class).withSelfRel().getHref()).startsWith("http://");
	}

	/**
	 * @see #527
	 */
	@Test
	void createsLinkRelativeToContextRoot() {

		request.setContextPath("/ctx");
		request.setServletPath("/foo");
		request.setRequestURI("/ctx/foo");

		assertThat(linkTo(PersonControllerImpl.class).withSelfRel().getHref()).endsWith("/ctx/people");
	}

	/**
	 * @see #639
	 */
	@Test
	void considersEmptyOptionalMethodParameterOptional() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithJdk8Optional(Optional.empty())).withSelfRel();

		assertThat(link.isTemplated()).isTrue();
		assertThat(link.getVariableNames()).containsExactly("value");
	}

	/**
	 * @see #639
	 */
	@Test
	void considersOptionalWithValueMethodParameterOptional() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithJdk8Optional(Optional.of(1))).withSelfRel();

		assertThat(link.isTemplated()).isFalse();
		assertThat(link.getHref()).endsWith("?value=1");
	}

	/**
	 * @see #617
	 */
	@Test
	void alternativePathVariableParameter() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithAlternatePathVariable("bar")).withSelfRel();
		assertThat(link.getHref()).isEqualTo("http://localhost/something/bar/foo");
	}

	/**
	 * @see #1003, #122, #169
	 */
	@Test
	void appendsOptionalParameterIfSet() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForOptionalNextPage(1)).withSelfRel();

		assertThat(link.getVariables()).isEmpty();
		assertThat(link.expand().getHref()).endsWith("/foo?offset=1");
	}

	@Test // #1189
	void parentInterfaceCanHoldSpringWebAnnotations() {

		Link link = linkTo(methodOn(WebMvcClass.class).root("short")).withSelfRel();

		assertThat(link.getHref()).endsWith("/api?view=short");
	}

	@Test // #118
	void usesFallbackConversionServiceIfNoContextIsCurrentlyPresent() {

		RequestContextHolder.setRequestAttributes(null);

		linkTo(methodOn(ControllerWithHandlerMethodParameterThatNeedsConversion.class).method(41L)).withSelfRel();
	}

	@Test // #1548
	void mapsRequestParamMap() {

		Object original = ReflectionTestUtils.getField(MethodParameters.class, "DISCOVERER");

		try {

			ReflectionTestUtils.setField(MethodParameters.class, "DISCOVERER", null);

			Stream.of(null, new HashMap<String, String>()).forEach(it -> {

				Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithMapRequestParam(it)).withSelfRel();

				assertThat(link.getHref()).endsWith("/with-map");
			});

		} finally {
			ReflectionTestUtils.setField(MethodParameters.class, "DISCOVERER", original);
		}
	}

	@Test // #1588, #1589
	void buildsLinkFromMethodAndParameters() throws Exception {

		Method method = ControllerWithMethods.class.getDeclaredMethod("methodWithRequestParam", String.class);

		assertThat(linkTo(method, "someString").withSelfRel().getHref()).endsWith("?id=someString");
		assertThat(linkTo(method, new Object[] { null }).withSelfRel().getHref()).endsWith("?id={id}");
	}

	@Test // #1575
	void buildsNonCompositeRequestParamUri() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).nonCompositeRequestParam(Arrays.asList("first", "second")))
				.withSelfRel();

		assertThat(link.getHref()).endsWith("?foo=first,second");
	}

	@Test // #1485
	void encodesDatesCorrectly() {

		OffsetDateTime reference = OffsetDateTime.now(ZoneId.of("CET"));
		Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithOffsetDateTime(reference)).withSelfRel();

		assertThat(UriComponentsBuilder.fromUriString(link.getHref()).build().getQuery())
				.contains("%3A", "%2B")
				.doesNotContain(":", "+");
	}

	@Test // #1598
	void usesRegisteredConverterForCollectionValues() {

		ConfigurableConversionService conversionService = //
				(ConfigurableConversionService) ReflectionTestUtils.getField(WebMvcLinkBuilderFactory.class,
						"FALLBACK_CONVERSION_SERVICE");

		conversionService.addConverter(SampleConverter.INSTANCE);

		Link result = linkTo(
				methodOn(ControllerWithMethods.class).methodWithCustomEnum(Collections.singletonList(Sample.ENUM)))
						.withSelfRel();

		assertThat(result.getHref()).endsWith("?param=first");
	}

	@Test // #1652
	void buildsLinkWithoutParameterValuesGiven() throws Exception {

		Method method = ControllerWithMethods.class.getDeclaredMethod("myMethod", Object.class);

		Stream.<ThrowingCallable> of( //
				() -> linkTo(method, new Object[0]), //
				() -> linkTo(ControllerWithMethods.class, method, new Object[0]) //
		) //
				.map(assertThatIllegalArgumentException()::isThrownBy)
				.forEach(it -> it.withMessageContaining("Expected 1, got 0"));

		Stream.<ThrowingCallable> of( //
				() -> linkTo(method), //
				() -> linkTo(ControllerWithMethods.class, method) //
		).forEach(assertThatNoException()::isThrownBy);
	}

	@Test // #1729
	@SuppressWarnings("null")
	void linksPointingToTheSameMethodAreEqual() {

		Link first = linkTo(methodOn(ControllerWithMethods.class).methodWithRequestBody(null)).withSelfRel();
		Link second = linkTo(methodOn(ControllerWithMethods.class).methodWithRequestBody(null)).withSelfRel();

		assertThat(first).isEqualTo(second);
		assertThat(second).isEqualTo(first);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
		assertThat(second.hashCode()).isEqualTo(first.hashCode());
	}

	@Test // #1776
	void ignoresRequestParamMultipartFile() {

		Stream.of(null, mock(MultipartFile.class)).forEach(it -> {

			Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithMultipartFile(it)).withSelfRel();

			assertThat(link.getVariables()).isEmpty();
			assertThat(link.expand().getHref()).endsWith("/multipart-file");
		});
	}

	@Test // #1722
	void toUriDoesNotDoubleEncodeRequestParameters() {

		assertThat(linkTo(methodOn(MyController.class).test("I+will:be+double+encoded")).toUri().toString())
				.endsWith(UriUtils.encode("I+will:be+double+encoded", Charset.defaultCharset()));
	}

	@TestFactory // #1793
	Stream<DynamicTest> bindsCatchAllPathVariableCorrectly() {

		Stream<Named<String[]>> tests = Stream.of(//
				Named.of("Appends single", new String[] { "second", "/second" }),
				Named.of("Appends multiple", new String[] { "second/second", "/second/second" }),
				Named.of("Appends empty", new String[] { "", "/" }),
				Named.of("Appends null", new String[] { null, "/first{/second*}" }));

		return DynamicTest.stream(tests, it -> {

			assertThat(
					linkTo(methodOn(ControllerWithPathVariableCatchAll.class).test("first", it[0])).withSelfRel().getHref())
							.endsWith(it[1]);
		});
	}

	@Test // #1886
	void doesNotAppendTrailingSlashForEmptyMapping() {

		var controller = methodOn(PersonController.class);

		assertThat(linkTo(controller.getMappingWithoutPath()).toString())
				.isEqualTo("http://localhost/people");

		assertThat(linkTo(controller.getMappingWithEmptyPath()).toString())
				.isEqualTo("http://localhost/people");
	}

	private static UriComponents toComponents(Link link) {
		return UriComponentsBuilder.fromUriString(link.expand().getHref()).build();
	}

	static class Person {
		Long id;
	}

	@RequestMapping("/people")
	interface PersonController {

		@GetMapping
		default Object getMappingWithoutPath() {
			return null;
		}

		@GetMapping("")
		default Object getMappingWithEmptyPath() {
			return null;
		}
	}

	class PersonControllerImpl implements PersonController {}

	@RequestMapping("/people/{id}/addresses")
	static class PersonsAddressesController {

		@RequestMapping("/{country}")
		public HttpEntity<Void> getAddressesForCountry(@PathVariable String country) {
			return null;
		}
	}

	@RequestMapping({ "/persons", "/people" })
	class InvalidController {

	}

	class UnmappedController {

	}

	@RequestMapping("/something")
	static class ControllerWithMethods {

		@RequestMapping("/else")
		HttpEntity<Void> myMethod(@RequestBody Object payload) {
			return null;
		}

		@RequestMapping("/{id}/foo")
		HttpEntity<Void> methodWithPathVariable(@PathVariable String id) {
			return null;
		}

		@RequestMapping("/foo")
		HttpEntity<Void> methodWithRequestParam(@RequestParam String id) {
			return null;
		}

		@RequestMapping(path = "/foo", params = { "a=1", "b=2", "c!=4", "!d" })
		HttpEntity<Void> methodWithPrimaryParams() {
			return null;
		}

		@RequestMapping(value = "/{id}/foo")
		HttpEntity<Void> methodForNextPage(@PathVariable String id, @RequestParam(required = false) Integer offset,
				@RequestParam Integer limit) {
			return null;
		}

		@RequestMapping(value = "/{id}/foo")
		HttpEntity<Void> methodWithMultiValueRequestParams(@PathVariable String id, @RequestParam List<Integer> items,
				@RequestParam Integer limit) {
			return null;
		}

		@RequestMapping(value = "/{id}/foo")
		HttpEntity<Void> methodWithAlternatePathVariable(@PathVariable(name = "id") String otherId) {
			return null;
		}

		@RequestMapping(value = "/foo")
		HttpEntity<Void> methodForOptionalNextPage(@RequestParam(required = false) Integer offset) {
			return null;
		}

		@RequestMapping(value = "/bar")
		HttpEntity<Void> methodForOptionalSizeWithDefaultValue(@RequestParam(defaultValue = "10") Integer size) {
			return null;
		}

		@RequestMapping
		HttpEntity<Void> methodWithJdk8Optional(@RequestParam Optional<Integer> value) {
			return null;
		}

		@RequestMapping(path = "/with-map") // #1548
		HttpEntity<Void> methodWithMapRequestParam(@RequestParam Map<String, String> params) {
			return null;
		}

		@RequestMapping("/non-composite")
		HttpEntity<Void> nonCompositeRequestParam(@NonComposite @RequestParam("foo") Collection<String> params) {
			return null;
		}

		@RequestMapping("/offset")
		HttpEntity<Void> methodWithOffsetDateTime(@RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime date) {
			return null;
		}

		@RequestMapping("/custom-enum")
		HttpEntity<Void> methodWithCustomEnum(@RequestParam List<Sample> param) {
			return null;
		}

		// #1729
		@RequestMapping(method = RequestMethod.POST, path = "/with-request-body")
		HttpEntity<Void> methodWithRequestBody(@RequestBody Person param) {
			return null;
		}

		@RequestMapping("/multipart-file")
		HttpEntity<Void> methodWithMultipartFile(@RequestParam("file") MultipartFile file) {
			return null;
		}
	}

	@RequestMapping("/parent")
	interface ParentController {}

	interface ChildController extends ParentController {

		@RequestMapping("/child")
		Object myMethod();
	}

	interface ParentWithMethod {

		@RequestMapping("/parent")
		Object myMethod();
	}

	@RequestMapping("/child")
	interface ChildWithTypeMapping extends ParentWithMethod {}

	interface ParentControllerWithoutRootMapping {

		@RequestMapping
		Object someEmptyMappedMethod();
	}

	@RequestMapping("/root")
	interface ChildControllerWithRootMapping extends ParentControllerWithoutRootMapping {

	}

	// #1189

	interface WebMvcInterface {

		@GetMapping("/api")
		HttpEntity<?> root(@RequestParam String view);
	}

	@RestController
	static class WebMvcClass implements WebMvcInterface {

		@Override
		public HttpEntity<?> root(String view) {
			return ResponseEntity.noContent().build();
		}
	}

	// #118
	interface ControllerWithHandlerMethodParameterThatNeedsConversion {

		@GetMapping("/{id}")
		HttpEntity<?> method(@PathVariable Long id);
	}

	// #1598
	enum Sample {

		ENUM("first");

		String label;

		/**
		 * @param label
		 */
		private Sample(String label) {
			this.label = label;
		}

		enum SampleConverter implements Converter<Sample, String> {

			INSTANCE;

			@NonNull
			@Override
			public String convert(Sample source) {
				return source.label;
			}
		}
	}

	// #1722
	static class MyController {

		@RequestMapping("/someTestMapping")
		HttpEntity<?> test(@RequestParam("param") String param) {
			return null;
		}
	}

	// #1793
	static class ControllerWithPathVariableCatchAll {

		@RequestMapping("/{first}/{*second}")
		HttpEntity<?> test(@PathVariable String first, @PathVariable String second) {
			return null;
		}
	}
}
