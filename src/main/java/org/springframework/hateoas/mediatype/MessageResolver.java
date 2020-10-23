/*
 * Copyright 2019-2020 the original author or authors.
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

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;

/**
 * A simplified variant of {@link MessageSourceAccessor} to allow more direct replacement with a no-op implementation in
 * case the target {@link MessageSource} is unavailable to avoid resolution overhead.
 *
 * @author Oliver Drotbohm
 */
public interface MessageResolver {

	MessageResolver DEFAULTS_ONLY = DefaultOnlyMessageResolver.INSTANCE;

	/**
	 * Resolve the given {@link MessageSourceResolvable}. Return {@literal null} if no message was found.
	 *
	 * @param resolvable must not be {@literal null}.
	 * @return
	 */
	@Nullable
	String resolve(MessageSourceResolvable resolvable);

	/**
	 * Obtains a {@link MessageResolver} for the given {@link MessageSource}.
	 *
	 * @param messageSource can be {@literal null}.
	 * @return will never be {@literal null}.
	 */
	static MessageResolver of(@Nullable MessageSource messageSource) {

		return messageSource == null //
				? DefaultOnlyMessageResolver.INSTANCE //
				: new MessageSourceResolver(messageSource);
	}
}
