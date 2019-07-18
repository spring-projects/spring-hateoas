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
package org.springframework.hateoas.mediatype;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A {@link MessageResolver} based on a {@link MessageSource}.
 *
 * @author Oliver Drotbohm
 */
class MessageSourceResolver implements MessageResolver {

	private final MessageSourceAccessor accessor;

	/**
	 * Creates a new {@link MessageSourceResolver} for the given {@link MessageSource}.
	 *
	 * @param messageSource must not be {@literal null}.
	 */
	MessageSourceResolver(MessageSource messageSource) {

		Assert.notNull(messageSource, "MessageSource must not be null!");

		this.accessor = new MessageSourceAccessor(messageSource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.mediatype.MessageResolver#resolve(org.springframework.context.MessageSourceResolvable)
	 */
	@Nullable
	@Override
	public String resolve(MessageSourceResolvable resolvable) {

		String resolved = accessor.getMessage(resolvable);

		return StringUtils.hasText(resolved) ? resolved : null;
	}
}
