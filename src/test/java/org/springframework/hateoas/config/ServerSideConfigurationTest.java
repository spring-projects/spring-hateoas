package org.springframework.hateoas.config;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ServerSideConfigurationTest {

	@Test
	void simpleCreation() {
		assertThat(new ServerSideConfiguration().getBaseUrl()).isEqualTo("");
	}

	@Test
	void definingABaseUrl() {
		assertThat(new ServerSideConfiguration().withBaseUrl("/api").getBaseUrl()).isEqualTo("/api");
	}

	@Test
	void overridingABaseUrl() {
		assertThat(new ServerSideConfiguration().withBaseUrl("/api").withBaseUrl("/other").getBaseUrl())
				.isEqualTo("/other");
	}
}
