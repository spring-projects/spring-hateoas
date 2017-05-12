/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.hateoas.affordance.support;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.hateoas.affordance.springmvc.sample.test.EventStatusType;

public class DataTypeUtilsTest {

	@Test
	public void testIsSingleValueTypeInt() throws Exception {
		Assert.assertTrue(DataTypeUtils.isSingleValueType(int.class));
	}

	@Test
	public void testIsSingleValueType() throws Exception {

		Assert.assertTrue(DataTypeUtils.isSingleValueType(int.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(Integer.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(BigInteger.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(BigDecimal.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(boolean.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(Boolean.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(EventStatusType.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(Date.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(Calendar.class));
		Assert.assertTrue(DataTypeUtils.isSingleValueType(Currency.class));
		Assert.assertFalse(DataTypeUtils.isSingleValueType(Object.class));
	}

	@Test
	public void testIsNumber() throws Exception {

		Assert.assertTrue(DataTypeUtils.isNumber(int.class));
		Assert.assertTrue(DataTypeUtils.isNumber(Integer.class));
	}

	@Test
	public void testIsBoolean() throws Exception {

		Assert.assertTrue(DataTypeUtils.isBoolean(boolean.class));
		Assert.assertTrue(DataTypeUtils.isBoolean(Boolean.class));
	}

	@Test
	public void testIsNumberBigDecimal() throws Exception {
		Assert.assertTrue(DataTypeUtils.isNumber(BigDecimal.class));
	}


	@Test
	public void testIsCurrency() throws Exception {
		Assert.assertTrue(DataTypeUtils.isCurrency(Currency.class));
	}

	@Test
	public void testAsTypeNumbers() {

		assertEquals(12, DataTypeUtils.asType(int.class, "12"));
		assertEquals(12, DataTypeUtils.asType(Integer.class, "12"));
		assertEquals(12L, DataTypeUtils.asType(long.class, "12"));
		assertEquals(12L, DataTypeUtils.asType(Long.class, "12"));
		assertEquals(12F, DataTypeUtils.asType(float.class, "12"));
		assertEquals(12F, DataTypeUtils.asType(Float.class, "12"));
		assertEquals(12D, DataTypeUtils.asType(double.class, "12"));
		assertEquals(12D, DataTypeUtils.asType(Double.class, "12"));
		assertEquals((byte) 12, DataTypeUtils.asType(byte.class, "12"));
		assertEquals((byte) 12, DataTypeUtils.asType(Byte.class, "12"));
		assertEquals((short) 12, DataTypeUtils.asType(short.class, "12"));
		assertEquals((short) 12, DataTypeUtils.asType(Short.class, "12"));
		assertEquals(BigInteger.valueOf(12), DataTypeUtils.asType(BigInteger.class, "12"));
		assertEquals(BigDecimal.valueOf(12), DataTypeUtils.asType(BigDecimal.class, "12"));
	}

	@Test
	public void testAsTypeEnum() {

		Object type = DataTypeUtils.asType(EventStatusType.class, "EVENT_CANCELLED");
		assertEquals(EventStatusType.EVENT_CANCELLED, type);
	}

	@Test
	public void testAsTypeCurrency() {
		assertEquals(Currency.getInstance("EUR"), DataTypeUtils.asType(Currency.class, "EUR"));
	}

	@Test
	public void testAsTypeString() {
		assertEquals("foo", DataTypeUtils.asType(String.class, "foo"));
	}

	@Test
	public void isArrayOrCollection() {

		assertTrue(DataTypeUtils.isArrayOrIterable(String[].class));
		assertTrue(DataTypeUtils.isArrayOrIterable(List.class));
		assertTrue(DataTypeUtils.isArrayOrIterable(Set.class));
		assertTrue(DataTypeUtils.isArrayOrIterable(Collection.class));
		assertFalse(DataTypeUtils.isArrayOrIterable(Map.class));
		assertFalse(DataTypeUtils.isArrayOrIterable(Object.class));
	}

	@Test
	public void testAsTypeDateLong() {

		Date type = (Date) DataTypeUtils.asType(Date.class, "1431762464134");
		assertEquals(1431762464134L, type.getTime());
	}

	@Test
	public void testAsTypeDateBoolean() {

		assertTrue((Boolean) DataTypeUtils.asType(Boolean.class, "true"));
		assertTrue((Boolean) DataTypeUtils.asType(Boolean.class, "TRUE"));
		assertTrue((Boolean) DataTypeUtils.asType(Boolean.class, "True"));

		assertFalse((Boolean) DataTypeUtils.asType(Boolean.class, "false"));
		assertFalse((Boolean) DataTypeUtils.asType(Boolean.class, "FALSE"));
		assertFalse((Boolean) DataTypeUtils.asType(Boolean.class, "False"));
		assertFalse((Boolean) DataTypeUtils.asType(Boolean.class, "yes"));
		assertFalse((Boolean) DataTypeUtils.asType(Boolean.class, "foo"));
		assertFalse((Boolean) DataTypeUtils.asType(Boolean.class, null));
	}

	@Test
	public void testAsTypeDateISO8601() {

		Date type = (Date) DataTypeUtils.asType(Date.class, "2015-05-16T07:47:44Z");
		assertEquals(1431762464000L, type.getTime());
	}

	@Test
	public void testAsTypeCalendarDateOnly() {

		Calendar type = (Calendar) DataTypeUtils.asType(Calendar.class, "2015-05-16");

		assertEquals(2015, type.get(Calendar.YEAR));
		assertEquals(4, type.get(Calendar.MONTH));
		assertEquals(16, type.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, type.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, type.get(Calendar.MINUTE));
		assertEquals(TimeZone.getDefault(), type.getTimeZone());
	}

	@Test
	public void testAsTypeCalendarDateTime() {

		Calendar type = (Calendar) DataTypeUtils.asType(Calendar.class, "2015-05-16T08:45:05");

		assertEquals(2015, type.get(Calendar.YEAR));
		assertEquals(4, type.get(Calendar.MONTH));
		assertEquals(16, type.get(Calendar.DAY_OF_MONTH));
		assertEquals(8, type.get(Calendar.HOUR_OF_DAY));
		assertEquals(45, type.get(Calendar.MINUTE));
		assertEquals(5, type.get(Calendar.SECOND));
		assertEquals(TimeZone.getDefault(), type.getTimeZone());
	}


	@Test
	public void testIsIsoLatin1Number() throws Exception {

		assertTrue(DataTypeUtils.isIsoLatin1Number("1"));
		assertTrue(DataTypeUtils.isIsoLatin1Number("-1"));
		assertTrue(DataTypeUtils.isIsoLatin1Number("+1"));
		assertTrue(DataTypeUtils.isIsoLatin1Number("123456789"));
		assertTrue(DataTypeUtils.isIsoLatin1Number("-123456789"));
		assertTrue(DataTypeUtils.isIsoLatin1Number("+123456789"));

		// not an iso latin 1 number
		assertFalse(DataTypeUtils.isIsoLatin1Number("?432"));
		assertFalse(DataTypeUtils.isIsoLatin1Number("1+"));
		assertFalse(DataTypeUtils.isIsoLatin1Number("1-"));
		assertFalse(DataTypeUtils.isIsoLatin1Number("2.99792458e8"));
		assertFalse(DataTypeUtils.isIsoLatin1Number("foo"));
		assertFalse(DataTypeUtils.isIsoLatin1Number(""));
		assertFalse(DataTypeUtils.isIsoLatin1Number(null));
	}
}