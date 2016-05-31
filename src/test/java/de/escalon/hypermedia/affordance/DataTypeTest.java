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

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import de.escalon.hypermedia.spring.sample.test.EventStatusType;
import org.junit.Assert;
import org.junit.Test;

public class DataTypeTest {

	@Test
	public void testIsSingleValueTypeInt() throws Exception {
		Assert.assertTrue(DataType.isSingleValueType(int.class));
	}

	@Test
	public void testIsSingleValueType() throws Exception {
		Assert.assertTrue(DataType.isSingleValueType(int.class));
		Assert.assertTrue(DataType.isSingleValueType(Integer.class));
		Assert.assertTrue(DataType.isSingleValueType(BigInteger.class));
		Assert.assertTrue(DataType.isSingleValueType(BigDecimal.class));
		Assert.assertTrue(DataType.isSingleValueType(boolean.class));
		Assert.assertTrue(DataType.isSingleValueType(Boolean.class));
		Assert.assertTrue(DataType.isSingleValueType(EventStatusType.class));
		Assert.assertTrue(DataType.isSingleValueType(Date.class));
		Assert.assertTrue(DataType.isSingleValueType(Calendar.class));
		Assert.assertTrue(DataType.isSingleValueType(Currency.class));

		Assert.assertFalse(DataType.isSingleValueType(Object.class));
	}


	@Test
	public void testIsNumber() throws Exception {
		Assert.assertTrue(DataType.isNumber(int.class));
		Assert.assertTrue(DataType.isNumber(Integer.class));
	}

	@Test
	public void testIsBoolean() throws Exception {
		Assert.assertTrue(DataType.isBoolean(boolean.class));
		Assert.assertTrue(DataType.isBoolean(Boolean.class));
	}

	@Test
	public void testIsNumberBigDecimal() throws Exception {
		Assert.assertTrue(DataType.isNumber(BigDecimal.class));
	}


	@Test
	public void testIsCurrency() throws Exception {
		Assert.assertTrue(DataType.isCurrency(Currency.class));
	}

	@Test
	public void testAsTypeNumbers() {
		assertEquals(12, DataType.asType(int.class, "12"));
		assertEquals(12, DataType.asType(Integer.class, "12"));
		assertEquals(12L, DataType.asType(long.class, "12"));
		assertEquals(12L, DataType.asType(Long.class, "12"));
		assertEquals(12F, DataType.asType(float.class, "12"));
		assertEquals(12F, DataType.asType(Float.class, "12"));
		assertEquals(12D, DataType.asType(double.class, "12"));
		assertEquals(12D, DataType.asType(Double.class, "12"));
		assertEquals((byte) 12, DataType.asType(byte.class, "12"));
		assertEquals((byte) 12, DataType.asType(Byte.class, "12"));
		assertEquals((short) 12, DataType.asType(short.class, "12"));
		assertEquals((short) 12, DataType.asType(Short.class, "12"));
		assertEquals(BigInteger.valueOf(12), DataType.asType(BigInteger.class, "12"));
		assertEquals(BigDecimal.valueOf(12), DataType.asType(BigDecimal.class, "12"));
	}

	@Test
	public void testAsTypeEnum() {
		Object type = DataType.asType(EventStatusType.class, "EVENT_CANCELLED");
		assertEquals(EventStatusType.EVENT_CANCELLED, type);
	}

	@Test
	public void testAsTypeCurrency() {
		assertEquals(Currency.getInstance("EUR"), DataType.asType(Currency.class, "EUR"));
	}

	@Test
	public void testAsTypeString() {
		assertEquals("foo", DataType.asType(String.class, "foo"));
	}

	@Test
	public void isArrayOrCollection() {
		assertTrue(DataType.isArrayOrCollection(String[].class));
		assertTrue(DataType.isArrayOrCollection(List.class));
		assertTrue(DataType.isArrayOrCollection(Set.class));
		assertTrue(DataType.isArrayOrCollection(Collection.class));
		assertFalse(DataType.isArrayOrCollection(Map.class));
		assertFalse(DataType.isArrayOrCollection(Object.class));
	}

	@Test
	public void testAsTypeDateLong() {
		Date type = (Date) DataType.asType(Date.class, "1431762464134");
		assertEquals(1431762464134L, type.getTime());
	}

	@Test
	public void testAsTypeDateBoolean() {
		assertTrue((Boolean) DataType.asType(Boolean.class, "true"));
		assertTrue((Boolean) DataType.asType(Boolean.class, "TRUE"));
		assertTrue((Boolean) DataType.asType(Boolean.class, "True"));

		assertFalse((Boolean) DataType.asType(Boolean.class, "false"));
		assertFalse((Boolean) DataType.asType(Boolean.class, "FALSE"));
		assertFalse((Boolean) DataType.asType(Boolean.class, "False"));
		assertFalse((Boolean) DataType.asType(Boolean.class, "yes"));
		assertFalse((Boolean) DataType.asType(Boolean.class, "foo"));
		assertFalse((Boolean) DataType.asType(Boolean.class, null));
	}

	@Test
	public void testAsTypeDateISO8601() {
		Date type = (Date) DataType.asType(Date.class, "2015-05-16T07:47:44Z");
		assertEquals(1431762464000L, type.getTime());
	}

	@Test
	public void testAsTypeCalendarDateOnly() {
		Calendar type = (Calendar) DataType.asType(Calendar.class, "2015-05-16");
		assertEquals(2015, type.get(Calendar.YEAR));
		assertEquals(4, type.get(Calendar.MONTH));
		assertEquals(16, type.get(Calendar.DAY_OF_MONTH));
		assertEquals(0, type.get(Calendar.HOUR_OF_DAY));
		assertEquals(0, type.get(Calendar.MINUTE));
		assertEquals(TimeZone.getDefault(), type.getTimeZone());
	}

	@Test
	public void testAsTypeCalendarDateTime() {
		Calendar type = (Calendar) DataType.asType(Calendar.class, "2015-05-16T08:45:05");
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
		assertTrue(DataType.isIsoLatin1Number("1"));
		assertTrue(DataType.isIsoLatin1Number("-1"));
		assertTrue(DataType.isIsoLatin1Number("+1"));
		assertTrue(DataType.isIsoLatin1Number("123456789"));
		assertTrue(DataType.isIsoLatin1Number("-123456789"));
		assertTrue(DataType.isIsoLatin1Number("+123456789"));
		// not an iso latin 1 number
		assertFalse(DataType.isIsoLatin1Number("1+"));
		assertFalse(DataType.isIsoLatin1Number("1-"));
		assertFalse(DataType.isIsoLatin1Number("2.99792458e8"));
		assertFalse(DataType.isIsoLatin1Number("foo"));
		assertFalse(DataType.isIsoLatin1Number(""));
		assertFalse(DataType.isIsoLatin1Number(null));
	}
}