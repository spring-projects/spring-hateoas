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

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class UberMessageModelTest {

	private UberMessageModel uberMessageModel;

	private Map<String, String> map;

	@Before
	public void setUp() throws Exception {
		map = new HashMap<String, String>();
	}

	@Test
	public void hasVersionAndDataAndError() throws Exception {
		uberMessageModel = new UberMessageModel(map);
		assertEquals("1.0", uberMessageModel.getVersion());
		assertEquals(0, uberMessageModel.getData()
				.size());
		assertEquals(0, uberMessageModel.getError()
				.size());
	}

	@Test
	public void readsMapIntoData() throws Exception {
		map.put("name", "Doe");
		uberMessageModel = new UberMessageModel(map);
		assertEquals(1, uberMessageModel.getData().size());
	}
}
