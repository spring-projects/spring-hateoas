package de.escalon.hypermedia.spring;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.escalon.hypermedia.affordance.ActionInputParameter;

/**
 * Default documentation provider, always returns null as documentation url.
 * Created by Dietrich on 04.04.2015.
 */
public class DefaultDocumentationProvider implements DocumentationProvider {


	@Override
	public String getDocumentationUrl(ActionInputParameter annotatedParameter, Object content) {
		return null;
	}

	@Override
	public String getDocumentationUrl(Field field, Object content) {
		return null;
	}

	@Override
	public String getDocumentationUrl(Method method, Object content) {
		return null;
	}

	@Override
	public String getDocumentationUrl(Class clazz, Object content) {
		return null;
	}

	@Override
	public String getDocumentationUrl(String name, Object content) {
		return null;
	}
}
