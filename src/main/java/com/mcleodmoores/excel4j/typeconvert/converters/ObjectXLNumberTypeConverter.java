package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.util.Excel4JRuntimeException;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Type converter to convert from Objects to Excel Numbers and back again.
 */
public final class ObjectXLNumberTypeConverter extends AbstractTypeConverter {
  private static final int PRIORITY = 7;

  /**
   * Default constructor.
   */
  public ObjectXLNumberTypeConverter() {
    super(Object.class, XLNumber.class, PRIORITY);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (from instanceof Number) {
      Number num = (Number) from;
      return XLNumber.of(num.doubleValue());
    } else {
      throw new Excel4JRuntimeException("could not convert from " + from.getClass() + " to XLNumber");
    }
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (Double) ((XLNumber) from).getValue();
  }
}