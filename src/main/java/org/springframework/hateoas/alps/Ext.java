/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.hateoas.alps;

import lombok.Builder;
import lombok.Value;

/**
 * A value object for an ALPS ext element.
 * 
 * @author Oliver Gierke
 * @since 0.15
 * @see http://alps.io/spec/#prop-ext
 */
@Value
@Builder(builderMethodName = "ext")
public class Ext {

	private final String id;
	private final String href;
	private final String value;
}
