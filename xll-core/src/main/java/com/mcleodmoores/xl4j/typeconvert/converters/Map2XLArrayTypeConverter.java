/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.typeconvert.converters;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.Excel;
import com.mcleodmoores.xl4j.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.typeconvert.ExcelToJavaTypeMapping;
import com.mcleodmoores.xl4j.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.typeconvert.TypeConverterRegistry;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLArray;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * Type converter for maps to {@link XLArray}.
 */
public final class Map2XLArrayTypeConverter extends AbstractTypeConverter {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(Map2XLArrayTypeConverter.class);
  /** The priority */
  private static final int PRIORITY = 6;
  /** The Excel context */
  private final Excel _excel;

  /**
   * Default constructor.
   *
   * @param excel
   *          the excel context object, used to access the type converter
   *          registry, not null
   */
  public Map2XLArrayTypeConverter(final Excel excel) {
    super(Map.class, XLArray.class, PRIORITY);
    _excel =  ArgumentChecker.notNull(excel, "excel");
  }

  @Override
  public Object toXLValue(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    if (!Map.class.isAssignableFrom(from.getClass())) {
      throw new Excel4JRuntimeException("\"from\" parameter must be a Map");
    }
    final Map<?, ?> fromMap = (Map<?, ?>) from;
    if (fromMap.size() == 0) { // empty array
      return XLArray.of(new XLValue[1][1]);
    }
    // we know the length is > 0
    final XLValue[][] toArr = new XLValue[fromMap.size()][2];
    TypeConverter lastKeyConverter = null, lastValueConverter = null;
    Class<?> lastKeyClass = null, lastValueClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final Iterator<?> iter = fromMap.entrySet().iterator();
    for (int i = 0; i < fromMap.size(); i++) {
      final Map.Entry<?, ?> nextEntry = (Map.Entry<?, ?>) iter.next();
      final Object key = nextEntry.getKey();
      final Object value = nextEntry.getValue();
      // get converters for key and value
      if (lastKeyConverter == null || !key.getClass().equals(lastKeyClass)) {
        lastKeyClass = key.getClass();
        lastKeyConverter = typeConverterRegistry.findConverter(lastKeyClass);
      }
      if (lastValueConverter == null || !value.getClass().equals(lastValueClass)) {
        lastValueClass = value.getClass();
        lastValueConverter = typeConverterRegistry.findConverter(lastValueClass);
      }
      final XLValue xlKey = (XLValue) lastKeyConverter.toXLValue(null, key);
      final XLValue xlValue = (XLValue) lastValueConverter.toXLValue(null, value);
      toArr[i][0] = xlKey;
      toArr[i][1] = xlValue;
    }
    return XLArray.of(toArr);
  }

  @Override
  public Object toJavaObject(final Type expectedType, final Object from) {
    ArgumentChecker.notNull(from, "from");
    final XLArray xlArr = (XLArray) from;
    final Type keyType;
    final Type valueType;
    if (expectedType instanceof Class) {
      if (!Map.class.isAssignableFrom((Class<?>) expectedType)) {
        throw new Excel4JRuntimeException("expectedType is not a Map");
      }
      keyType = Object.class;
      valueType = Object.class;
    } else if (expectedType instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) expectedType;
      if (!(parameterizedType.getRawType() instanceof Class && Map.class.isAssignableFrom((Class<?>) parameterizedType.getRawType()))) {
        throw new Excel4JRuntimeException("expectedType is not a Map");
      }
      final Type[] typeArguments = parameterizedType.getActualTypeArguments();
      if (typeArguments.length == 2) {
        keyType = getBound(typeArguments[0]);
        valueType = getBound(typeArguments[1]);
      } else {
        throw new Excel4JRuntimeException("Could not get two type argument from " + expectedType);
      }
    } else {
      throw new Excel4JRuntimeException("expectedType not Class or ParameterizedType");
    }
    final XLValue[][] arr = xlArr.getArray();
    final Map<Object, Object> targetMap = new LinkedHashMap<>();
    TypeConverter lastKeyConverter = null, lastValueConverter = null;
    Class<?> lastKeyClass = null, lastValueClass = null;
    final TypeConverterRegistry typeConverterRegistry = _excel.getTypeConverterRegistry();
    final int n = arr.length == 2 ? arr[0].length : arr.length;
    for (int i = 0; i < n; i++) {
      final XLValue keyValue, valueValue;
      if (arr.length == 2) { // row
        keyValue = arr[0][i];
        valueValue = arr[1][i];
      } else { // column
        keyValue = arr[i][0];
        valueValue = arr[i][1];
      }
      // This is a rather weak attempt at optimizing converter lookup - other
      // options seemed to have greater overhead.
      if (lastKeyConverter == null || !keyValue.getClass().equals(lastKeyClass)) {
        lastKeyClass = keyValue.getClass();
        lastKeyConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastKeyClass, keyType));
        if (lastKeyConverter == null) {
          // TODO should we use conversion to Object here?
          throw new Excel4JRuntimeException("Could not find type converter for " + lastKeyClass + " using component type " + keyType);
        }
      }
      if (lastValueConverter == null || !valueValue.getClass().equals(lastValueClass)) {
        lastValueClass = valueValue.getClass();
        lastValueConverter = typeConverterRegistry.findConverter(ExcelToJavaTypeMapping.of(lastValueClass, valueType));
        if (lastValueConverter == null) {
          // TODO should we use conversion to Object here?
          throw new Excel4JRuntimeException("Could not find type converter for " + lastValueClass + " using component type " + valueType);
        }
      }
      final Object key = lastKeyConverter.toJavaObject(keyType, keyValue);
      final Object value = lastValueConverter.toJavaObject(valueType, valueValue);
      targetMap.put(key, value);
    }
    return targetMap;
  }

  private static Type getBound(final Type type) {
    if (type instanceof WildcardType) {
      final Type[] upperBounds = ((WildcardType) type).getUpperBounds();
      final Type[] lowerBounds = ((WildcardType) type).getLowerBounds();
      Type[] bounds;
      if (upperBounds.length > 0 && lowerBounds.length > 0) {
        // ? super X, so use the lower bound as it's the most specific
        bounds = lowerBounds;
      } else {
        bounds = lowerBounds.length > 0 ? lowerBounds : upperBounds;
      }
      switch (bounds.length) {
        case 0:
          return Object.class; //TODO is this possible?
        case 1:
          return bounds[0];
        default:
          // should never be reached
          LOGGER.warn("Map value parameter has multiple bounds, only considering first in conversion");
          return bounds[0];
      }
    }
    return type;
  }
}
