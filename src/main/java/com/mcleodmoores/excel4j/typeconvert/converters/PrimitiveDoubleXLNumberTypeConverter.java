package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Type converter to convert from doubles to Excel Numbers and back again.
 */
public final class PrimitiveDoubleXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public PrimitiveDoubleXLNumberTypeConverter() {
    super(Double.TYPE, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of((Double) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (double) ((XLNumber) from).getValue();
  }
}
