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
package org.springframework.hateoas;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Unit tests for {@link QueryParameter} with {@link ModelAttribute} support.
 *
 * @author Oliver Drotbohm
 */
class QueryParameterModelAttributeTest {

	@Test
	void createsQueryParameterFromExplicitModelAttribute() throws Exception {

		Method method = SampleController.class.getMethod("methodWithExplicitModelAttribute", SearchCriteria.class);
		MethodParameter parameter = new MethodParameter(method, 0);

		QueryParameter queryParameter = QueryParameter.of(parameter);

		assertSoftly(softly -> {
			softly.assertThat(queryParameter.getName()).isEqualTo("criteria");
			softly.assertThat(queryParameter.isRequired()).isTrue();
			softly.assertThat(queryParameter.isExploded()).isTrue();
		});
	}

	@Test
	void createsQueryParameterFromImplicitModelAttribute() throws Exception {

		Method method = SampleController.class.getMethod("methodWithImplicitModelAttribute", SearchCriteria.class);
		MethodParameter parameter = new MethodParameter(method, 0);

		QueryParameter queryParameter = QueryParameter.of(parameter);

		assertSoftly(softly -> {
			softly.assertThat(queryParameter.getName()).isEqualTo("criteria");
			softly.assertThat(queryParameter.isRequired()).isTrue();
			softly.assertThat(queryParameter.isExploded()).isTrue();
		});
	}

	@Test
	void createsOptionalQueryParameterFromOptionalModelAttribute() throws Exception {

		Method method = SampleController.class.getMethod("methodWithOptionalModelAttribute", Optional.class);
		MethodParameter parameter = new MethodParameter(method, 0);

		QueryParameter queryParameter = QueryParameter.of(parameter);

		assertSoftly(softly -> {
			softly.assertThat(queryParameter.getName()).isEqualTo("criteria");
			softly.assertThat(queryParameter.isRequired()).isFalse();
			softly.assertThat(queryParameter.isExploded()).isTrue();
		});
	}

	@Test
	void doesNotCreateQueryParameterFromSimpleType() throws Exception {

		Method method = SampleController.class.getMethod("methodWithSimpleType", String.class);
		MethodParameter parameter = new MethodParameter(method, 0);

		QueryParameter queryParameter = QueryParameter.of(parameter);

		assertSoftly(softly -> {
			softly.assertThat(queryParameter.getName()).isEqualTo("name");
			softly.assertThat(queryParameter.isRequired()).isTrue();
			softly.assertThat(queryParameter.isExploded()).isFalse(); // Simple types should not be exploded
		});
	}

	@Test
	void respectsModelAttributeNameOverride() throws Exception {

		Method method = SampleController.class.getMethod("methodWithNamedModelAttribute", SearchCriteria.class);
		MethodParameter parameter = new MethodParameter(method, 0);

		QueryParameter queryParameter = QueryParameter.of(parameter);

		assertSoftly(softly -> {
			softly.assertThat(queryParameter.getName()).isEqualTo("search");
			softly.assertThat(queryParameter.isRequired()).isTrue();
			softly.assertThat(queryParameter.isExploded()).isTrue();
		});
	}

	@Test
	void createsExplodedQueryParameter() {

		QueryParameter exploded = QueryParameter.requiredExploded("test");

		assertSoftly(softly -> {
			softly.assertThat(exploded.getName()).isEqualTo("test");
			softly.assertThat(exploded.isRequired()).isTrue();
			softly.assertThat(exploded.isExploded()).isTrue();
		});
	}

	@Test
	void createsOptionalExplodedQueryParameter() {

		QueryParameter exploded = QueryParameter.optionalExploded("test");

		assertSoftly(softly -> {
			softly.assertThat(exploded.getName()).isEqualTo("test");
			softly.assertThat(exploded.isRequired()).isFalse();
			softly.assertThat(exploded.isExploded()).isTrue();
		});
	}

	@Test
	void preservesExplodedStateInWithValue() {

		QueryParameter original = QueryParameter.requiredExploded("test");
		QueryParameter withValue = original.withValue("value");

		assertSoftly(softly -> {
			softly.assertThat(withValue.getName()).isEqualTo("test");
			softly.assertThat(withValue.getValue()).isEqualTo("value");
			softly.assertThat(withValue.isRequired()).isTrue();
			softly.assertThat(withValue.isExploded()).isTrue();
		});
	}

	// Test controller for method parameter examples
	static class SampleController {

		public void methodWithExplicitModelAttribute(@ModelAttribute("criteria") SearchCriteria criteria) {}

		public void methodWithImplicitModelAttribute(@ModelAttribute("criteria") SearchCriteria criteria) {}

		public void methodWithOptionalModelAttribute(@ModelAttribute("criteria") Optional<SearchCriteria> criteria) {}

		public void methodWithSimpleType(@RequestParam("name") String name) {}

		public void methodWithNamedModelAttribute(@ModelAttribute("search") SearchCriteria criteria) {}

		public void methodWithRequestParam(@RequestParam("param") String param) {}
	}

	// Sample model class for testing
	static class SearchCriteria {
		private String query;
		private String category;
		private Integer page;
		private Integer size;

		public String getQuery() { return query; }
		public void setQuery(String query) { this.query = query; }

		public String getCategory() { return category; }
		public void setCategory(String category) { this.category = category; }

		public Integer getPage() { return page; }
		public void setPage(Integer page) { this.page = page; }

		public Integer getSize() { return size; }
		public void setSize(Integer size) { this.size = size; }
	}
}
