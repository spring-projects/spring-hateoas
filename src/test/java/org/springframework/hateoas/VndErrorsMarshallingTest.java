/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.support.MappingUtils.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.VndErrors.VndError;
import org.springframework.hateoas.core.AnnotationRelProvider;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Integration tests for marshalling of {@link VndErrors}.
 *
 * @author Oliver Gierke
 * @author Greg Turnquist
 */
public class VndErrorsMarshallingTest {

	ObjectMapper mapper;

	@Before
	public void setUp() {

		RelProvider relProvider = new AnnotationRelProvider();
		
		mapper = new com.fasterxml.jackson.databind.ObjectMapper();
		mapper.registerModule(new Jackson2HalModule());
		mapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(relProvider, null, null));
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
	}

	/**
	 * @see #93, #94, #775
	 */
	@Test
	public void singleItemVndErrorShouldDeserialize() throws IOException {

		String json = read(new ClassPathResource("vnderror-single-item.json"));

		VndError error = new VndError("Validation failed", "/username", 42, //
			new Link("https://path.to/user/resource/1", VndErrors.REL_ABOUT),
			new Link("https://path.to/describes", VndErrors.REL_DESCRIBES),
			new Link("https://path.to/help", VndErrors.REL_HELP));

		assertThat(mapper.readValue(json, VndError.class)).isEqualTo(error);
	}

	/**
	 * @see #62, #775
	 */
	@Test
	public void singleItemVndErrorShouldSerialize() throws IOException {

		VndError error = new VndError("Validation failed", "/username", 42, //
			new Link("https://path.to/user/resource/1", VndErrors.REL_ABOUT),
			new Link("https://path.to/describes", VndErrors.REL_DESCRIBES),
			new Link("https://path.to/help", VndErrors.REL_HELP));

		String json = read(new ClassPathResource("vnderror-single-item.json"));

		assertThat(mapper.writeValueAsString(error)).isEqualTo(json);

	}

	/**
	 * @see #775
	 */
	@Test
	public void multipleItemVndErrorsShouldDeserialize() throws IOException {

		String json = read(new ClassPathResource("vnderrors-multiple-item.json"));

		VndError error1 = new VndError("\"username\" field validation failed", null, 50, //
			new Link("https://.../", VndErrors.REL_HELP));

		VndError error2 = new VndError("\"postcode\" field validation failed", null, 55, //
			new Link("https://.../", VndErrors.REL_HELP));

		VndErrors vndErrors = new VndErrors().withError(error1).withError(error2);

		assertThat(mapper.readValue(json, VndErrors.class)).isEqualTo(vndErrors);
	}

	/**
	 * @see #775
	 */
	@Test
	public void multipleItemVndErrorsShouldSerialize() throws IOException {

		VndError error1 = new VndError("\"username\" field validation failed", null, 50, //
			new Link("https://.../", VndErrors.REL_HELP));

		VndError error2 = new VndError("\"postcode\" field validation failed", null, 55, //
			new Link("https://.../", VndErrors.REL_HELP));

		VndErrors vndErrors = new VndErrors().withError(error1).withError(error2);

		String json = read(new ClassPathResource("vnderrors-multiple-item.json"));

		assertThat(mapper.writeValueAsString(vndErrors)).isEqualTo(json);
	}

	/**
	 * @see #775
	 */
	@Test
	public void nestedVndErrorsShouldDeserialize() throws IOException {

		String json = read(new ClassPathResource("vnderrors-nested.json"));

		VndError error = new VndError("Username must contain at least three characters", "/username", null, //
			new Link("https://path.to/user/resource/1", VndErrors.REL_ABOUT));

		VndErrors vndErrors = new VndErrors()
			.withError(error)
			.withLink(new Link("https://path.to/describes").withRel(VndErrors.REL_DESCRIBES))
			.withLink(new Link("https://path.to/help").withRel(VndErrors.REL_HELP))
			.withLink(new Link("https://path.to/user/resource/1").withRel(VndErrors.REL_ABOUT))
			.withMessage("Validation failed")
			.withLogref(42);

		assertThat(mapper.readValue(json, VndErrors.class)).isEqualTo(vndErrors);
	}

	/**
	 * @see #775
	 */
	@Test
	public void nestedVndErrorsShouldSerialize() throws IOException {

		VndError error = new VndError("Username must contain at least three characters", "/username", null, //
			new Link("https://path.to/user/resource/1", VndErrors.REL_ABOUT));

		VndErrors vndErrors = new VndErrors()
			.withError(error)
			.withLink(new Link("https://path.to/describes").withRel(VndErrors.REL_DESCRIBES))
			.withLink(new Link("https://path.to/help").withRel(VndErrors.REL_HELP))
			.withLink(new Link("https://path.to/user/resource/1").withRel(VndErrors.REL_ABOUT))
			.withMessage("Validation failed")
			.withLogref(42);

		String json = read(new ClassPathResource("vnderrors-nested.json"));

		assertThat(mapper.writeValueAsString(vndErrors)).isEqualTo(json);
	}
}
