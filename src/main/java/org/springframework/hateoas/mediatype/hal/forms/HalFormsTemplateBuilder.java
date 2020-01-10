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

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@RequiredArgsConstructor
class HalFormsTemplateBuilder {

	private final HalFormsConfiguration configuration;
	private final MessageResolver resolver;

	/**
	 * Extract template details from a {@link RepresentationModel}'s {@link Affordance}s.
	 *
	 * @param resource
	 * @return
	 */
	public Map<String, HalFormsTemplate> findTemplates(RepresentationModel<?> resource) {

		if (!resource.hasLink(IanaLinkRelations.SELF)) {
			return Collections.emptyMap();
		}

		Map<String, HalFormsTemplate> templates = new HashMap<>();
		List<Affordance> affordances = resource.getLink(IanaLinkRelations.SELF) //
				.map(Link::getAffordances) //
				.orElse(Collections.emptyList());

		affordances.stream() //
				.map(it -> it.getAffordanceModel(MediaTypes.HAL_FORMS_JSON)) //
				.peek(it -> {
					Assert.notNull(it, "No HAL Forms affordance model found but expected!");
				}) //
				.map(HalFormsAffordanceModel.class::cast) //
				.filter(it -> !it.hasHttpMethod(HttpMethod.GET)) //
				.forEach(it -> {

					PropertyCustomizations propertyCustomizations = forMetadata(it.getInput());

					List<HalFormsProperty> propertiesWithPrompt = it.getInputProperties().stream() //
							.map(property -> propertyCustomizations.apply(property)) //
							.map(property -> it.hasHttpMethod(HttpMethod.PATCH) ? property.withRequired(false) : property)
							.collect(Collectors.toList());

					HalFormsTemplate template = HalFormsTemplate.forMethod(it.getHttpMethod()) //
							.withProperties(propertiesWithPrompt);

					template = applyTo(template, TemplateTitle.of(it, templates.isEmpty()));
					templates.put(templates.isEmpty() ? "default" : it.getName(), template);
				});

		return templates;
	}

	public PropertyCustomizations forMetadata(InputPayloadMetadata metadata) {
		return new PropertyCustomizations(metadata);
	}

	public HalFormsTemplate applyTo(HalFormsTemplate template, HalFormsTemplateBuilder.TemplateTitle templateTitle) {

		return Optional.ofNullable(resolver.resolve(templateTitle)) //
				.map(template::withTitle) //
				.orElse(template);
	}

	@RequiredArgsConstructor
	class PropertyCustomizations {

		private final InputPayloadMetadata metadata;

		private HalFormsProperty apply(HalFormsProperty property) {

			String message = resolver.resolve(PropertyPrompt.of(metadata, property));

			HalFormsProperty withPrompt = Optional.ofNullable(message) //
					.map(it -> property.withPrompt(it)) //
					.orElse(property);

			HalFormsProperty withConfig = metadata.getPropertyMetadata(withPrompt.getName()) //
					.flatMap(it -> applyConfig(it, withPrompt)) //
					.orElse(withPrompt);

			return metadata.applyTo(withConfig);
		}

		private Optional<HalFormsProperty> applyConfig(PropertyMetadata metadata, HalFormsProperty property) {
			return configuration.getTypePatternFor(metadata.getType()).map(property::withRegex);
		}
	}

	@RequiredArgsConstructor(staticName = "of")
	static class TemplateTitle implements MessageSourceResolvable {

		private static final String TEMPLATE_TEMPLATE = "_templates.%s.title";

		private final HalFormsAffordanceModel affordance;
		private final boolean soleTemplate;

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

	@RequiredArgsConstructor(staticName = "of")
	static class PropertyPrompt implements MessageSourceResolvable {

		private static final String PROMPT_TEMPLATE = "%s._prompt";

		private final InputPayloadMetadata metadata;
		private final HalFormsProperty property;

		/*
		 * (non-Javadoc)
		 * @see org.springframework.context.MessageSourceResolvable#getDefaultMessage()
		 */
		@Nullable
		@Override
		public String getDefaultMessage() {
			return "";
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.context.MessageSourceResolvable#getCodes()
		 */
		@NonNull
		@Override
		public String[] getCodes() {

			String globalCode = String.format(PROMPT_TEMPLATE, property.getName());

			List<String> codes = new ArrayList<>();

			metadata.getI18nCodes().stream() //
					.map(it -> String.format("%s.%s", it, globalCode)) //
					.forEach(codes::add);

			codes.add(globalCode);

			return codes.toArray(new String[0]);
		}
	}
}
