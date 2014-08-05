package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Type converter to convert from Floats to Excel Numbers and back again.
 */
public final class FloatXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public FloatXLNumberTypeConverter() {
    super(Float.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of((Float) from);
  }

  @Override
  public Object toJavaObject(final Class<?> expectedClass, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (float) ((XLNumber) from).getValue();
  }
}
