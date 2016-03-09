/**
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.javacode;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.ExcelFactory;
import com.mcleodmoores.excel4j.testutil.TestObject;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 *
 */
public class JStaticMethodTest {
  private static final XLString CLASS = XLString.of("com.mcleodmoores.excel4j.testutil.TestObject");

  @Test
  public void testReturnType() {
    final Object methodResult1 = JMethod.jStaticMethod(XLString.of("java.lang.String"), XLString.of("valueOf"), XLBoolean.FALSE);
    final Object methodResult2 = JMethod.jStaticMethod(XLString.of("java.lang.Double"), XLString.of("valueOf"), XLString.of("3"));
    // expect reduction to xl types
    assertEquals(methodResult1, XLString.of("false"));
    assertEquals(methodResult2, XLNumber.of(3));
  }

  @Test
  public void testNoArgsConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), new XLValue[0]);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of();
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(1));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(1);
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testStringConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLString.of("2"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of("2");
    // could have gone through (Double)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testObjectConstructor() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(3));
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), number);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(Short.valueOf("3"));
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntStringConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(4), XLString.of("40"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(4, "40");
    // could go through (int, Object)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntDoubleConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(5), XLNumber.of(50));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(5, 50);
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testDoubleConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(6.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(6.);
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntObjectConstructor() {
    final XLValue number = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(70));
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(7), number);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(7, Short.valueOf("70"));
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testStringsConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLString.of("80"), XLString.of("81"), XLString.of("82"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of("80", "81", "82");
    // could have gone through (Object...) or (Double...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntStringsConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(9), XLString.of("90"), XLString.of("91"), XLString.of("92"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(9, new String[] {"90", "91", "92"});
    // could have gone through (Double...) or (int, Object...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testDoublesConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(100.), XLNumber.of(101.), XLNumber.of(102.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(100., 101., 102.);
    // could have gone through (Object...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntObjectsConstructor() {
    final XLValue number1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(110));
    final XLValue number2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(111));
    final XLValue number3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(112));
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(11), number1, number2, number3);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(11, new Object[] {(short) 110, 111L, 112F});
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testIntDoublesConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), XLNumber.of(12), XLNumber.of(120.), XLNumber.of(121.), XLNumber.of(122.));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(12, new Double[] {120., 121., 122.});
    // could have gone through (int, Object...), (Double...) or (int, Double...)
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testObjectsConstructor() {
    final XLValue number1 = JConstruct.jconstruct(XLString.of("java.lang.Short"), XLNumber.of(130));
    final XLValue number2 = JConstruct.jconstruct(XLString.of("java.lang.Long"), XLNumber.of(131));
    final XLValue number3 = JConstruct.jconstruct(XLString.of("java.lang.Float"), XLNumber.of(132));
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"), number1, number2, number3);
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(new Object[] {(short) 130, 131L, 132F});
    assertEquals(constructedObject, expectedObject);
  }

  @Test
  public void testStringArrayStringsConstructor() {
    final Object constructed = JMethod.jStaticMethod(CLASS, XLString.of("of"),
        XLArray.of(new XLValue[][] {new XLValue[] {XLString.of("14")}}), XLString.of("140"), XLString.of("141"));
    assertTrue(constructed instanceof XLObject);
    final XLObject constructedXlObject = (XLObject) constructed;
    final Object constructedObject = ExcelFactory.getInstance().getHeap().getObject(constructedXlObject.getHandle());
    assertTrue(constructedObject instanceof TestObject);
    final TestObject expectedObject = TestObject.of(new String[] {"14"}, new String[] {"140", "141"});
    assertEquals(constructedObject, expectedObject);
  }
}