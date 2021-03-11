/*
 * Copyright 2012-2021 the original author or authors.
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
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariable.VariableType;
import org.springframework.hateoas.TestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

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
	 * @see #122, #169
	 */
	@Test
	void rejectsMissingPathVariable() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			linkTo(methodOn(ControllerWithMethods.class).methodWithPathVariable(null))//
					.withSelfRel().expand();
		});
	}

	/**
	 * @see #122, #169
	 */
	@Test
	void rejectsMissingRequiredRequestParam() {

		assertThatIllegalArgumentException().isThrownBy(() -> {
			Link link = linkTo(methodOn(ControllerWithMethods.class).methodWithRequestParam(null)).withSelfRel();

			assertThat(link.getVariableNames()).containsExactly("id");

			link.expand();
		});
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
	 * @see #331
	 */
	@Test
	void linksToMethodWithRequestParamImplicitlySetToFalse() {

		Link link = linkTo(methodOn(ControllerWithMethods.class).methodForOptionalSizeWithDefaultValue(null)).withSelfRel();

		assertThat(link.getHref()).endsWith("/bar");
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
	void addsRequestParameterVariablesForMissingRequiredParameter() {

		assertThatIllegalArgumentException().isThrownBy(() -> {

			Link link = linkTo(methodOn(ControllerWithMethods.class).methodForNextPage("1", 10, null)).withSelfRel();

			assertThat(link.getVariableNames()).containsExactly("limit");

			link.expand();
		}).withMessageContaining("limit");
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

	private static UriComponents toComponents(Link link) {
		return UriComponentsBuilder.fromUriString(link.expand().getHref()).build();
	}

	static class Person {
		Long id;
	}

	@RequestMapping("/people")
	interface PersonController {}

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

	// #???
	interface ControllerWithHandlerMethodParameterThatNeedsConversion {

		@GetMapping("/{id}")
		HttpEntity<?> method(@PathVariable Long id);
	}
}
