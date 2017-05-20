/*
 * Copyright 2017 the original author or authors.
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
package org.springframework.hateoas.mvc;

import static org.springframework.util.StringUtils.*;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Some common operations for {@link UriComponentsBuilder}, pulled into a utility class.
 * 
 * @author Greg Turnquist
 */
public final class UriComponentsSupport {

	private static final String REQUEST_ATTRIBUTES_MISSING = "Could not find current request via RequestContextHolder. Is this being called from a Spring MVC handler?";


	/**
	 * Returns a {@link UriComponentsBuilder} obtained from the current servlet mapping with scheme tweaked in case the
	 * request contains an {@code X-Forwarded-Ssl} header, which is not (yet) supported by the underlying
	 * {@link UriComponentsBuilder}.
	 *
	 * @return
	 */
	public static UriComponentsBuilder getBuilder() {

		HttpServletRequest request = getCurrentRequest();
		UriComponentsBuilder builder = ServletUriComponentsBuilder.fromServletMapping(request);

		// special case handling for X-Forwarded-Ssl:
		// apply it, but only if X-Forwarded-Proto is unset.

		String forwardedSsl = request.getHeader("X-Forwarded-Ssl");
		ForwardedHeader forwarded = ForwardedHeader.of(request.getHeader(ForwardedHeader.NAME));
		String proto = hasText(forwarded.getProto()) ? forwarded.getProto() : request.getHeader("X-Forwarded-Proto");

		if (!hasText(proto) && hasText(forwardedSsl) && forwardedSsl.equalsIgnoreCase("on")) {
			builder.scheme("https");
		}

		return builder;
	}

	/**
	 * Copy of {@link org.springframework.web.servlet.support.ServletUriComponentsBuilder#getCurrentRequest()} until SPR-10110 gets fixed.
	 *
	 * @return
	 */
	@SuppressWarnings("null")
	private static HttpServletRequest getCurrentRequest() {

		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		Assert.state(requestAttributes != null, REQUEST_ATTRIBUTES_MISSING);
		Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
		HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
		Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
		return servletRequest;
	}


}
