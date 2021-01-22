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
package org.springframework.hateoas.mediatype.html;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.io.ResourceLoader;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

/**
 * @author Oliver Drotbohm
 */
public class HtmlFormRenderer {

	private static final String TEMPLATE = "classpath:org/springframework/hateoas/mediatype/html/form.template";

	private final Template template;
	private final MessageResolver messages;

	HtmlFormRenderer(ResourceLoader loader, MessageResolver resolver) throws IOException {

		try (Reader reader = new InputStreamReader(loader.getResource(TEMPLATE).getInputStream())) {

			com.samskivert.mustache.Mustache.Compiler compiler = Mustache.compiler();
			this.template = compiler.compile(reader);
			this.messages = resolver;
		}
	}

	@SuppressWarnings("unused")
	String renderForm(Affordance affordance) {

		AffordanceModel model = affordance.getAffordanceModel(MediaType.TEXT_HTML);

		if (model == null) {
			return "";
		}

		return template.execute(new Object() {

			String action = model.getLink().getHref();
			String method = model.getHttpMethod().name();
			Iterable<Object> properties = model.getInput().stream()
					.map(it -> {

						Map<String, Object> map = new HashMap<>();

						Optional<String> pattern = it.getPattern();

						if (pattern.isPresent()) {
							map.put("patternPrompt", messages.resolve(it.i18nize("_pattern")));
							map.put("pattern", pattern.get());
						}

						map.put("prompt", messages.resolve(it.i18nizeWithDefault("_prompt")));
						map.put("it", it);
						map.put("placeholder", messages.resolve(it.i18nize("_placeholder")));

						return map;
					})
					.collect(Collectors.toList());
		}).concat("\n");
	}

	String toOptionalAttribute(MessageSourceResolvable message, String template) {

		String resolve = messages.resolve(message);

		return StringUtils.hasText(resolve) ? String.format(template, resolve) + " " : "";
	}
}
