/*
 * Copyright 2017-2021 the original author or authors.
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
package org.springframework.hateoas.mediatype.hal.forms;

import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.AffordanceModelFactory;
import org.springframework.hateoas.mediatype.ConfiguredAffordance;
import org.springframework.http.MediaType;

/**
 * Factory for creating {@link HalFormsAffordanceModel}s.
 *
 * @author Greg Turnquist
 * @author Oliver Gierke
 */
class HalFormsAffordanceModelFactory implements AffordanceModelFactory {

	private final MediaType mediaType = MediaTypes.HAL_FORMS_JSON;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.hateoas.mediatype.AffordanceModelFactory#getAffordanceModel(org.springframework.hateoas.mediatype.ConfiguredAffordance)
	 */
	@Override
	public AffordanceModel getAffordanceModel(ConfiguredAffordance configured) {
		return new HalFormsAffordanceModel(configured);
	}

	public MediaType getMediaType() {
		return this.mediaType;
	}
}
