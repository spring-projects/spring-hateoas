/*
 * Copyright 2016 the original author or authors.
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
package org.springframework.hateoas.core;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.hateoas.core.Recorder.DefaultPropertyNameDetectionStrategy.*;

import lombok.Getter;
import org.junit.Ignore;
import org.junit.Test;

import org.springframework.core.convert.converter.Converter;
import org.springframework.hateoas.core.Recorder.Recorded;

/**
 * @author Oliver Gierke
 */
public class RecorderUnitTests {

	@Test
	public void returnsPropertyName() {

		assertThat(getPropertyName(Bar.class, "getBar"), is("bar"));
		assertThat(getPropertyName(Boolean.class, "getTrue"), is("true"));
		assertThat(getPropertyName(boolean.class, "isTrue"), is("true"));
	}

	@Ignore
	@Test
	public void createsPropertyPathForNestedCall() {

		Recorded<Object> wrapper = Recorder.forType(Foo.class).apply(FooConverter.INSTANCE);

		// FooConverter.INSTANCE.convert(wrapper.getObject());

		assertThat(wrapper.getCurrentPropertyName(), is("bar.fooBar"));
	}

	/**
	 * @author Oliver Gierke
	 */
	private enum FooConverter implements Converter<Foo, Object> {

		INSTANCE;

		@Override
		public Object convert(Foo source) {
			return source.getBar().getFooBar();
		}
	}

	@Getter
	static class Foo {
		Bar bar;

		public Foo(Bar bar) {

		}
	}

	@Getter
	static class Bar {
		FooBar fooBar;
	}

	static class FooBar {

	}
}
