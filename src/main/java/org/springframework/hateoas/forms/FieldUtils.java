package org.springframework.hateoas.forms;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.springframework.core.ResolvableType;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Utility class for searching fields of a class
 * 
 */
public class FieldUtils {
	public static final String DOT = ".";

	/**
	 * Attempt to find the {@link Class} of the {@link Field} on the supplied class by the especified path.
	 * 
	 * @param type
	 * @param path may be a nested path dot separated
	 * @return
	 */
	public static Class<?> findFieldClass(Class<?> type, String path) {
		if (path.contains(DOT)) {
			int firstDot = path.indexOf(DOT);
			String propertyName = path.substring(0, firstDot);
			Class<?> childType = findSimpleFieldClass(type, propertyName);
			if (childType == null) {
				throw new FieldNotFoundException(type, propertyName);
			}
			return findFieldClass(childType, path.substring(firstDot + 1, path.length()));
		}
		else {
			return findSimpleFieldClass(type, path);
		}
	}

	/**
	 * Attempt to find a {@link Field} declared in a {@link Class}. In first instance tries to find the getter
	 * {@link Method} of supplied fieldName. If getter is not present attempts to find for declared field.
	 * @param type
	 * @param fieldName
	 * @return
	 */
	private static Class<?> findSimpleFieldClass(Class<?> type, String fieldName) {

		Method method = ReflectionUtils.findMethod(type, "get" + StringUtils.capitalize(fieldName));
		ResolvableType resolvableType = null;
		if (null != method) {
			resolvableType = ResolvableType.forMethodReturnType(method);
		}
		else {
			try {
				resolvableType = ResolvableType.forField(type.getDeclaredField(fieldName));
			}
			catch (SecurityException e) {
				throw new FieldNotFoundException(type, fieldName);
			}
			catch (NoSuchFieldException e) {
				throw new FieldNotFoundException(type, fieldName);
			}
		}

		return getActualType(resolvableType);
	}

	/**
	 * Returns the {@link Class} of supplied {@link ResolvableType} considering that:
	 * 
	 * <ul>
	 * <li>- if resolvableType is an array, the component type is returned</li>
	 * <li>- if resolvableType is a {@link Collection}, generic parameter class is returned</li>
	 * <li>- if resolvableType is a {@link Map}, {@link NotSupportedException} is returned</li>
	 * <li>- otherwise resolvableType raw class is returned</li>
	 * </ul>
	 * 
	 * @param resolvableType
	 * @return
	 */
	private static Class<?> getActualType(ResolvableType resolvableType) {
		if (resolvableType.isArray()) {
			return resolvableType.getComponentType().resolve();
		}
		else if (resolvableType.asCollection() != ResolvableType.NONE) {
			return resolvableType.getGeneric(0).resolve();
		}
		else if (resolvableType.asMap() != ResolvableType.NONE) {
			throw new NotSupportedException(resolvableType.resolve() + " is not supported");
		}
		else {
			return resolvableType.resolve();
		}
	}
}
