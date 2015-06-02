/*
 * Copyright (c) 2015. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.spring.uber;

import org.junit.Before;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UberActionTest {

	@Before
	public void setUp() throws Exception {

	}

	@Test
	public void translatesGetToNull() throws Exception {
		assertNull(UberAction.forRequestMethod(RequestMethod.GET));
	}

	@Test
	public void translatesPostToAppend() throws Exception {
		assertEquals(UberAction.APPEND, UberAction.forRequestMethod(RequestMethod.POST));
	}

	@Test
	public void translatesPutToReplace() throws Exception {
		assertEquals(UberAction.REPLACE, UberAction.forRequestMethod(RequestMethod.PUT));
	}
	
	@Test
	public void translatesDeleteToRemove() throws Exception {
		assertEquals(UberAction.REMOVE, UberAction.forRequestMethod(RequestMethod.DELETE));
	}

	@Test
	public void translatesPatchToPartial() throws Exception {
		assertEquals(UberAction.PARTIAL, UberAction.forRequestMethod(RequestMethod.PATCH));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void translateOptionsFails() throws Exception {
		UberAction.forRequestMethod(RequestMethod.OPTIONS);
	}

}
