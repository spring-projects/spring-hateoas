package org.springframework.hateoas.mediatype.hal.forms;

import lombok.Data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.*;

public class HalFormsPropertyOrderingUnitTest {

	private RepresentationModel<?> model;

	@BeforeEach
	void setUp() {

		this.model = new RepresentationModel<>(Affordances.of(Link.of("/example")) //
				.afford(HttpMethod.POST) //
				.withInput(Thing.class) //
				.toLink());
	}

	@Test
	void noCustomOrdering() {

		HalFormsConfiguration halFormsConfiguration = new HalFormsConfiguration();

		assertThat(createTemplate(halFormsConfiguration).getProperties()).flatExtracting(HalFormsProperty::getName)
				.containsExactly("a", "b", "z");

	}

	@Test
	void specifyAllProperties() {

		HalFormsConfiguration halFormsConfiguration = new HalFormsConfiguration() //
				.withFieldOrderFor(Thing.class, "z", "b", "a");

		assertThat(createTemplate(halFormsConfiguration).getProperties()).flatExtracting(HalFormsProperty::getName)
				.containsExactly("z", "b", "a");
	}

	@Test
	void specifySomeProperties() {

		HalFormsConfiguration halFormsConfiguration = new HalFormsConfiguration() //
				.withFieldOrderFor(Thing.class, "z");

		assertThat(createTemplate(halFormsConfiguration).getProperties()).flatExtracting(HalFormsProperty::getName)
				.containsExactly("z", "a", "b");
	}

	@Test
	void nonExistentProperty() {

		HalFormsConfiguration halFormsConfiguration = new HalFormsConfiguration() //
				.withFieldOrderFor(Thing.class, "q", "b");

		assertThat(createTemplate(halFormsConfiguration).getProperties()).flatExtracting(HalFormsProperty::getName)
				.containsExactly("b", "a", "z");
	}

	private HalFormsTemplate createTemplate(HalFormsConfiguration halFormsConfiguration) {
		return new HalFormsTemplateBuilder(halFormsConfiguration, MessageResolver.DEFAULTS_ONLY).findTemplates(this.model)
				.get("default");
	}

	@Data
	private static class Thing {

		private String a;
		private String b;
		private String z;
	}
}
