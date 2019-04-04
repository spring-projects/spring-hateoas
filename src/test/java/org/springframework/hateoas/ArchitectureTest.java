/*
 * Copyright 2019 the original author or authors.
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

import org.junit.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOptions;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Tests to verify certain architectural assumptions.
 *
 * @author Oliver Gierke
 */
public class ArchitectureTest {

	ImportOptions options = new ImportOptions().with(ImportOption.Predefined.DONT_INCLUDE_TESTS);
	JavaClasses classes = new ClassFileImporter(options) //
			.importPackages("org.springframework.hateoas");

	@Test
	public void assertNoCyclicPackageDependencies() {

		SliceRule rule = SlicesRuleDefinition.slices() //
				.matching("org.springframework.hateoas.(**)..") //
				.should().beFreeOfCycles();

		rule.check(classes);
	}
}
