/*
 * Copyright 2021-2026 the original author or authors.
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
package org.springframework.hateoas.mediatype;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import org.springframework.context.MessageSourceResolvable;

/**
 * A Jackson serializer triggering message resolution via a {@link MessageResolver} for {@link MessageSourceResolvable}
 * instances about to be serialized.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 */
public class MessageSourceResolvableSerializer extends StdSerializer<MessageSourceResolvable> {

	private final MessageResolver resolver;

	/**
	 * Creates a new {@link MessageSourceResolvableSerializer} for the given {@link MessageResolver}.
	 *
	 * @param resolver must not be {@literal null}.
	 */
	public MessageSourceResolvableSerializer(MessageResolver resolver) {

		super(MessageSourceResolvable.class);

		this.resolver = resolver;
	}

	/*
	 * (non-Javadoc)
	 * @see tools.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, tools.jackson.core.JsonGenerator, tools.jackson.databind.SerializationContext)
	 */
	@Override
	@SuppressWarnings("null")
	public void serialize(MessageSourceResolvable value, JsonGenerator gen, SerializationContext provider) {
		gen.writeString(resolver.resolve(value));
	}
}
