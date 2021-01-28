/*
 * Copyright 2020-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.problem;

import java.util.Collections;
import java.util.List;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.MediaType;

/**
 * {@link HypermediaMappingInformation} implementation to setup support for {@link Problem}.
 *
 * @author Oliver Drotbohm
 */
class HttpProblemDetailsMappingInformation implements HypermediaMappingInformation {

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getRootType()
	 */
	@Override
	public Class<?> getRootType() {
		return Problem.class;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.config.HypermediaMappingInformation#getMediaTypes()
	 */
	@Override
	public List<MediaType> getMediaTypes() {
		return Collections.singletonList(MediaTypes.HTTP_PROBLEM_DETAILS_JSON);
	}
}
