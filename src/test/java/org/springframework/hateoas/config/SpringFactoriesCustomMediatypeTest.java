package org.springframework.hateoas.config;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.hateoas.support.ContextTester.*;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType;
import org.springframework.hateoas.support.SpringFactoriesCustomMediatype;

class SpringFactoriesCustomMediatypeTest {

	@Test
	void springFactoriesRegisteredMediatypeShouldBeAvailableAmidstHal() {

		withServletContext(HalConfig.class, context -> {
			Collection<HypermediaMappingInformation> mappingInformation = context
					.getBeansOfType(HypermediaMappingInformation.class).values();

			assertThat(mappingInformation).flatExtracting(HypermediaMappingInformation::getMediaTypes)
					.containsExactlyInAnyOrder(MediaTypes.HAL_JSON, SpringFactoriesCustomMediatype.BILBO_MEDIATYPE);

		});
	}

	@Test
	void springFactoriesRegisteredMediatypeShouldBeAvailableAmidstCollectionJson() {

		withServletContext(CollectionJsonConfig.class, context -> {
			Collection<HypermediaMappingInformation> mappingInformation = context
					.getBeansOfType(HypermediaMappingInformation.class).values();

			assertThat(mappingInformation).flatExtracting(HypermediaMappingInformation::getMediaTypes)
					.containsExactlyInAnyOrder(MediaTypes.COLLECTION_JSON, SpringFactoriesCustomMediatype.BILBO_MEDIATYPE);

		});
	}

	@Test
	void springFactoriesRegisteredMediatypeShouldBeAvailableAmidstHalForms() {

		withServletContext(HalFormsConfig.class, context -> {
			Collection<HypermediaMappingInformation> mappingInformation = context
					.getBeansOfType(HypermediaMappingInformation.class).values();

			assertThat(mappingInformation).flatExtracting(HypermediaMappingInformation::getMediaTypes)
					.containsExactlyInAnyOrder(MediaTypes.HAL_FORMS_JSON, SpringFactoriesCustomMediatype.BILBO_MEDIATYPE);

		});
	}

	@Test
	void springFactoriesRegisteredMediatypeShouldBeAvailableAmidstUber() {

		withServletContext(UberConfig.class, context -> {
			Collection<HypermediaMappingInformation> mappingInformation = context
					.getBeansOfType(HypermediaMappingInformation.class).values();

			assertThat(mappingInformation).flatExtracting(HypermediaMappingInformation::getMediaTypes)
					.containsExactlyInAnyOrder(MediaTypes.UBER_JSON, SpringFactoriesCustomMediatype.BILBO_MEDIATYPE);

		});
	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL)
	static class HalConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.COLLECTION_JSON)
	static class CollectionJsonConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.HAL_FORMS)
	static class HalFormsConfig {

	}

	@Configuration
	@EnableHypermediaSupport(type = HypermediaType.UBER)
	static class UberConfig {

	}
}
