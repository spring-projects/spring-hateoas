/*
 * Copyright 2013-2020 the original author or authors.
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
package org.springframework.hateoas;

import lombok.experimental.UtilityClass;

/**
 * Static class to find out whether a relation type is defined by the IANA.
 *
 * @see https://www.iana.org/assignments/link-relations/link-relations.xhtml
 * @author Oliver Gierke
 * @author Roland Kulcsár
 * @author Greg Turnquist
 */
@UtilityClass
@Deprecated
public class IanaRels {

	/**
	 * Returns whether the given relation type is defined by the IANA.
	 *
	 * @param rel the relation type to check
	 * @return
	 * @deprecated Migrate to {@link IanaLinkRelations#isIanaRel(String)}.
	 */
	@Deprecated
	public static boolean isIanaRel(String rel) {
		return IanaLinkRelations.isIanaRel(rel);
	}
}
