/*
 * Copyright 2018-2024 the original author or authors.
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
package org.springframework.hateoas.server.core;

import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.QueryParameter;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Integration tests for {@link SpringAffordanceBuilder} with {@link ModelAttribute} support.
 *
 * @author Oliver Drotbohm
 */
class SpringAffordanceBuilderModelAttributeTest {

	@Test
	void includesExplicitModelAttributeParametersInAffordances() throws Exception {

		Method method = TestController.class.getMethod("searchWithExplicitModelAttribute", SearchForm.class);
		List<Affordance> affordances = SpringAffordanceBuilder.getAffordances(TestController.class, method, "/search");

		assertThat(affordances).hasSize(1);

		AffordanceModel model = affordances.get(0).getAffordanceModel(MediaType.APPLICATION_JSON);
		List<QueryParameter> queryParameters = model.getQueryMethodParameters();

		assertThat(queryParameters).hasSize(1);
		QueryParameter parameter = queryParameters.get(0);

		assertThat(parameter.getName()).isEqualTo("searchForm");
		assertThat(parameter.isRequired()).isTrue();
		assertThat(parameter.isExploded()).isTrue();
	}

	@Test
	void includesImplicitModelAttributeParametersInAffordances() throws Exception {

		Method method = TestController.class.getMethod("searchWithImplicitModelAttribute", SearchForm.class);
		List<Affordance> affordances = SpringAffordanceBuilder.getAffordances(TestController.class, method, "/search");

		assertThat(affordances).hasSize(1);

		AffordanceModel model = affordances.get(0).getAffordanceModel(MediaType.APPLICATION_JSON);
		List<QueryParameter> queryParameters = model.getQueryMethodParameters();

		assertThat(queryParameters).hasSize(1);
		QueryParameter parameter = queryParameters.get(0);

		assertThat(parameter.getName()).isEqualTo("searchForm");
		assertThat(parameter.isRequired()).isTrue();
		assertThat(parameter.isExploded()).isTrue();
	}

	@Test
	void includesBothRequestParamAndModelAttributeParameters() throws Exception {

		Method method = TestController.class.getMethod("searchWithMixedParameters", String.class, SearchForm.class, Integer.class);
		List<Affordance> affordances = SpringAffordanceBuilder.getAffordances(TestController.class, method, "/search");

		assertThat(affordances).hasSize(1);

		AffordanceModel model = affordances.get(0).getAffordanceModel(MediaType.APPLICATION_JSON);
		List<QueryParameter> queryParameters = model.getQueryMethodParameters();

		assertThat(queryParameters).hasSize(3);

		// Find parameters by name
		QueryParameter queryParam = queryParameters.stream()
				.filter(p -> "q".equals(p.getName()))
				.findFirst()
				.orElseThrow();

		QueryParameter modelAttrParam = queryParameters.stream()
				.filter(p -> "filters".equals(p.getName()))
				.findFirst()
				.orElseThrow();

		QueryParameter implicitParam = queryParameters.stream()
				.filter(p -> "page".equals(p.getName()))
				.findFirst()
				.orElseThrow();

		// Verify @RequestParam behavior
		assertThat(queryParam.getName()).isEqualTo("q");
		assertThat(queryParam.isRequired()).isTrue();
		assertThat(queryParam.isExploded()).isFalse();

		// Verify @ModelAttribute behavior
		assertThat(modelAttrParam.getName()).isEqualTo("filters");
		assertThat(modelAttrParam.isRequired()).isTrue();
		assertThat(modelAttrParam.isExploded()).isTrue();

		// Verify simple type (implicit @RequestParam, not @ModelAttribute)
		assertThat(implicitParam.getName()).isEqualTo("page");
		assertThat(implicitParam.isRequired()).isTrue();
		assertThat(implicitParam.isExploded()).isFalse();
	}

	@Test
	void handlesOptionalModelAttributeParameters() throws Exception {

		Method method = TestController.class.getMethod("searchWithOptionalModelAttribute", Optional.class);
		List<Affordance> affordances = SpringAffordanceBuilder.getAffordances(TestController.class, method, "/search");

		assertThat(affordances).hasSize(1);

		AffordanceModel model = affordances.get(0).getAffordanceModel(MediaType.APPLICATION_JSON);
		List<QueryParameter> queryParameters = model.getQueryMethodParameters();

		assertThat(queryParameters).hasSize(1);
		QueryParameter parameter = queryParameters.get(0);

		assertThat(parameter.getName()).isEqualTo("searchForm");
		assertThat(parameter.isRequired()).isFalse();
		assertThat(parameter.isExploded()).isTrue();
	}

	// Test controller for integration testing
	@RestController
	static class TestController {

		@GetMapping("/search")
		public String searchWithExplicitModelAttribute(@ModelAttribute("searchForm") SearchForm searchForm) {
			return "result";
		}

		@GetMapping("/search")
		public String searchWithImplicitModelAttribute(SearchForm searchForm) {
			return "result";
		}

		@GetMapping("/search")
		public String searchWithMixedParameters(@RequestParam("q") String query,
												@ModelAttribute("filters") SearchForm filters,
												Integer page) {
			return "result";
		}

		@GetMapping("/search")
		public String searchWithOptionalModelAttribute(@ModelAttribute("searchForm") Optional<SearchForm> searchForm) {
			return "result";
		}
	}

	// Sample form class for testing
	static class SearchForm {
		private String category;
		private String sortBy;
		private Boolean includeArchived;

		public String getCategory() { return category; }
		public void setCategory(String category) { this.category = category; }

		public String getSortBy() { return sortBy; }
		public void setSortBy(String sortBy) { this.sortBy = sortBy; }

		public Boolean getIncludeArchived() { return includeArchived; }
		public void setIncludeArchived(Boolean includeArchived) { this.includeArchived = includeArchived; }
	}
}
