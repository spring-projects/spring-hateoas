package de.escalon.hypermedia;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Convenience methods to work with Java bean properties.
 *
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

    public static Constructor findDefaultCtor(Constructor[] constructors) {
        // TODO duplicate on HtmlResourceMessageConverter
        Constructor constructor = null;
        for (Constructor ctor : constructors) {
            if (ctor.getParameterTypes().length == 0) {
                constructor = ctor;
            }
        }
        return constructor;
    }

    public static Constructor findJsonCreator(Constructor[] constructors, Class creatorAnnotation) {
        Constructor constructor = null;
        for (Constructor ctor : constructors) {
            if (AnnotationUtils.findAnnotation(ctor, creatorAnnotation) != null) {
                constructor = ctor;
                break;
            }
        }
        return constructor;
    }

    // TODO move to PropertyUtil and remove current method for propertyDescriptors, cache search results
    public static Object getPropertyOrFieldValue(Object currentCallValue, String propertyOrFieldName) {
        if (currentCallValue == null) {
            return null;
        }
        Object propertyValue = getBeanPropertyValue(currentCallValue, propertyOrFieldName);
        if (propertyValue == null) {
            propertyValue = getFieldValue(currentCallValue, propertyOrFieldName);
        }
        return propertyValue;
    }

    public static Object getFieldValue(Object currentCallValue, String fieldName) {
        try {
            Class<?> beanType = currentCallValue.getClass();
            Object propertyValue = null;
            Field[] fields = beanType.getFields();
            for (Field field : fields) {
                if (fieldName.equals(field.getName())) {
                    propertyValue = field.get(currentCallValue);
                    break;
                }
            }
            return propertyValue;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read field " + fieldName + " from " + currentCallValue.toString(), e);
        }
    }

    public static Object getBeanPropertyValue(Object currentCallValue, String paramName) {
        if (currentCallValue == null) {
            return null;
        }
        try {
            Object propertyValue = null;
            BeanInfo info = Introspector.getBeanInfo(currentCallValue.getClass());
            PropertyDescriptor[] pds = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                if (paramName.equals(pd.getName())) {
                    Method readMethod = pd.getReadMethod();
                    if (readMethod != null) {
                        propertyValue = readMethod.invoke(currentCallValue);
                    }
                    break;
                }
            }
            return propertyValue;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read property " + paramName + " from " + currentCallValue.toString
                    (), e);
        }
    }

}
