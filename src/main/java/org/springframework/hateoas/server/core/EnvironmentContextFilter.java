/*
 * Copyright 2020 the original author or authors.
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
package org.springframework.hateoas.server.core;

import javax.servlet.*;
import java.io.IOException;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * The {@code EnvironmentContextFilter} populates the {@link EnvironmentContext} with the {@link Environment} of the
 * current servlet request.
 *
 * @author Lars Michele
 */
public class EnvironmentContextFilter implements Filter, EnvironmentAware {

	private Environment environment;

	@Override
	public void init(FilterConfig filterConfig) {
		// NOOP
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		try {
			EnvironmentContext.set(environment);
			chain.doFilter(servletRequest, servletResponse);
		} finally {
			EnvironmentContext.clear();
		}
	}


	@Override
	public void destroy() {
		// NOOP
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
}
