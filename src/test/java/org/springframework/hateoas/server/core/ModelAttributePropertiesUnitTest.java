/*
 * Copyright 2026 the original author or authors.
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

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

/**
 * Unit tests for {@link ModelAttributeProperties}.
 *
 * @author Kim Tae Eun
 */
class ModelAttributePropertiesUnitTest {

	@Test // GH-1240
	void detectsBindablePropertiesInAlphabeticalOrder() {

		assertThat(getPropertyNames(SearchForm.class)) //
				.containsExactly("category", "includeArchived", "page", "sortBy", "tags");
	}

	@Test // GH-1240
	void skipsNestedObjectProperties() {
		assertThat(getPropertyNames(SearchForm.class)).doesNotContain("nested");
	}

	@Test // GH-1240
	void skipsDerivedReadOnlyProperties() {
		assertThat(getPropertyNames(SearchForm.class)).doesNotContain("summary");
	}

	@Test // GH-1240
	void skipsCollectionsOfNestedObjects() {
		assertThat(getPropertyNames(SearchForm.class)).doesNotContain("children");
	}

	@Test // GH-1240
	void detectsRecordComponents() {

		assertThat(getPropertyNames(SearchRecord.class)) //
				.containsExactly("category", "sortBy");
	}

	@Test // GH-1240
	void detectsInheritedProperties() {

		assertThat(getPropertyNames(ChildForm.class)) //
				.containsExactly("childProperty", "parentProperty");
	}

	@Test // GH-1240
	void resolvesTypeVariablesAgainstTheActualTypeArgument() {

		ResolvableType bindable = ResolvableType.forClassWithGenerics(GenericForm.class, String.class);

		assertThat(ModelAttributeProperties.getPropertyNames(bindable)).containsExactly("name", "value");
	}

	@Test // GH-1240
	void skipsTypeVariablesResolvingToANonSimpleType() {

		ResolvableType nested = ResolvableType.forClassWithGenerics(GenericForm.class, Nested.class);

		assertThat(ModelAttributeProperties.getPropertyNames(nested)).containsExactly("name");
	}

	@Test // GH-1240
	void tellsParameterisationsOfTheSameRawTypeApart() {

		ResolvableType simple = ResolvableType.forClassWithGenerics(GenericForm.class, LocalDate.class);
		ResolvableType nested = ResolvableType.forClassWithGenerics(GenericForm.class, Nested.class);

		assertThat(ModelAttributeProperties.getPropertyNames(simple)).contains("value");
		assertThat(ModelAttributeProperties.getPropertyNames(nested)).doesNotContain("value");
	}

	private static List<String> getPropertyNames(Class<?> type) {
		return ModelAttributeProperties.getPropertyNames(ResolvableType.forClass(type));
	}

	record SearchRecord(String category, String sortBy) {}

	static class GenericForm<T> {

		private T value;
		private String name;

		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	static class ParentForm {

		private String parentProperty;

		public String getParentProperty() {
			return parentProperty;
		}

		public void setParentProperty(String parentProperty) {
			this.parentProperty = parentProperty;
		}
	}

	static class ChildForm extends ParentForm {

		private String childProperty;

		public String getChildProperty() {
			return childProperty;
		}

		public void setChildProperty(String childProperty) {
			this.childProperty = childProperty;
		}
	}

	static class SearchForm {

		private String category;
		private String sortBy;
		private Boolean includeArchived;
		private int page;
		private List<String> tags;
		private List<Nested> children;
		private Nested nested;

		public String getCategory() {
			return category;
		}

		public void setCategory(String category) {
			this.category = category;
		}

		public String getSortBy() {
			return sortBy;
		}

		public void setSortBy(String sortBy) {
			this.sortBy = sortBy;
		}

		public Boolean getIncludeArchived() {
			return includeArchived;
		}

		public void setIncludeArchived(Boolean includeArchived) {
			this.includeArchived = includeArchived;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public List<Nested> getChildren() {
			return children;
		}

		public void setChildren(List<Nested> children) {
			this.children = children;
		}

		public Nested getNested() {
			return nested;
		}

		public void setNested(Nested nested) {
			this.nested = nested;
		}

		public String getSummary() {
			return category + "/" + sortBy;
		}
	}

	static class Nested {

		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
}
