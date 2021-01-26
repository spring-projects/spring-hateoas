/*
 * Copyright 2021 the original author or authors.
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
package org.springframework.hateoas.config;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * A {@link Comparator} over {@link HypermediaMappingInformation} to sort them by the appearance of the
 * {@link MediaType}s configured. I.e. given the media types UBER and HAL FORMS, the
 * {@link HypermediaMappingInformation} supporting the former will be ordered before the one for the latter.
 *
 * @author Oliver Drotbohm
 */
class HypermediaMappingInformationComparator implements Comparator<HypermediaMappingInformation> {

	private final List<MediaType> mediaTypes;

	/**
	 * Creates a new {@link HypermediaMappingInformationComparator} using the given reference {@link MediaType}s.
	 *
	 * @param mediaTypes must not be {@literal null}.
	 */
	HypermediaMappingInformationComparator(List<MediaType> mediaTypes) {

		Assert.notEmpty(mediaTypes, "MediaTypes must not be empty!");

		this.mediaTypes = mediaTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(@Nullable HypermediaMappingInformation left, @Nullable HypermediaMappingInformation right) {

		for (MediaType mediaType : mediaTypes) {

			boolean leftSupports = left != null && left.getMediaTypes().contains(mediaType);
			boolean rightSupports = right != null && right.getMediaTypes().contains(mediaType);

			if (leftSupports && !rightSupports) {
				return -1;
			}

			if (!leftSupports && rightSupports) {
				return 1;
			}
		}

		return 0;
	}
}
