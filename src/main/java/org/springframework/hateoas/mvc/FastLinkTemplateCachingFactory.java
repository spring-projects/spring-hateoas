package org.springframework.hateoas.mvc;

import org.springframework.hateoas.core.DummyInvocationUtils;
import org.springframework.hateoas.mvc.FastLinks.LastInvocationHolder;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class FastLinkTemplateCachingFactory {
	private Map<Key, FastLinkTemplate> templateCache = new ConcurrentHashMap<Key, FastLinkTemplate>();
	private FastLinkTemplateFactory linkFactory = new FastLinkTemplateFactory();

	private class Key {
		private Class<?> type;
		private Method method;

		Key(Class<?> type, Method method) {
			this.type = type;
			this.method = method;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			Key key = (Key) o;

			if (!type.equals(key.type)) return false;
			return method.equals(key.method);

		}

		@Override
		public int hashCode() {
			int result = type.hashCode();
			result = 31 * result + method.hashCode();
			return result;
		}
	}

	FastLinkTemplate createLinkTemplate(LastInvocationHolder invocations) {
		DummyInvocationUtils.MethodInvocation methodInvocation = invocations.getLastInvocation();
		Class<?> type = methodInvocation.getTargetType();
		Method method = methodInvocation.getMethod();

		Key key = new Key(type, method);

		if (templateCache.containsKey(key)) {
			return templateCache.get(key);
		} else {
			FastLinkTemplate linkTemplate = linkFactory.createLinkTemplate(invocations);
			templateCache.put(key, linkTemplate);
			return linkTemplate;
		}
	}
}
