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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import de.escalon.hypermedia.affordance.ActionDescriptor;
import org.springframework.hateoas.Link;

public abstract class AbstractUberNode implements Iterable<UberNode> {

	protected List<UberNode> data = new ArrayList<UberNode>();

	public AbstractUberNode() {
		super();
	}

	@JsonInclude(Include.NON_EMPTY)
	public List<UberNode> getData() {
		return data;
	}

	public void addData(UberNode item) {
		data.add(item);
	}

	public void addLink(Link link) {
		List<ActionDescriptor> actionDescriptors = UberUtils.getActionDescriptors(link);
		List<String> rels = UberUtils.getRels(link);
		for (ActionDescriptor actionDescriptor : actionDescriptors) {
			UberNode uberLink = UberUtils.toUberLink(link.getHref(), actionDescriptor, rels);
			data.add(uberLink);
		}
	}

	public void addLinks(Iterable<Link> links) {
		for (Link link : links) {
			addLink(link);
		}
	}

	/**
	 * Gets first child of this uber node having the given name attribute.
	 *
	 * @param name to look up
	 * @return found child or null
	 */
	public UberNode getFirstByName(String name) {
		// TODO consider less naive impl
		UberNode ret = null;
		for (UberNode node : data) {
			if (name.equals(node.getName())) {
				ret = node;
				break;
			}
		}
		return ret;
	}

	/**
	 * Gets first child of this uber node having the given rel attribute.
	 *
	 * @param rel to look up
	 * @return found child or null
	 */
	public UberNode getFirstByRel(String rel) {
		// TODO consider less naive impl
		for (UberNode node : data) {
			List<String> myRels = node.getRel();
			if (myRels != null) {
				for (String myRel : myRels) {
					if (rel.equals(myRel)) {
						return node;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Allows iterating over children of this uber node which have a data attribute.
	 */
	@Override
	public Iterator<UberNode> iterator() {

		return new Iterator<UberNode>() {

			int index = 0;

			@Override
			public void remove() {
				throw new UnsupportedOperationException("removing from uber node is not supported");
			}

			@Override
			public UberNode next() {
				index = findNextChildWithData();
				return data.get(index++);
			}

			@Override
			public boolean hasNext() {
				return findNextChildWithData() != -1;
			}

			private int findNextChildWithData() {
				for (int i = index; i < data.size(); i++) {
					if (!data.get(i)
							.getData()
							.isEmpty()) {
						return i;
					}
				}
				return -1;
			}
		};
	}
}
