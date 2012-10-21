package org.springframework.hateoas.util;

import java.util.ArrayList;
import java.util.List;


public class InvocationsImpl implements Invocations {

	List<Invocation> invocations = new ArrayList<Invocation>();

	@Override
	public List<Invocation> getInvocations() {
		return invocations;
	}

}