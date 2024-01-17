/*
 * Copyright 2019-2024 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import static org.assertj.core.api.Assertions.*;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.hateoas.InputType;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * @author Oliver Drotbohm
 */
class HalFormsTemplateBuilderUnitTest {

	@ParameterizedTest(name = "Detects regex pattern ''{1}'' for property ''{0}''")
	@CsvSource({ "number, [0-9]{16}", "overridden, foo", "annotated, bar" })
	void detectsRegularExpressionsOnProperties(String propertyName, String expected) {

		HalFormsConfiguration configuration = new HalFormsConfiguration() //
				.withPattern(CreditCardNumber.class, "[0-9]{16}");

		HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(configuration, MessageResolver.DEFAULTS_ONLY);

		PatternExample resource = new PatternExample();
		resource.add(Affordances.of(Link.of("/examples")) //
				.afford(HttpMethod.POST) //
				.withInput(PatternExample.class) //
				.toLink());

		Map<String, HalFormsTemplate> templates = builder.findTemplates(resource);

		HalFormsTemplate template = templates.get("default");

		assertThat(template).isNotNull();
		assertThat(template.getPropertyByName(propertyName) //
				.map(HalFormsProperty::getRegex)) //
						.hasValue(expected);
	}

	@Test
	void allPropertiesAreOptionalForPatchRequests() throws Exception {

		RequiredProperty model = new RequiredProperty();
		model.add(Affordances.of(Link.of("/example")) //
				.afford(HttpMethod.PATCH) //
				.withInput(RequiredProperty.class) //
				.andAfford(HttpMethod.POST) //
				.withInput(RequiredProperty.class) //
				.withName("post") //
				.toLink());

		HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(new HalFormsConfiguration(),
				MessageResolver.DEFAULTS_ONLY);

		Map<String, HalFormsTemplate> templates = builder.findTemplates(model);

		HalFormsTemplate template = templates.get("default");

		assertThat(template).isNotNull();
		assertThat(template.getPropertyByName("name").map(HalFormsProperty::isRequired)).hasValue(false);

		template = templates.get("post");

		assertThat(template).isNotNull();
		assertThat(template.getPropertyByName("name").map(HalFormsProperty::isRequired)).hasValue(true);
	}

	@Test // #1439
	void considersMinandMaxAnnotations() {

		Link link = Affordances.of(Link.of("/example")) //
				.afford(HttpMethod.POST) //
				.withInput(Payload.class) //
				.toLink();

		HalFormsTemplate template = new HalFormsTemplateBuilder(new HalFormsConfiguration(), //
				MessageResolver.DEFAULTS_ONLY).findTemplates(new RepresentationModel<>().add(link)).get("default");

		Optional<HalFormsProperty> number = template.getPropertyByName("number");
		assertThat(number).map(HalFormsProperty::getMin).hasValue(2L);
		assertThat(number).map(HalFormsProperty::getMax).hasValue(5L);

		Optional<HalFormsProperty> range = template.getPropertyByName("range");
		assertThat(range).map(HalFormsProperty::getMin).hasValue(8L);
		assertThat(range).map(HalFormsProperty::getMax).hasValue(10L);

		Optional<HalFormsProperty> text = template.getPropertyByName("text");
		assertThat(text).map(HalFormsProperty::getMinLength).hasValue(2L);
		assertThat(text).map(HalFormsProperty::getMaxLength).hasValue(5L);
	}

	@Test // #1557
	void considersDecimalMinandDecimalMaxAnnotations() {

		Link link = Affordances.of(Link.of("/example")) //
				.afford(HttpMethod.POST) //
				.withInput(Payload.class) //
				.toLink();

		HalFormsTemplate template = new HalFormsTemplateBuilder(new HalFormsConfiguration(), //
				MessageResolver.DEFAULTS_ONLY).findTemplates(new RepresentationModel<>().add(link)).get("default");

		Optional<HalFormsProperty> decimal = template.getPropertyByName("decimal");
		assertThat(decimal).map(HalFormsProperty::getMin).hasValue(new BigDecimal("2.1"));
		assertThat(decimal).map(HalFormsProperty::getMax).hasValue(new BigDecimal("5.3"));
	}

	@Test // #1427
	void addsTargetAttributeForLinksNotPointingToSelf() {

		Link link = Affordances.of(Link.of("/example", LinkRelation.of("create"))) //
				.afford(HttpMethod.POST) //
				.withInput(Payload.class) //
				.withName("create") //
				.toLink();

		Map<String, HalFormsTemplate> templates = new HalFormsTemplateBuilder(new HalFormsConfiguration(),
				MessageResolver.DEFAULTS_ONLY).findTemplates(new RepresentationModel<>().add(link));

		assertThat(templates.get("default").getTarget()).isEqualTo("/example");
	}

