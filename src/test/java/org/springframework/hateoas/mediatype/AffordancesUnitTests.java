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
package org.springframework.hateoas.mediatype;

import static org.assertj.core.api.Assertions.*;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.AffordanceModel.PayloadMetadata;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.support.Employee;
import org.springframework.http.HttpMethod;

/**
 * @author Oliver Drotbohm
 */
public class AffordancesUnitTests {

	@Test
	void affordanceConvenienceMethodChainsExistingLink() {

		Link link = Affordances.of(Link.of("/")) //
				.afford(HttpMethod.POST) //
				.withInputAndOutput(Employee.class) //
				.withName("name") //
				.toLink();

		assertThat(link.getHref()).isEqualTo("/");
		assertThat(link.hasRel(IanaLinkRelations.SELF)).isTrue();
		assertThat(link.getAffordances()).hasSize(1);

		Affordance affordance = link.getAffordances().get(0);

		assertThat(affordance).hasSize(3);

		assertAffordanceModel(affordance, commonAssertions().andThen(it -> {
			assertThat(it.getName()).isEqualTo("name");
		}));
	}

	@Test
	void affordanceConvenienceMethodDefaultsNameBasedOnHttpVerb() {

		Link link = Affordances.of(Link.of("/")) //
				.afford(HttpMethod.POST) //
				.withInputAndOutput(Employee.class) //
				.toLink();

		assertThat(link.getHref()).isEqualTo("/");
		assertThat(link.hasRel(IanaLinkRelations.SELF)).isTrue();
		assertThat(link.getAffordances()).hasSize(1);

		Affordance affordance = link.getAffordances().get(0);

		assertThat(affordance).hasSize(3);

		assertAffordanceModel(affordance, commonAssertions().andThen(it -> {
			assertThat(it.getName()).isEqualTo("postEmployee");
		}));
	}

	private static Consumer<AffordanceModel> commonAssertions() {

		return it -> {

			assertThat(it.getHttpMethod()).isEqualTo(HttpMethod.POST);
			assertThat(it.getQueryMethodParameters()).hasSize(0);

			assertThatPayload(it.getInput()).isBackedBy(Employee.class);
			assertThatPayload(it.getOutput()).isBackedBy(Employee.class);
		};
	}

	private static void assertAffordanceModel(Affordance affordance, Consumer<AffordanceModel> assertions) {

		Stream.of(MediaTypes.COLLECTION_JSON, MediaTypes.HAL_FORMS_JSON, MediaTypes.UBER_JSON) //
				.map(affordance::getAffordanceModel) //
				.map(AffordanceModel.class::cast) //
				.forEach(assertions);
	}

	private static PayloadMetadataAssert assertThatPayload(PayloadMetadata metadata) {
		return new PayloadMetadataAssert(metadata);
	}

	private static class PayloadMetadataAssert extends AbstractAssert<PayloadMetadataAssert, PayloadMetadata> {

		public PayloadMetadataAssert(PayloadMetadata actual) {
			super(actual, PayloadMetadataAssert.class);
		}

		public PayloadMetadataAssert isBackedBy(Class<?> type) {

			Assertions.assertThat(actual).isInstanceOfSatisfying(TypeBasedPayloadMetadata.class, it -> {
				Assertions.assertThat(it.getType()).isEqualTo(ResolvableType.forClass(type));
			});

			return this;
		}
	}
}
