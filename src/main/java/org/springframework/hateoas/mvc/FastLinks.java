package org.springframework.hateoas.mvc;

import org.springframework.hateoas.core.DummyInvocationUtils.LastInvocationAware;
import org.springframework.hateoas.core.DummyInvocationUtils.MethodInvocation;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FastLinks {
	private static FastLinkTemplateCachingFactory LINK_FACTORY = new FastLinkTemplateCachingFactory();

	private FastLinks() {
	}

	/**
	 * Simple bean for storing last invocation but without proxy overhead.
	 */
	static class LastInvocationHolder {
		private final MethodInvocation lastInvocation;
		private final List<Object> objectParameters;

		LastInvocationHolder(LastInvocationAware original) {
			lastInvocation = original.getLastInvocation();
			objectParameters = toList(original.getObjectParameters());
		}

		private List<Object> toList(Iterator<Object> ite) {
			List<Object> result = new ArrayList<Object>();
			while(ite.hasNext()) {
				result.add(ite.next());
			}
			return result;
		}

		List<Object> getObjectParameters() {
			return objectParameters;
		}

		MethodInvocation getLastInvocation() {
			return lastInvocation;
		}
	}

	public static String linkTo(Object invocationValue) {
		Assert.isInstanceOf(LastInvocationAware.class, invocationValue);
		LastInvocationHolder invocations = new LastInvocationHolder((LastInvocationAware) invocationValue);

		FastLinkTemplate linkTemplate = LINK_FACTORY.createLinkTemplate(invocations);
		return linkTemplate.build(invocations);
	}

}
