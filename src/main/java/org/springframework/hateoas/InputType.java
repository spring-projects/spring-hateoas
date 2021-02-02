/*
 * Copyright 2021 the original author or authors.
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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.hateoas.mediatype.html.HtmlInputType;

/**
 * Annotation to declare a dedicated input type for a property of an representation model. Input types are usually
 * derived from the property's type or JSR-303 validation annotations. Use this annotation to override the type.
 * <p>
 * Values are usually constrained by {@link HtmlInputType} as most media types align with those semantically. That said,
 * the annotation doesn't prescribe the usage of those and is open for extensions.
 *
 * @author Oliver Drotbohm
 * @see HtmlInputType
 * @since 1.3
 * @soundtrack Boney M - Daddy Cool (Take The Heat Off Me)
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD, ANNOTATION_TYPE })
public @interface InputType {
	String value();
}
