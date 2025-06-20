/*
 * Copyright 2021-2024 the original author or authors.
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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to be used in combination with {@link RequestParam} to indicate that collection based values are supposed
 * to be rendered as non-composite values, i.e. like {@code param=value1,value2,value3} rather than
 * {@code param=value1&param=value2} when generating links by pointing to controller methods.
 *
 * @author Oliver Drotbohm
 * @since 1.4
 */
@Retention(RUNTIME)
@Target({ PARAMETER, ANNOTATION_TYPE })
public @interface NonComposite {}
