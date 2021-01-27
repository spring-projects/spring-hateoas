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
package org.springframework.hateoas.mediatype;

import java.io.IOException;

import org.springframework.context.MessageSourceResolvable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * A Jackson serializer triggering message resolution via a {@link MessageResolver} for {@link MessageSourceResolvable}
 * instances about to be serialized.
 *
 * @author Oliver Drotbohm
 * @since 1.3
 */
public class MessageSourceResolvableSerializer extends StdSerializer<MessageSourceResolvable> {

	private static final long serialVersionUID = 4302540100251549622L;

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
	 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
	 */
	@Override
	@SuppressWarnings("null")
	public void serialize(MessageSourceResolvable value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		gen.writeString(resolver.resolve(value));
	}
}
