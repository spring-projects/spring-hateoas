/*
 * Copyright 2013-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.vnderror;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.support.MappingUtils.*;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule.HalHandlerInstantiator;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors.VndError;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.AnnotationLinkRelationProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Integration tests for marshalling of {@link VndErrors}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
class VndErrorsMarshallingTest {

	ObjectMapper mapper;

	@BeforeEach
	void setUp() {

		LinkRelationProvider relProvider = new AnnotationLinkRelationProvider();

		this.mapper = new ObjectMapper();
		this.mapper.registerModule(new Jackson2HalModule());
		this.mapper.setHandlerInstantiator(
				new HalHandlerInstantiator(relProvider, CurieProvider.NONE, MessageResolver.DEFAULTS_ONLY));
		this.mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	@Test // #93, #94, #775
	void singleItemVndErrorShouldDeserialize() throws IOException {

		String expected = read(new ClassPathResource("vnderror-single-item.json", getClass()));

		VndError actual = new VndError("Validation failed", "/username", 42,
				Link.of("http://path.to/user/resource/1", IanaLinkRelations.ABOUT),
				Link.of("http://path.to/describes", IanaLinkRelations.DESCRIBES),
				Link.of("http://path.to/help", IanaLinkRelations.HELP));

		assertThat(this.mapper.readValue(expected, VndError.class)).isEqualTo(actual);
	}

	@Test // #62, #775
	public void singleItemVndErrorShouldSerialize() throws IOException {

		VndError error = new VndError("Validation failed", "/username", 42, //
				Link.of("http://path.to/user/resource/1", IanaLinkRelations.ABOUT),
				Link.of("http://path.to/describes", IanaLinkRelations.DESCRIBES),
				Link.of("http://path.to/help", IanaLinkRelations.HELP));

		String json = read(new ClassPathResource("vnderror-single-item.json", getClass()));

		assertThat(mapper.writeValueAsString(error)).isEqualToIgnoringWhitespace(json);
	}

	@Test // #62, #775
	public void multipleItemVndErrorsShouldDeserialize() throws IOException {

		String json = read(new ClassPathResource("vnderror-multiple-items.json", getClass()));

		VndError error1 = new VndError("\"username\" field validation failed", null, 50, //
				Link.of("http://.../", IanaLinkRelations.HELP));

		VndError error2 = new VndError("\"postcode\" field validation failed", null, 55, //
				Link.of("http://.../", IanaLinkRelations.HELP));

		VndErrors vndErrors = new VndErrors().withError(error1).withError(error2);

		assertThat(this.mapper.readValue(json, VndErrors.class)).isEqualTo(vndErrors);
	}

	@Test // #775
	public void multipleItemVndErrorsShouldSerialize() throws IOException {

		VndError error1 = new VndError("\"username\" field validation failed", null, 50, //
				Link.of("http://.../", IanaLinkRelations.HELP));

		VndError error2 = new VndError("\"postcode\" field validation failed", null, 55, //
				Link.of("http://.../", IanaLinkRelations.HELP));

		VndErrors vndErrors = new VndErrors().withError(error1).withError(error2);

		String json = read(new ClassPathResource("vnderror-multiple-items.json", getClass()));

		assertThat(this.mapper.writeValueAsString(vndErrors)).isEqualToIgnoringWhitespace(json);
	}

	@Test // #775
	public void nestedVndErrorsShouldSerialize() throws IOException {

		VndError error = new VndError("Username must contain at least three characters", "/username", (Integer) null, //
				Link.of("http://path.to/user/resource/1", IanaLinkRelations.ABOUT));

		VndErrors vndErrors = new VndErrors().withError(error)
				.withLink(Link.of("http://path.to/describes").withRel(IanaLinkRelations.DESCRIBES))
				.withLink(Link.of("http://path.to/help").withRel(IanaLinkRelations.HELP))
				.withLink(Link.of("http://path.to/user/resource/1").withRel(IanaLinkRelations.ABOUT))
				.withMessage("Validation failed").withLogref(42);

		String json = read(new ClassPathResource("vnderror-nested.json", getClass()));

		assertThat(mapper.writeValueAsString(vndErrors)).isEqualToIgnoringWhitespace(json);
	}

	@Test // #1291
	void basicVndErrorShouldSerialize() throws IOException {

		VndError error = new VndError("message", "path", "alphaLogref", Link.of("foo", "bar"));

		String json = read(new ClassPathResource("vnderror-string-logref.json", getClass()));

		assertThat(mapper.writeValueAsString(error)).isEqualToIgnoringWhitespace(json);
	}
}
