/**
 *
 */
package com.mcleodmoores.xl4j.v1;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.lang.reflect.Method;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLObject;
import com.mcleodmoores.xl4j.v1.api.values.XLString;

/**
 * Units tests for {@link XLObject}.
 */
public class XLObjectTest {
  /** A handle */
  private static final long LONG_123 = 123L;
  /** A handle */
  private static final long LONG_1234 = 1234L;

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testXLObjectEqualsAndHashCode() {
    final XLObject xlStringArray = XLObject.of(String[].class, LONG_123);
    assertEquals(xlStringArray, xlStringArray);
    assertNotEquals(null, xlStringArray);
    final XLInteger xlInteger = XLInteger.of(0);
    assertNotEquals(xlInteger, xlStringArray);
    XLObject other = XLObject.of(String[].class, LONG_123);
    assertEquals(xlStringArray, other);
    assertEquals(xlStringArray.hashCode(), other.hashCode());
    other = XLObject.of(String[].class, LONG_1234);
    assertNotEquals(xlStringArray, other);
    other = XLObject.of(String.class, LONG_123);
    assertNotEquals(xlStringArray, other);
  }

  /**
   * Tests the getters, including checking that the number of getters is two.
   */
  @Test
  public void testGetters() {
    final XLObject xlStringArray = XLObject.of(String[].class.getSimpleName(), LONG_123);
    final Method[] methods = xlStringArray.getClass().getMethods();
    int count = 0;
    for (final Method method : methods) {
      if (method.getName().startsWith("get")) {
        count++;
      }
    }
    assertEquals(count, 3);
    assertEquals(xlStringArray.getClazz(), "String[]");
    assertEquals(xlStringArray.getHandle(), LONG_123);
  }

  /**
   * Tests the toString and toXLString methods.
   */
  @Test
  public void testToString() {
    final XLObject xlStringArray = XLObject.of(String[].class, LONG_1234);
    final String expected = "XLObject[class=String[], 1234]";
    assertEquals(xlStringArray.toString(), expected);
    final XLString expectedXLString = XLString.of('\u00BB' + "String[]-1234");
    assertEquals(xlStringArray.toXLString(), expectedXLString);
  }
}
