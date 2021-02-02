/*
 * Copyright 2021 the original author or authors.
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

import static org.springframework.http.HttpMethod.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.hateoas.AffordanceModel.InputPayloadMetadata;
import org.springframework.hateoas.AffordanceModel.PropertyMetadata;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.html.HtmlInputType;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Factory to create {@link HalFormsProperty} instances.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 * @soundtrack The Chicks - March March (Gaslighter)
 */
class HalFormsPropertyFactory {

	private static final Set<HttpMethod> ENTITY_ALTERING_METHODS = EnumSet.of(POST, PUT, PATCH);

	private final HalFormsConfiguration configuration;
	private final MessageResolver resolver;

	/**
	 * Creates a new {@link HalFormsPropertyFactory} for the given {@link HalFormsConfiguration} and
	 * {@link MessageResolver}.
	 *
	 * @param configuration must not be {@literal null}.
	 * @param resolver must not be {@literal null}.
	 */
	public HalFormsPropertyFactory(HalFormsConfiguration configuration, MessageResolver resolver) {

		Assert.notNull(configuration, "HalFormsConfiguration must not be null!");
		Assert.notNull(resolver, "MessageResolver must not be null!");

		this.configuration = configuration;
		this.resolver = resolver;
	}

	/**
	 * Creates {@link HalFormsProperty} from the given {@link HalFormsAffordanceModel}.
	 *
	 * @param model must not be {@literal null}.
	 * @return
	 */
	public List<HalFormsProperty> createProperties(HalFormsAffordanceModel model) {

		Assert.notNull(model, "HalFormsModel must not be null!");

		if (!ENTITY_ALTERING_METHODS.contains(model.getHttpMethod())) {
			return Collections.emptyList();
		}

		return model.createProperties((payload, metadata) -> {

			String inputTypeSource = metadata.getInputType();
			HtmlInputType inputType = inputTypeSource == null ? null : HtmlInputType.of(inputTypeSource);

			HalFormsProperty property = new HalFormsProperty()
					.withName(metadata.getName())
					.withRequired(metadata.isRequired()) //
					.withReadOnly(metadata.isReadOnly())
					.withMin(metadata.getMin())
					.withMax(metadata.getMax())
					.withMinLength(metadata.getMinLength())
					.withMaxLength(metadata.getMaxLength())
					.withRegex(lookupRegex(metadata)) //
					.withType(inputType);

			Function<String, I18nedPropertyMetadata> factory = I18nedPropertyMetadata.factory(payload, property);

			return Optional.of(property)
					.map(it -> i18n(it, factory.apply("_placeholder"), it::withPlaceholder))
					.map(it -> i18n(it, factory.apply("_prompt"), it::withPrompt))
					.map(it -> model.hasHttpMethod(HttpMethod.PATCH) ? it.withRequired(false) : it)
					.orElse(property);
		});
	}

	private Optional<String> lookupRegex(PropertyMetadata metadata) {

		Optional<String> pattern = metadata.getPattern();

		if (pattern.isPresent()) {
			return pattern;
		}

		return configuration.getTypePatternFor(metadata.getType());
	}

	private HalFormsProperty i18n(HalFormsProperty property, MessageSourceResolvable metadata,
			Function<String, HalFormsProperty> application) {

		String resolved = resolver.resolve(metadata);

		return !StringUtils.hasText(resolved)
				? property
				: application.apply(resolved);
	}

	private static class I18nedPropertyMetadata implements MessageSourceResolvable {

		private final String template;
		private final InputPayloadMetadata metadata;
		private final HalFormsProperty property;

		private I18nedPropertyMetadata(String template, InputPayloadMetadata metadata, HalFormsProperty property) {

			this.template = template;
			this.metadata = metadata;
			this.property = property;
		}

		public static Function<String, I18nedPropertyMetadata> factory(InputPayloadMetadata metadata,
				HalFormsProperty property) {
			return suffix -> new I18nedPropertyMetadata("%s.".concat(suffix), metadata, property);
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

		/*
		 * (non-Javadoc)
		 * @see org.springframework.context.MessageSourceResolvable#getCodes()
		 */
		@NonNull
		@Override
		public String[] getCodes() {

			String globalCode = String.format(template, property.getName());

			List<String> codes = new ArrayList<>();

			metadata.getI18nCodes().stream() //
					.map(it -> String.format("%s.%s", it, globalCode)) //
					.forEach(codes::add);

			codes.add(globalCode);

			return codes.toArray(new String[0]);
		}
	}
}
