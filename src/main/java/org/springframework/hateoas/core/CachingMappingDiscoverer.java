package org.springframework.hateoas.core;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachingMappingDiscoverer implements MappingDiscoverer {
	private Map<String, String> mappingCache = new ConcurrentHashMap<String, String>();
	private MappingDiscoverer discoverer;

	public CachingMappingDiscoverer(MappingDiscoverer discoverer) {
		this.discoverer = discoverer;
	}

	@Override
	public String getMapping(final Class<?> type) {
		String key = key(type, null);
		return getMapping(key, new CachedCall() {
			@Override
			public String getMapping() {
				return discoverer.getMapping(type);
			}
		});
	}

	@Override
	public String getMapping(final Method method) {
		String key = key(method.getDeclaringClass(), method);
		return getMapping(key, new CachedCall() {
			@Override
			public String getMapping() {
				return discoverer.getMapping(method);
			}
		});
	}

	@Override
	public String getMapping(final Class<?> type, final Method method) {
		String key = key(type, method);
		return getMapping(key, new CachedCall() {
			@Override
			public String getMapping() {
				return discoverer.getMapping(type, method);
			}
		});
	}

	public String getMapping(String key, CachedCall cachedCall) {
		if (mappingCache.containsKey(key)) {
			return mappingCache.get(key);
		} else {
			String mapping = cachedCall.getMapping();
			mappingCache.put(key, mapping);
			return mapping;
		}
	}

	private interface CachedCall {
		String getMapping();
	}

	private String key(Class<?> type, Method method) {
		StringBuilder buf = new StringBuilder();
		buf.append(type.getName());
		if (method != null) {
			buf.append(method.getName());
			for (Class<?> par: method.getParameterTypes()) {
				buf.append(par.getName());
			}
		}
		return buf.toString();
	}
}
