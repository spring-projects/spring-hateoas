/*
 * Copyright 2017-2021 the original author or authors.
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
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * {@link AffordanceModel} for a HAL-FORMS {@link MediaType}.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
class HalFormsAffordanceModel extends AffordanceModel {

	private static final Set<HttpMethod> ENTITY_ALTERING_METHODS = EnumSet.of(POST, PUT, PATCH);

	public HalFormsAffordanceModel(String name, Link link, HttpMethod httpMethod, InputPayloadMetadata inputType,
			List<QueryParameter> queryMethodParameters, PayloadMetadata outputType) {
		super(name, link, httpMethod, inputType, queryMethodParameters, outputType);
	}

	/**
	 * Applies the given customizer to all {@link HalFormsProperty} of this model.
	 *
	 * @param customizer must not be {@literal null}.
	 * @return
	 */
	public List<HalFormsProperty> getProperties(HalFormsConfiguration configuration, MessageResolver resolver) {

		if (!ENTITY_ALTERING_METHODS.contains(getHttpMethod())) {
			return Collections.emptyList();
		}

		Function<PropertyMetadata, HalFormsProperty> creator = it -> {

			HalFormsProperty property = new HalFormsProperty().withName(it.getName());

			return configuration.getTypePatternFor(it.getType()) //
					.map(property::withRegex) //
					.orElse(property);
		};

		return getInput().createProperties(creator, (property, metadata) -> {

			return Optional.of(property)
					.map(it -> apply(it, I18nedPlaceholder::of, it::withPlaceholder, resolver))
					.map(it -> apply(it, I18nedPropertyPrompt::of, it::withPrompt, resolver))
					.map(it -> hasHttpMethod(HttpMethod.PATCH) ? it.withRequired(false) : it)
					.orElse(property);
		});
	}

	private HalFormsProperty apply(HalFormsProperty property,
			BiFunction<InputPayloadMetadata, HalFormsProperty, I18nedPropertyMetadata> creator,
			Function<String, HalFormsProperty> application, MessageResolver resolver) {

		InputPayloadMetadata metadata = getInput();
		I18nedPropertyMetadata source = creator.apply(metadata, property);
		String resolved = resolver.resolve(source);

		return !StringUtils.hasText(resolved)
				? property
				: application.apply(resolved);
	}

	private static class I18nedPropertyMetadata implements MessageSourceResolvable {

		private final String template;
		private final InputPayloadMetadata metadata;
		private final HalFormsProperty property;

		protected I18nedPropertyMetadata(String template, InputPayloadMetadata metadata, HalFormsProperty property) {

			this.template = template;
			this.metadata = metadata;
			this.property = property;
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

	private static class I18nedPropertyPrompt extends I18nedPropertyMetadata {

		private I18nedPropertyPrompt(InputPayloadMetadata metadata, HalFormsProperty property) {
			super("%s._prompt", metadata, property);
		}

		public static I18nedPropertyPrompt of(InputPayloadMetadata metadata, HalFormsProperty property) {
			return new I18nedPropertyPrompt(metadata, property);
		}
	}

	private static class I18nedPlaceholder extends I18nedPropertyMetadata {

		private I18nedPlaceholder(InputPayloadMetadata metadata, HalFormsProperty property) {
			super("%s._placeholder", metadata, property);
		}

		public static I18nedPlaceholder of(InputPayloadMetadata metadata, HalFormsProperty property) {
			return new I18nedPlaceholder(metadata, property);
		}
	}
}
