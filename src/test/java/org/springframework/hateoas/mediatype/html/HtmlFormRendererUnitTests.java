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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.IOException;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpMethod;

/**
 * @author Oliver Drotbohm
 */
class HtmlFormRendererUnitTests {

	ResourceLoader loader = new PathMatchingResourcePatternResolver();

	@Test
	void testName() throws IOException {

		Link link = Affordances.of(Link.of("/target"))
				.afford(HttpMethod.POST)
				.withInput(Payload.class)
				.toLink();

		LocaleContextHolder.setLocale(Locale.GERMAN);
		StaticMessageSource source = new StaticMessageSource();
		source.addMessage("Payload.firstname._prompt", Locale.GERMAN, "Vorname");
		source.addMessage("Payload.lastname._prompt", Locale.GERMAN, "Nachname");
		source.addMessage("Payload.email._prompt", Locale.GERMAN, "E-Mail-Adresse");
		source.addMessage("Payload.email._placeholder", Locale.GERMAN, "foo@bar.com");
		source.addMessage("Payload.ccn._prompt", Locale.GERMAN, "Kreditkartennummer");
		source.addMessage("Payload.ccn._pattern", Locale.GERMAN, "16 Ziffern");

		HtmlFormRenderer renderer = new HtmlFormRenderer(loader, MessageResolver.of(source));

		System.out.println(renderer.renderForm(link.getAffordances().get(0)));
	}

	@Data
	static class Payload {
		String firstname, lastname;
		@Email String email;
		@Pattern(regexp = "[0-9]{16}") String ccn;
	}
}
