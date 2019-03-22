/*
 * Copyright 2012-2016 the original author or authors.
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
package org.springframework.hateoas.server.mvc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * Special {@link RequestMappingHandlerAdapter} that tweaks the {@link HandlerMethodReturnValueHandlerComposite} to be
 * proxied by a {@link RepresentationModelProcessorHandlerMethodReturnValueHandler} which will invoke the
 * {@link RepresentationModelProcessor} s found in the application context and eventually delegate to the originally
 * configured {@link HandlerMethodReturnValueHandler}.
 * <p/>
 * This is a separate component as it might make sense to deploy it in a standalone SpringMVC application to enable post
 * processing. It would actually make most sense in Spring HATEOAS project.
 * 
 * @author Oliver Gierke
 * @author Phil Webb
 * @since 0.20
 * @soundtrack Dopplekopf - Regen für immer (Von Abseits)
 */
@RequiredArgsConstructor
public class RepresentationModelProcessorInvokingHandlerAdapter extends RequestMappingHandlerAdapter {

	private @NonNull final RepresentationModelProcessorInvoker invoker;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		super.afterPropertiesSet();

		// Retrieve actual handlers to use as delegate
		HandlerMethodReturnValueHandlerComposite oldHandlers = getReturnValueHandlersComposite();

		// Set up ResourceProcessingHandlerMethodResolver to delegate to originally configured ones
		List<HandlerMethodReturnValueHandler> newHandlers = new ArrayList<>();
		newHandlers.add(new RepresentationModelProcessorHandlerMethodReturnValueHandler(oldHandlers, invoker));

		// Configure the new handler to be used
		this.setReturnValueHandlers(newHandlers);
	}

	/**
	 * Gets a {@link HandlerMethodReturnValueHandlerComposite} for return handlers, dealing with API changes introduced in
	 * Spring 4.0.
	 * 
	 * @return a HandlerMethodReturnValueHandlerComposite
	 */
	@SuppressWarnings("unchecked")
	private HandlerMethodReturnValueHandlerComposite getReturnValueHandlersComposite() {

		Object handlers = this.getReturnValueHandlers();

		if (handlers instanceof HandlerMethodReturnValueHandlerComposite) {
			return (HandlerMethodReturnValueHandlerComposite) handlers;
		}

		return new HandlerMethodReturnValueHandlerComposite() //
				.addHandlers((List<? extends HandlerMethodReturnValueHandler>) handlers);
	}
}
