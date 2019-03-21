/*
 * Copyright 2018-2019 the original author or authors.
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
package org.springframework.hateoas.uber;

import org.springframework.hateoas.Resources;
import org.springframework.hateoas.uber.Jackson2UberModule.UberResourcesDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Jackson 2 mixin to handle {@link Resources} for {@literal UBER+JSON}.
 *
 * @author Greg Turnquist
 * @since 1.0
 */
@JsonDeserialize(using = UberResourcesDeserializer.class)
abstract class ResourcesMixin<T> extends Resources<T> {

}