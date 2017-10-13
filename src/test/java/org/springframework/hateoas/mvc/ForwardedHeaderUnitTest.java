/*
 * Copyright 2014 the original author or authors.
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

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;

/**
 * Unit tests for {@link ForwardedHeader}.
 * 
 * @author Oliver Gierke
 */
public class ForwardedHeaderUnitTest {

	/**
	 * @see #257
	 */
	@Test
	public void detectsProtoValue() {
		assertThat(ForwardedHeader.of("for=192.0.2.60;proto=http").getProto()).isEqualTo("http");
	}

	/**
	 * @see #257
	 */
	@Test
	public void detectsHostValue() {
		assertThat(ForwardedHeader.of("host=localhost;proto=http").getHost()).isEqualTo("localhost");
	}

	/**
	 * @see #257
	 */
	@Test
	public void returnsNullObjectForNullSource() {

		ForwardedHeader header = ForwardedHeader.of(null);

		assertThat(header).isNotNull();
		assertThat(header.getHost()).isNull();
		assertThat(header.getProto()).isNull();
	}
}
