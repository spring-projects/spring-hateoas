/*
 * Copyright 2019-2020 the original author or authors.
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

import lombok.Getter;

import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpMethod;

/**
 * @author Oliver Drotbohm
 */
public class HalFormsTemplateBuilderUnitTest {

	@ParameterizedTest(name = "Detects regex pattern ''{1}'' for property ''{0}''")
	@CsvSource({ "number, [0-9]{16}", "overridden, foo", "annotated, bar" })
	void detectsRegularExpressionsOnProperties(String propertyName, String expected) {

		HalFormsConfiguration configuration = new HalFormsConfiguration();
		configuration.registerPattern(CreditCardNumber.class, "[0-9]{16}");

		HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(configuration, MessageResolver.DEFAULTS_ONLY);

		PatternExample resource = new PatternExample();
		resource.add(Affordances.of(new Link("/examples")) //
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

		Affordances.of(new Link("/example")) //
				.afford(HttpMethod.PATCH) //
				.withInput(RequiredProperty.class);

		RequiredProperty model = new RequiredProperty();
		model.add(Affordances.of(new Link("/example")) //
				.afford(HttpMethod.PATCH) //
				.withInput(RequiredProperty.class) //
				.andAfford(HttpMethod.POST) //
				.withInput(RequiredProperty.class) //
				.withName("post") //
				.toLink());

		HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(new HalFormsConfiguration(), MessageResolver.DEFAULTS_ONLY);

		Map<String, HalFormsTemplate> templates = builder.findTemplates(model);

		HalFormsTemplate template = templates.get("default");

		assertThat(template).isNotNull();
		assertThat(template.getPropertyByName("name").map(HalFormsProperty::isRequired)).hasValue(false);

		template = templates.get("post");

		assertThat(template).isNotNull();
		assertThat(template.getPropertyByName("name").map(HalFormsProperty::isRequired)).hasValue(true);
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
}
