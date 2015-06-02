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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName("uber")
public class UberMessageModel extends AbstractUberNode {

	private String version = "1.0";

	private List<UberNode> error = new ArrayList<UberNode>();

	public UberMessageModel(Object toWrap) {
		UberUtils.toUberData(this, toWrap);
	}

	public String getVersion() {
		return version;
	}

	@JsonInclude(Include.NON_EMPTY)
	public List<UberNode> getError() {
		return error;
	}

}
