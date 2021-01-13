/*
 * Copyright 2019-2021 the original author or authors.
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

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static org.springframework.hateoas.ArchitectureTest.Architecture.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.lang.Nullable;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.ImportOptions;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

/**
 * Tests to verify certain architectural assumptions.
 *
 * @author Oliver Drotbohm
 */
@TestInstance(Lifecycle.PER_CLASS)
class ArchitectureTest {

	ImportOptions options = new ImportOptions().with(ImportOption.Predefined.DONT_INCLUDE_TESTS);
	JavaClasses classes = new ClassFileImporter(options) //
			.importPackages("org.springframework.hateoas", "org.springframework");

	@Test
	void assertNoCyclicPackageDependencies() {

		SliceRule rule = SlicesRuleDefinition.slices() //
				.matching("org.springframework.hateoas.(**)..") //
				.should().beFreeOfCycles();

		rule.check(classes);
	}

	@Test // #1119
	void onlyReactivePackagesReferToReactiveTypesInSpringFramework() {

		DescribedPredicate<JavaClass> areSpringFrameworkClassesWithReactiveDependency = JavaClass.Predicates
				.resideInAnyPackage("org.springframework..") //
				.and(JavaClass.Predicates.resideOutsideOfPackage("..hateoas..")) //
				.and(dependsOn(reactiveType()));

		ArchRuleDefinition.noClasses().that() //
				.resideInAnyPackage("org.springframework.hateoas..") //
				.and().resideOutsideOfPackages("..reactive") //
				.and().haveSimpleNameNotStartingWith("WebFlux") //
				.and().haveSimpleNameNotStartingWith("WebClient") //
				.and().haveSimpleNameNotStartingWith("HypermediaWebClient") //
				.and().haveSimpleNameNotStartingWith("HypermediaWebTestClient") //
				.should().dependOnClassesThat(areSpringFrameworkClassesWithReactiveDependency) //
				.check(classes);
	}

	static class Architecture {

		public static DescribedPredicate<JavaClass> hasWebFluxPrefix() {
			return simpleNameStartingWith("WebClient").or(simpleNameStartingWith("WebFlux"));
		}

		public static DescribedPredicate<JavaClass> reactorType() {
			return resideInAPackage("reactor..");
		}

		public static DescribedPredicate<JavaClass> reactiveStreamsType() {
			return resideInAPackage("org.reactivestreams..");
		}

		public static DescribedPredicate<JavaClass> reactiveType() {
			return reactorType().or(reactiveStreamsType());
		}

		public static DescribedPredicate<JavaClass> dependsOn(DescribedPredicate<JavaClass> predicate) {

			return new DescribedPredicate<JavaClass>("depends on reactive types") {

				/*
				 * (non-Javadoc)
				 * @see com.tngtech.archunit.base.DescribedPredicate#apply(java.lang.Object)
				 */
				@Override
				public boolean apply(@Nullable JavaClass input) {

					return input != null && input.getDirectDependenciesFromSelf().stream() //
							.map(Dependency::getTargetClass) //
							.anyMatch(predicate::apply);
				}
			};
		}
	}
}
