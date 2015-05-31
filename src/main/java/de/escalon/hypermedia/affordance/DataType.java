/*
 * Copyright (c) 2014. Escalon System-Entwicklung, Dietrich Schulten
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.escalon.hypermedia.affordance;

import javax.xml.bind.DatatypeConverter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;


/**
 * Distinguishes and creates data types, e.g. for serialization/deserialization.
 * Created by dschulten on 22.10.2014.
 */
public class DataType {

    /**
     * Determines if the given class holds only one data item. Can be useful to determine if a value should be rendered
     * as scalar.
     *
     * @param clazz to check
     * @return true if class is scalar
     */
    public static boolean isSingleValueType(Class<?> clazz) {
        boolean ret;
        if (isNumber(clazz)
                || isBoolean(clazz)
                || isString(clazz)
                || isEnum(clazz)
                || isDate(clazz)
                || isCalendar(clazz)
                || isCurrency(clazz)
                ) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;

    }

    public static boolean isArrayOrCollection(Class<?> parameterType) {
        return (parameterType.isArray() || Collection.class.isAssignableFrom(parameterType));
    }

    public static boolean isNumber(Class<?> clazz) {
        return (
                Number.class.isAssignableFrom(clazz) ||
                        int.class == clazz ||
                        long.class == clazz ||
                        float.class == clazz ||
                        byte.class == clazz ||
                        short.class == clazz ||
                        double.class == clazz
        );
    }

    public static boolean isInteger(Class<?> clazz) {
        return (Integer.class.isAssignableFrom(clazz) || int.class == clazz);
    }

    public static boolean isLong(Class<?> clazz) {
        return (Long.class.isAssignableFrom(clazz) || long.class == clazz);
    }

    public static boolean isFloat(Class<?> clazz) {
        return (Float.class.isAssignableFrom(clazz) || float.class == clazz);
    }

    public static boolean isDouble(Class<?> clazz) {
        return (Double.class.isAssignableFrom(clazz) || double.class == clazz);
    }

    public static boolean isByte(Class<?> clazz) {
        return (Byte.class.isAssignableFrom(clazz) || byte.class == clazz);
    }

    public static boolean isShort(Class<?> clazz) {
        return (Short.class.isAssignableFrom(clazz) || short.class == clazz);
    }

    public static boolean isBoolean(Class<?> clazz) {
        return (Boolean.class.isAssignableFrom(clazz) || boolean.class == clazz);
    }

    public static boolean isEnum(Class<?> clazz) {
        return Enum.class.isAssignableFrom(clazz);
    }

    public static boolean isString(Class<?> parameterType) {
        return String.class.isAssignableFrom(parameterType);
    }

    public static boolean isBigInteger(Class<?> clazz) {
        return BigInteger.class.isAssignableFrom(clazz);
    }

    public static boolean isBigDecimal(Class<?> clazz) {
        return BigDecimal.class.isAssignableFrom(clazz);
    }

    public static boolean isDate(Class<?> clazz) {
        return Date.class.isAssignableFrom(clazz);
    }

    public static boolean isCalendar(Class<?> clazz) {
        return Calendar.class.isAssignableFrom(clazz);
    }

    public static boolean isCurrency(Class<?> clazz) {
        return Currency.class.isAssignableFrom(clazz);
    }

    public static Object asType(Class<?> type, String string) {
        if (isBoolean(type)) {
            return Boolean.parseBoolean(string);
        } else if (isInteger(type)) {
            return Integer.parseInt(string);
        } else if (isLong(type)) {
            return Long.parseLong(string);
        } else if (isDouble(type)) {
            return Double.parseDouble(string);
        } else if (isFloat(type)) {
            return Float.parseFloat(string);
        } else if (isByte(type)) {
            return Byte.parseByte(string);
        } else if (isShort(type)) {
            return Short.parseShort(string);
        } else if (isBigInteger(type)) {
            return new BigInteger(string);
        } else if (isBigDecimal(type)) {
            return new BigDecimal(string);
        } else if (isCalendar(type)) {
            return DatatypeConverter.parseDateTime(string);
        } else if (isDate(type)) {
            if (isIsoLatin1Number(string)) {
                return new Date(Long.parseLong(string));
            } else {
                return DatatypeConverter.parseDateTime(string)
                        .getTime();
            }
        } else if (isCurrency(type)) {
            return Currency.getInstance(string);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, string);
        } else {
            return string;
        }
    }

    /**
     * Determines if the given string contains only 0-9 [ISO-LATIN-1] or an optional leading +/- sign.
     *
     * @param str to check
     * @return true if condition holds, false otherwise
     * @see <a href="http://stackoverflow.com/a/29331473/743507">Comparison of regex and char array performance</a>
     * @see Character#isDigit Examples for non-ISO-Latin-1-Digits
     */
    public static boolean isIsoLatin1Number(String str) {
        if (str == null)
            return false;
        char[] data = str.toCharArray();
        if (data.length == 0)
            return false;
        int index = 0;
        if (data.length > 1 && (data[0] == '-' || data[0] == '+'))
            index = 1;
        for (; index < data.length; index++) {
            if (data[index] < '0' || data[index] > '9')
                return false;
        }
        return true;
    }

}
