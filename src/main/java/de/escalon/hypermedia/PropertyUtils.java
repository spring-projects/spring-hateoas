package de.escalon.hypermedia;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience methods to work with Java bean properties.
 * Created by Dietrich on 11.03.2015.
 */
public class PropertyUtils {

	private PropertyUtils() {

	}

	public static Object getPropertyValue(Object currentCallValue, PropertyDescriptor propertyDescriptor) {
		Object propertyValue = null;
		if (currentCallValue != null && propertyDescriptor.getReadMethod() != null) {
			try {
				propertyValue = propertyDescriptor.getReadMethod()
						.invoke(currentCallValue);
			} catch (Exception e) {
				throw new RuntimeException("failed to read property from call value", e);
			}
		}
		return propertyValue;
	}

	public static Map<String, PropertyDescriptor> getPropertyDescriptors(Object bean) {
		try {
			PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(bean.getClass())
					.getPropertyDescriptors();
			Map<String, PropertyDescriptor> ret = new HashMap<String, PropertyDescriptor>();
			for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
				ret.put(propertyDescriptor.getName(), propertyDescriptor);
			}
			return ret;
		} catch (IntrospectionException e) {
			throw new RuntimeException("failed to get property descriptors of bean " + bean, e);
		}
	}
}
