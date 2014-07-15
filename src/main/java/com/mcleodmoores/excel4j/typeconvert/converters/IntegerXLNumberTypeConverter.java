package com.mcleodmoores.excel4j.typeconvert.converters;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLValue;

/**
 * Type converter to convert from integers to Excel Numbers and back again.
 */
public final class IntegerXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public IntegerXLNumberTypeConverter() {
    super(Integer.class, XLNumber.class);
  }

  @Override
  public XLValue toXLValue(final Object from) {
    return XLNumber.of((Integer) from);
  }

  @Override
  public Object toJavaObject(final XLValue from) {
    return (int) ((XLNumber) from).getValue();
  }
}
