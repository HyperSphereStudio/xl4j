/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mcleodmoores.xl4j.javacode.ConstructorInvoker;
import com.mcleodmoores.xl4j.util.ArgumentChecker;
import com.mcleodmoores.xl4j.util.Excel4JRuntimeException;
import com.mcleodmoores.xl4j.values.XLError;
import com.mcleodmoores.xl4j.values.XLString;
import com.mcleodmoores.xl4j.values.XLValue;

/**
 * The default Excel call handler for constructors.
 */
public class DefaultExcelConstructorCallHandler implements ExcelConstructorCallHandler {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExcelConstructorCallHandler.class);
  /** The registry */
  private final FunctionRegistry _registry;

  /**
   * Create a default call handler.
   * 
   * @param registry
   *          the function and constructor registry, not null
   */
  public DefaultExcelConstructorCallHandler(final FunctionRegistry registry) {
    ArgumentChecker.notNull(registry, "registry");
    _registry = registry;
  }

  @Override
  public XLValue newInstance(final int exportNumber, final XLValue... args) {
    LOGGER.info("newInstance called with {}", exportNumber);
    for (int i = 0; i < args.length; i++) {
      LOGGER.info("arg = {}", args[i]);
      if (args[i] instanceof XLString) {
        final XLString xlString = (XLString) args[i];
        if (xlString.isXLObject()) {
          args[i] = xlString.toXLObject();
          LOGGER.info("converted arg to XLObject");
        }
      }
    }
    ConstructorInvoker constructorInvoker = null;
    try {
      // see if the object was registered with a constructor annotation
      final ConstructorDefinition constructorDefinition = _registry.getConstructorDefinition(exportNumber);
      LOGGER.info("constructorDefinition = {}", constructorDefinition.getConstructorMetadata().getConstructorSpec().name());
      constructorInvoker = constructorDefinition.getConstructorInvoker();
    } catch (final Excel4JRuntimeException e) {
      // see if the object was registered with a class annotation
      final ClassConstructorDefinition classDefinition = _registry.getClassConstructorDefinition(exportNumber);
      LOGGER.info("classConstructorDefinition = {}", classDefinition.getClassMetadata().getClassSpec().name());
      constructorInvoker = classDefinition.getConstructorInvoker();
    }
    if (constructorInvoker == null) {
      throw new Excel4JRuntimeException("Could not get constructor invoker for with export number " + exportNumber);
    }
    LOGGER.info("constructor invoker = {}", constructorInvoker.getDeclaringClass().getSimpleName());
    try {
      return constructorInvoker.newInstance(args);
    } catch (final Exception e) {
      LOGGER.info("Exception occurred while instantiating class, returning XLError: {}", e.getMessage());
      return XLError.Null;
    }
  }
}