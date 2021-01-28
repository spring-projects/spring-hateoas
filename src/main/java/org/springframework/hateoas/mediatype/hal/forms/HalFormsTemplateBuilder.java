/*
 * Copyright 2019-2021 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

class HalFormsTemplateBuilder {

	private final MessageResolver resolver;
	private final HalFormsPropertyFactory factory;

	public HalFormsTemplateBuilder(HalFormsConfiguration configuration, MessageResolver resolver) {

		this.resolver = resolver;
		this.factory = new HalFormsPropertyFactory(configuration, resolver);
	}

	/**
	 * Extract template details from a {@link RepresentationModel}'s {@link Affordance}s.
	 *
	 * @param resource
	 * @return
	 */
	public Map<String, HalFormsTemplate> findTemplates(RepresentationModel<?> resource) {

		Map<String, HalFormsTemplate> templates = new HashMap<>();
		Link selfLink = resource.getLink(IanaLinkRelations.SELF).orElse(null);

		resource.getLinks().stream() //
				.flatMap(it -> it.getAffordances().stream()) //
				.map(it -> it.getAffordanceModel(MediaTypes.HAL_FORMS_JSON)) //
				.peek(it -> {
					Assert.notNull(it, "No HAL Forms affordance model found but expected!");
				}) //
				.map(HalFormsAffordanceModel.class::cast) //
				.filter(it -> !it.hasHttpMethod(HttpMethod.GET)) //
				.forEach(it -> {

					HalFormsTemplate template = HalFormsTemplate.forMethod(it.getHttpMethod()) //
							.withProperties(factory.createProperties(it))
							.withContentType(it.getInput().getPrimaryMediaType());

					String target = it.getLink().getHref();

					if (selfLink == null || !target.equals(selfLink.getHref())) {
						template = template.withTarget(target);
					}

					template = applyTo(template, TemplateTitle.of(it, templates.isEmpty()));
					templates.put(templates.isEmpty() ? "default" : it.getName(), template);
				});

		return templates;
	}

	private HalFormsTemplate applyTo(HalFormsTemplate template, HalFormsTemplateBuilder.TemplateTitle templateTitle) {

		return Optional.ofNullable(resolver.resolve(templateTitle)) //
				.filter(StringUtils::hasText) //
				.map(template::withTitle) //
				.orElse(template);
	}

	private static class TemplateTitle implements MessageSourceResolvable {

		private static final String TEMPLATE_TEMPLATE = "_templates.%s.title";

		private final HalFormsAffordanceModel affordance;
		private final boolean soleTemplate;

		private TemplateTitle(HalFormsAffordanceModel affordance, boolean soleTemplate) {

			this.affordance = affordance;
			this.soleTemplate = soleTemplate;
		}

		public static TemplateTitle of(HalFormsAffordanceModel affordance, boolean soleTemplate) {
			return new TemplateTitle(affordance, soleTemplate);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.context.MessageSourceResolvable#getCodes()
		 */
		@NonNull
		@Override
		public String[] getCodes() {

			Stream<String> seed = Stream.concat(//
					Stream.of(affordance.getName()), //
					soleTemplate ? Stream.of("default") : Stream.empty());

			return seed.flatMap(it -> getCodesFor(it, affordance.getInput())) //
					.toArray(String[]::new);
		}

		private static Stream<String> getCodesFor(String name, InputPayloadMetadata type) {

			String global = String.format(TEMPLATE_TEMPLATE, name);

			Stream<String> inputBased = type.getI18nCodes().stream() //
					.map(it -> String.format("%s.%s", it, global));

			return Stream.concat(inputBased, Stream.of(global));
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.context.MessageSourceResolvable#getDefaultMessage()
		 */
		@Nullable
		@Override
		public String getDefaultMessage() {
			return "";
		}
	}
}