	@Test // #1443
	void exposesInputMediaTypeAsContentType() {

		MediaType mediaType = MediaType.parseMediaType("text/uri-list");

		Link link = Affordances.of(Link.of("/example", LinkRelation.of("create"))) //
				.afford(HttpMethod.POST) //
				.withInput(Payload.class) //
				.withInputMediaType(mediaType) //
				.withName("create") //
				.toLink();

		Map<String, HalFormsTemplate> templates = new HalFormsTemplateBuilder(new HalFormsConfiguration(),
				MessageResolver.DEFAULTS_ONLY).findTemplates(new RepresentationModel<>().add(link));

		assertThat(templates.get("default").getContentType()).isEqualTo(mediaType.toString());
	}

	@Test // #1483
	void rendersInlineOptions() {

		List<String> values = Arrays.asList("1234123412341234", "4321432143214321");

		HalFormsConfiguration configuration = new HalFormsConfiguration() //
				.withOptions(PatternExample.class, "number", metadata -> HalFormsOptions.inline(values));

		RepresentationModel<?> models = new RepresentationModel<>(
				Affordances.of(Link.of("/example", LinkRelation.of("create"))) //
						.afford(HttpMethod.POST) //
						.withInput(PatternExample.class) //
						.toLink());

		Map<String, HalFormsTemplate> templates = new HalFormsTemplateBuilder(configuration, MessageResolver.DEFAULTS_ONLY)
				.findTemplates(models);

		assertThat(templates.get("default").getPropertyByName("number")) //
				.hasValueSatisfying(it -> {
					assertThat(it.getOptions()).isNotNull() //
							.isInstanceOfSatisfying(HalFormsOptions.Inline.class,
									inline -> assertThat(inline.getInline()).isEqualTo(values));
				});
	}

	@Test // #1510
	void propagatesSelectedValueToProperty() {

		String selected = "1234123412341234";
		List<String> values = Arrays.asList(selected, "4321432143214321");

		HalFormsConfiguration configuration = new HalFormsConfiguration() //
				.withOptions(PatternExample.class, "number",
						metadata -> HalFormsOptions.inline(values).withSelectedValue(selected));

		RepresentationModel<?> models = new RepresentationModel<>(
				Affordances.of(Link.of("/example", LinkRelation.of("create"))) //
						.afford(HttpMethod.POST) //
						.withInput(PatternExample.class) //
						.toLink());

		Map<String, HalFormsTemplate> templates = new HalFormsTemplateBuilder(configuration, MessageResolver.DEFAULTS_ONLY)
				.findTemplates(models);

		assertThat(templates.get("default").getPropertyByName("number")).hasValueSatisfying(it -> {
			assertThat(it.getValue()).isEqualTo(selected);
		});
	}

	@Test // #1608
	void exposesHttpMethodInAllCaps() {
		assertThat(HalFormsTemplate.forMethod(HttpMethod.POST).getMethod()).isEqualTo("POST");
	}

	@Test
	void expandsAffordanceLinkForFormTarget() {

		HalFormsConfiguration configuration = new HalFormsConfiguration();

		RepresentationModel<?> models = new RepresentationModel<>(
				Affordances.of(Link.of("/example{?foo}", LinkRelation.of("example"))) //
						.afford(HttpMethod.POST) //
						.toLink());

		Map<String, HalFormsTemplate> templates = new HalFormsTemplateBuilder(configuration, MessageResolver.DEFAULTS_ONLY)
				.findTemplates(models);

		assertThat(templates.get("default").getTarget()).endsWith("/example");
	}

	@Test // #1697
	void exposesCustomInputType() {

		RepresentationModel<?> models = new RepresentationModel<>(
				Affordances.of(Link.of("/example", LinkRelation.of("example"))) //
						.afford(HttpMethod.POST) //
						.withInput(WithCustomInputType.class) //
						.toLink());

		Map<String, HalFormsTemplate> templates = new HalFormsTemplateBuilder(new HalFormsConfiguration(),
				MessageResolver.DEFAULTS_ONLY).findTemplates(models);

		assertThat(templates.get("default").getPropertyByName("property")).hasValueSatisfying(it -> {
			assertThat(it.getType()).isEqualTo("custom");
		});
	}

	@Getter
	static class PatternExample extends RepresentationModel<PatternExample> {

		CreditCardNumber number;

		@Pattern(regexp = "foo") CreditCardNumber overridden;

		WithTypeLevelAnnotation annotated;
	}

	static class CreditCardNumber {}

	@Pattern(regexp = "bar")
	static class WithTypeLevelAnnotation {}

	@Getter
	static class RequiredProperty extends RepresentationModel<RequiredProperty> {
		@NotNull String name;
	}

	@Getter
	static class Payload {

		@Min(2) //
		@Max(5) //
		Integer number;

		@Length(min = 2, max = 5) //
		String text;

		@Range(min = 8, max = 10) //
		Integer range;

		@DecimalMin("2.1") //
		@DecimalMax("5.3") //
		BigDecimal decimal;
	}

	@Getter
	static class WithCustomInputType {

		@InputType("custom") //
		String property;
	}
}
