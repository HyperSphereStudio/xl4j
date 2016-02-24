/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.excel4j.typeconvert.converters;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.excel4j.typeconvert.JavaToExcelTypeMapping;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLInteger;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Unit tests for {@link DoubleXLNumberTypeConverter}.
 */
@Test
public class DoubleXLNumberTypeConverterTest extends TypeConverterTests {
  /** The expected priority */
  private static final int DEFAULT_PRIORITY = 10;
  /** Integer */
  private static final int TEN_I = 10;
  /** Double */
  private static final double TEN_D = 10d;
  // REVIEW isn't it a bit odd that there's no complaint when there's an upcast to Double?
  /** XLNumber holding a double. */
  private static final XLNumber XL_NUMBER_DOUBLE = XLNumber.of(10.);
  /** XLNumber holding a long. */
  private static final XLNumber XL_NUMBER_LONG = XLNumber.of(10L);
  /** XLNumber holding an int. */
  private static final XLNumber XL_NUMBER_INT = XLNumber.of(10);
  /** Double. */
  private static final Double DOUBLE = 10.;
  /** The converter. */
  private static final AbstractTypeConverter CONVERTER = new DoubleXLNumberTypeConverter();
  /** The class name */
  private static final String CLASSNAME = "java.lang.Double";

  /**
   * Tests that the java type is {@link Double}.
   */
  @Test
  public void testGetExcelToJavaTypeMapping() {
    assertEquals(CONVERTER.getExcelToJavaTypeMapping(), ExcelToJavaTypeMapping.of(XLNumber.class, Double.class));
  }

  /**
   * Tests that the excel type is {@link XLNumber}.
   */
  @Test
  public void testGetJavaToExcelTypeMapping() {
    assertEquals(CONVERTER.getJavaToExcelTypeMapping(), JavaToExcelTypeMapping.of(Double.class, XLNumber.class));
  }

  /**
   * Tests the expected priority.
   */
  @Test
  public void testPriority() {
    assertEquals(CONVERTER.getPriority(), DEFAULT_PRIORITY);
  }

  /**
   * Tests that passing in a null expected {@link XLValue} is successful.
   */
  @Test
  public void testNullExpectedXLValueClass() {
    CONVERTER.toXLValue(null, DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullObject() {
    CONVERTER.toXLValue(XLNumber.class, null);
  }

  /**
   * Tests that passing in a null expected Java class is successful.
   */
  @Test
  public void testNullExpectedClass() {
    CONVERTER.toJavaObject(null, XL_NUMBER_DOUBLE);
  }

  /**
   * Tests that passing in a null object gives the expected exception.
   */
  @Test(expectedExceptions = Excel4JRuntimeException.class)
  public void testNullXLValue() {
    CONVERTER.toJavaObject(Double.class, null);
  }

  /**
   * Tests for the exception when the object to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToJavaConversion() {
    CONVERTER.toJavaObject(Double.class, XLInteger.of(TEN_I));
  }

  /**
   * Tests that the expected type is ignored during conversions to Java.
   */
  @Test
  public void testWrongExpectedClassToJavaConversion() {
    assertEquals(CONVERTER.toJavaObject(BigDecimal.class, XLNumber.of(TEN_D)), DOUBLE);
  }

  /**
   * Tests for the exception when {@link XLValue} to convert is the wrong type.
   */
  @Test(expectedExceptions = ClassCastException.class)
  public void testWrongTypeToXLConversion() {
    CONVERTER.toXLValue(XLNumber.class, BigDecimal.valueOf(TEN_D));
  }

  /**
   * Tests that the expected type is ignored during conversion to a XL class.
   */
  @Test
  public void testWrongExpectedClassToXLConversion() {
    assertEquals(CONVERTER.toXLValue(XLBoolean.class, 1.), XLNumber.of(1));
  }

  /**
   * Tests the conversion from a {@link Double}.
   */
  @Test
  public void testConversionFromDouble() {
    final XLValue converted = (XLValue) CONVERTER.toXLValue(XL_NUMBER_DOUBLE.getClass(), DOUBLE);
    assertTrue(converted instanceof XLNumber);
    final XLNumber xlNumber = (XLNumber) converted;
    assertEquals(xlNumber.getValue(), TEN_D, 0);
  }

  /**
   * Tests the conversion from a {@link XLNumber}.
   */
  @Test
  public void testConversionFromXLNumber() {
    Object converted = CONVERTER.toJavaObject(Double.class, XL_NUMBER_INT);
    assertTrue(converted instanceof Double);
    Double doub = (Double) converted;
    assertEquals(doub, DOUBLE);
    converted = CONVERTER.toJavaObject(Double.class, XL_NUMBER_LONG);
    assertTrue(converted instanceof Double);
    doub = (Double) converted;
    assertEquals(doub, DOUBLE);
    converted = CONVERTER.toJavaObject(Double.class, XL_NUMBER_DOUBLE);
    assertTrue(converted instanceof Double);
    doub = (Double) converted;
    assertEquals(doub, DOUBLE);
  }

  /**
   * Tests creation of Double using its constructors.
   */
  @Test
  public void testJConstruct() {
    // no no-args constructor for Double
    XLValue xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME));
    assertTrue(xlValue instanceof XLError);
    // double constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    Object doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    Double doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue());
    // String constructor
    xlValue = PROCESSOR.invoke("JConstruct", XLString.of(CLASSNAME), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue());
  }

  /**
   * Tests creation of Double using its static constructors.
   */
  @Test
  public void testJMethod() {
    // Double.valueOf(double)
    XLValue xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XL_NUMBER_DOUBLE);
    assertTrue(xlValue instanceof XLObject);
    Object doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    Double doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue());
    // Double.valueOf(string)
    xlValue = PROCESSOR.invoke("JStaticMethodX", XLString.of(CLASSNAME), XLString.of("valueOf"), XLString.of("10"));
    assertTrue(xlValue instanceof XLObject);
    doubleObject = HEAP.getObject(((XLObject) xlValue).getHandle());
    assertTrue(doubleObject instanceof Double);
    doubleVal = (Double) doubleObject;
    assertEquals(doubleVal.doubleValue(), DOUBLE.doubleValue());
  }
}
