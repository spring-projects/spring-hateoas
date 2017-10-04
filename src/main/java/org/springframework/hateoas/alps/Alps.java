/*
 * Copyright 2014-2016 the original author or authors.
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
package org.springframework.hateoas.alps;

import lombok.Builder;
import lombok.Value;

import java.util.List;

import org.springframework.hateoas.alps.Descriptor.DescriptorBuilder;
import org.springframework.hateoas.alps.Doc.DocBuilder;
import org.springframework.hateoas.alps.Ext.ExtBuilder;

/**
 * An ALPS document.
 * 
 * @author Oliver Gierke
 * @since 0.15
 * @see http://alps.io
 * @see http://alps.io/spec/#prop-alps
 */
@Value
@Builder(builderMethodName = "alps")
public class Alps {

	private final String version = "1.0";
	private final Doc doc;
	private final List<Descriptor> descriptors;

	/**
	 * Returns a new {@link DescriptorBuilder}.
	 * 
	 * @return
	 */
	public static DescriptorBuilder descriptor() {
		return Descriptor.builder();
	}

	/**
	 * Returns a new {@link DocBuilder}.
	 * 
	 * @return
	 */
	public static DocBuilder doc() {
		return Doc.builder();
	}

	/**
	 * Returns a new {@link ExtBuilder}.
	 * 
	 * @return
	 */
	public static ExtBuilder ext() {
		return Ext.builder();
	}
}
