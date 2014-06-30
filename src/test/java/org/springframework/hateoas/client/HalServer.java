/*
 * Copyright 2013-2014 the original author or authors.
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
package org.springframework.hateoas.client;

import static net.jadler.Jadler.initJadler;
import static net.jadler.Jadler.onRequest;

import java.nio.charset.Charset;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;

/**
 * Helper class for integration tests.
 * 
 * @author Oliver Gierke
 */
public class HalServer extends DefaultJsonServer {

	public HalServer() {
		this.mapper.registerModule(new Jackson2HalModule());
		this.mapper.setHandlerInstantiator(new Jackson2HalModule.HalHandlerInstantiator(relProvider, null));
	}

	protected void init() {
		initJadler(). //
				that().//
				respondsWithDefaultContentType(MediaTypes.HAL_JSON.toString()). //
				respondsWithDefaultStatus(200).//
				respondsWithDefaultEncoding(Charset.forName("UTF-8"));

		onRequest(). //
				havingPathEqualTo("/"). //
				respond(). //
				withBody("");
	}

}
