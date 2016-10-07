package com.mcleodmoores.excel4j.typeconvert.converters;

import java.lang.reflect.Type;

import com.mcleodmoores.excel4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.excel4j.util.ArgumentChecker;
import com.mcleodmoores.excel4j.values.XLNumber;

/**
 * Type converter to convert from Longs to Excel Numbers and back again.
 */
public final class LongXLNumberTypeConverter extends AbstractTypeConverter {
  /**
   * Default constructor.
   */
  public LongXLNumberTypeConverter() {
    super(Long.class, XLNumber.class);
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return XLNumber.of((Long) from);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    return (long) ((XLNumber) from).getValue();
  }
}