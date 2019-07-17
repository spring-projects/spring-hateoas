/*
 * Copyright 2012-2018 the original author or authors.
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
package org.springframework.hateoas.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.hateoas.server.core.DelegatingEntityLinks;

/**
 * Enables the collection of {@link LinkBuilder} instances from the application context. All found ones will be exposed
 * through an instance of {@link DelegatingEntityLinks}.
 *
 * @author Oliver Gierke
 * @deprecated since 1.0 RC, to be removed in 1.0 RC2. EntityLinks are enabled by {@link EnableHypermediaSupport}
 *             automatically, so there's no need to explicitly activate them.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Documented
@Import({ EntityLinksConfiguration.class, WebMvcEntityLinksConfiguration.class })
@Deprecated
public @interface EnableEntityLinks {
}
