/**
 * Copyright (C) 2017 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.callback;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.mcleodmoores.xl4j.v1.api.annotations.FunctionType;
import com.mcleodmoores.xl4j.v1.api.annotations.TypeConversionMode;
import com.mcleodmoores.xl4j.v1.api.annotations.XLConstant;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunction;
import com.mcleodmoores.xl4j.v1.api.annotations.XLFunctions;
import com.mcleodmoores.xl4j.v1.api.annotations.XLParameter;
import com.mcleodmoores.xl4j.v1.api.core.ExcelFactory;
import com.mcleodmoores.xl4j.v1.api.core.FunctionDefinition;
import com.mcleodmoores.xl4j.v1.api.typeconvert.AbstractTypeConverter;
import com.mcleodmoores.xl4j.v1.api.typeconvert.TypeConverter;
import com.mcleodmoores.xl4j.v1.api.values.XLInteger;
import com.mcleodmoores.xl4j.v1.api.values.XLLocalReference;
import com.mcleodmoores.xl4j.v1.api.values.XLMultiReference;
import com.mcleodmoores.xl4j.v1.api.values.XLRange;
import com.mcleodmoores.xl4j.v1.api.values.XLSheetId;
import com.mcleodmoores.xl4j.v1.core.DefaultExcelCallback;
import com.mcleodmoores.xl4j.v1.core.FunctionMetadata;
import com.mcleodmoores.xl4j.v1.invoke.ObjectConstructorInvoker;
import com.mcleodmoores.xl4j.v1.invoke.ObjectFieldGetter;
import com.mcleodmoores.xl4j.v1.invoke.SimpleResultMethodInvoker;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.ObjectXLObjectTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerArrayXLArrayTypeConverter;
import com.mcleodmoores.xl4j.v1.typeconvert.converters.PrimitiveIntegerXLNumberTypeConverter;
import com.mcleodmoores.xl4j.v1.util.XL4JRuntimeException;
import com.mcleodmoores.xl4j.v1.xll.NativeExcelFunctionEntryAccumulator;
import com.mcleodmoores.xl4j.v1.xll.NativeExcelFunctionEntryAccumulator.FunctionEntry;

/**
 * Unit tests for {@link DefaultExcelCallback}.
 */
public class DefaultExcelCallbackTest {
  private static final int INT = 100;
  private static final XLRange XL_RANGE = XLRange.of(0, 10, 0, 11);
  private static final XLLocalReference XL_LOCAL_REF = XLLocalReference.of(XL_RANGE);
  private static final XLMultiReference XL_MULTI_REF = XLMultiReference.of(XLSheetId.of(1), Arrays.asList(XL_RANGE));
  private static final NativeExcelFunctionEntryAccumulator FUNCTION_REGISTRY = new NativeExcelFunctionEntryAccumulator();
  private static final DefaultExcelCallback CALLBACK = new DefaultExcelCallback(FUNCTION_REGISTRY);
  private static int s_exportNumber = 0;

  /**
   * Tests that a XLFunction annotation cannot be used for a field.
   * @throws SecurityException  if the field is not found
   * @throws NoSuchFieldException  if the field is not found
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testFunctionAnnotationForField() throws NoSuchFieldException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    parameters[0] = createXLParameter(null, null, null, null);
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, null, null, null, null, null, null, null), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Field field = DefaultExcelCallbackTest.class.getDeclaredField("INT");
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectFieldGetter(field, new PrimitiveIntegerXLNumberTypeConverter()), exportNumber));
  }

  /**
   * Checks that a function can't be volatile and multi-thread safe.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongCombinationOfAttributes1() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    parameters[0] = createXLParameter(null, null, null, null);
    final Boolean isVolatile = true;
    final Boolean isMultiThreadSafe = true;
    final Boolean isMacroEquivalent = true;
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, isVolatile, isMultiThreadSafe, isMacroEquivalent,
            FunctionType.FUNCTION, false, false, false, false, false), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
  }

  /**
   * Checks that a function can't be macro-equivalent and multi-thread safe.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongCombinationOfAttributes2() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    parameters[0] = createXLParameter(null, null, null, null);
    final Boolean isVolatile = false;
    final Boolean isMultiThreadSafe = true;
    final Boolean isMacroEquivalent = true;
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, isVolatile, isMultiThreadSafe, isMacroEquivalent,
            FunctionType.FUNCTION, false, false, false, false, false), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
  }

  /**
   * Tests that a command must return a XLInteger.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testWrongCommandReturnType() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    parameters[0] = createXLParameter(null, null, null, null);
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, null,
            FunctionType.COMMAND, null, null, null, null, null), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
  }

  /**
   * Tests that reference type parameters can only be supplied to macro-equivalent functions.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testReferenceTypeIsMacroEquivalent() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    parameters[0] = createXLParameter(null, null, null, true);
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, false,
            FunctionType.FUNCTION, null, null, null, null, null), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
  }

  /**
   * Tests that the number of parameter annotations, if non-zero, must match the number of parameters.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test(expectedExceptions = XL4JRuntimeException.class)
  public void testNumberOfParameterAnnotations() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[2];
    parameters[0] = createXLParameter(null, null, null, null);
    parameters[1] = createXLParameter(null, null, null, null);
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, null,
            FunctionType.FUNCTION, null, null, null, null, null), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
  }

  /**
   * Tests that parameter annotations are not required.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testNoParameterAnnotations() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[0];
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunctions(null, null, null, null, null, null, null, null, null, null, null, null, null), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    final FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[0]);
    assertEquals(entry._argumentNames, "");
    assertEquals(entry._description, "");
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, functionName);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ$");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, "");
    assertFalse(entry._isAutoAsynchronous);
    assertFalse(entry._isCallerRequired);
    assertFalse(entry._isLongRunning);
    assertFalse(entry._isManualAsynchronous);
    assertTrue(entry._isVarArgs);
  }
  /**
   * Tests a command.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testCommand() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    final String parameterName = "parameterName";
    final String parameterDescription = "parameterDescription";
    final Boolean optional = true;
    final Boolean referenceType = true;
    parameters[0] = createXLParameter(parameterName, parameterDescription, optional, referenceType);
    final String name = "name";
    final String category = "category";
    final String description = "description";
    final String helpTopic = "helpTopic";
    final Boolean isVolatile = true;
    final Boolean isMultiThreadSafe = false;
    final Boolean isMacroEquivalent = true;
    final Boolean isLongRunning = true;
    final Boolean isAutoRTDAsynchronous = true;
    final Boolean isManualAsynchronous = true;
    final Boolean isCallerRequired = true;
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(name, category, description, helpTopic, isVolatile, isMultiThreadSafe, isMacroEquivalent,
            FunctionType.COMMAND, isLongRunning, false, isAutoRTDAsynchronous, isManualAsynchronous, isCallerRequired), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method5", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new XLIntegerTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    final FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[] {parameterDescription + "  "});
    assertEquals(entry._argumentNames, parameterName);
    assertEquals(entry._description, description);
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, category);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "JU#");
    assertEquals(entry._functionType, FunctionType.COMMAND.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, helpTopic);
    assertEquals(entry._isCallerRequired, isCallerRequired.booleanValue());
    assertEquals(entry._isLongRunning, isLongRunning.booleanValue());
    assertEquals(entry._isManualAsynchronous, isManualAsynchronous.booleanValue());
    assertFalse(entry._isVarArgs);
  }

  /**
   * Tests the entry for a function definition containing a constant where the annotation contains only default
   * values.
   * @throws NoSuchFieldException  if the fields cannot be found
   * @throws SecurityException  if the fields cannot be found
   */
  @Test
  public void testConstantWithDefaultValues() throws NoSuchFieldException, SecurityException {
    final String functionName = "constant1";
    final FunctionMetadata annotation = FunctionMetadata.of(createXLConstant(null, null, null, null), functionName);
    int exportNumber = getExportNumber();
    Field field = DefaultExcelCallbackTest.class.getDeclaredField("INT");
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectFieldGetter(field, new PrimitiveIntegerXLNumberTypeConverter()), exportNumber));
    FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[0]);
    assertEquals(entry._argumentNames, "");
    assertEquals(entry._description, "");
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, functionName);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QQ$");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, "");
    assertFalse(entry._isAutoAsynchronous);
    assertFalse(entry._isCallerRequired);
    assertFalse(entry._isLongRunning);
    assertFalse(entry._isManualAsynchronous);
    assertFalse(entry._isVarArgs);
    exportNumber = getExportNumber();
    field = DefaultExcelCallbackTest.class.getDeclaredField("XL_LOCAL_REF");
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectFieldGetter(field, new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QU$");
    exportNumber = getExportNumber();
    field = DefaultExcelCallbackTest.class.getDeclaredField("XL_MULTI_REF");
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectFieldGetter(field, new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QU$");
  }

  /**
   * Tests the entry for a function definition containing a constant.
   * @throws NoSuchFieldException  if the fields cannot be found
   * @throws SecurityException  if the fields cannot be found
   */
  @Test
  public void testConstant() throws NoSuchFieldException, SecurityException {
    final String functionName = "constant1";
    final String name = "name";
    final String category = "category";
    final String description = "description";
    final String helpTopic = "helpTopic";
    final FunctionMetadata annotation = FunctionMetadata.of(createXLConstant(name, category, description, helpTopic), functionName);
    int exportNumber = getExportNumber();
    Field field = DefaultExcelCallbackTest.class.getDeclaredField("INT");
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectFieldGetter(field, new PrimitiveIntegerXLNumberTypeConverter()), exportNumber));
    FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[0]);
    assertEquals(entry._argumentNames, "");
    assertEquals(entry._description, description);
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, category);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QQ$");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, helpTopic);
    assertFalse(entry._isAutoAsynchronous);
    assertFalse(entry._isCallerRequired);
    assertFalse(entry._isLongRunning);
    assertFalse(entry._isManualAsynchronous);
    assertFalse(entry._isVarArgs);
    exportNumber = getExportNumber();
    field = DefaultExcelCallbackTest.class.getDeclaredField("XL_LOCAL_REF");
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectFieldGetter(field, new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QU$");
    exportNumber = getExportNumber();
    field = DefaultExcelCallbackTest.class.getDeclaredField("XL_MULTI_REF");
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectFieldGetter(field, new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QU$");
  }

  /**
   * Tests the entry for a function definition containing a method where the annotation contains only default
   * values.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testFunctionWithDefaultValues() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    parameters[0] = createXLParameter(null, null, null, null);
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, null, null, null, null, null, null, null), parameters, functionName);
    int exportNumber = getExportNumber();
    Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[] {"  "});
    assertEquals(entry._argumentNames, "1");
    assertEquals(entry._description, "");
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, functionName);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ$");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, "");
    assertFalse(entry._isAutoAsynchronous);
    assertFalse(entry._isCallerRequired);
    assertFalse(entry._isLongRunning);
    assertFalse(entry._isManualAsynchronous);
    assertTrue(entry._isVarArgs);
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method2", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QQ$");
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method3", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new XLLocalReferenceTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UQ$");
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method4", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UQ$");
  }

  /**
   * Tests the entry for a function definition containing a method.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testFunction() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    final String parameterName = "parameterName";
    final String parameterDescription = "parameterDescription";
    final Boolean optional = true;
    final Boolean referenceType = true;
    parameters[0] = createXLParameter(parameterName, parameterDescription, optional, referenceType);
    final String name = "name";
    final String category = "category";
    final String description = "description";
    final String helpTopic = "helpTopic";
    final Boolean isVolatile = true;
    final Boolean isMultiThreadSafe = false;
    final Boolean isMacroEquivalent = true;
    final Boolean isLongRunning = true;
    //final Boolean isAutoAsynchronous = true;
    final Boolean isAutoRTDAsynchronous = true;
    final Boolean isManualAsynchronous = true;
    final Boolean isCallerRequired = true;
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(name, category, description, helpTopic, isVolatile, isMultiThreadSafe, isMacroEquivalent,
            FunctionType.FUNCTION, isLongRunning, false, isAutoRTDAsynchronous, isManualAsynchronous, isCallerRequired), parameters, functionName);
    int exportNumber = getExportNumber();
    Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[] {parameterDescription + "  "});
    assertEquals(entry._argumentNames, parameterName);
    assertEquals(entry._description, description);
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, category);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU#");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, helpTopic);
    //assertEquals(entry._isAutoAsynchronous, isAutoAsynchronous.booleanValue());
    assertEquals(entry._isCallerRequired, isCallerRequired.booleanValue());
    assertEquals(entry._isLongRunning, isLongRunning.booleanValue());
    assertEquals(entry._isManualAsynchronous, isManualAsynchronous.booleanValue());
    assertTrue(entry._isVarArgs);
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method2", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QU#");
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method3", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new XLLocalReferenceTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UU#");
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method4", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UU#");
  }

  /**
   * Tests the argument names string when the method has multiple arguments.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testArgNames() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[2];
    parameters[0] = createXLParameter("parameter1", null, null, null);
    parameters[1] = createXLParameter("parameter2", null, null, null);
    final Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method6", new Class<?>[] {Integer.TYPE, Integer.TYPE});
    FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, null, null, null, null, null, null, null), parameters, functionName);
    int exportNumber = getExportNumber();
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter(), new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._argumentNames, "parameter1,parameter2");
    parameters[0] = createXLParameter(null, null, null, null);
    parameters[1] = createXLParameter(null, null, null, null);
    annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, null, null, null, null, null, null, null), parameters, functionName);
    exportNumber = getExportNumber();
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter(), new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._argumentNames, "1,2");
  }

  /**
   * Tests the entry for a function definition containing a method where the annotation contains only default
   * values.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testFunctionFromConstructorWithDefaultValues() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[0];
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(null, null, null, null, null, null, null, null, null, null, null, null, null), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Constructor<?> constructor = DefaultExcelCallbackTest.class.getConstructor(new Class<?>[0]);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectConstructorInvoker(constructor, new TypeConverter[0], new PrimitiveIntegerXLNumberTypeConverter(),
            new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    final FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[0]);
    assertEquals(entry._argumentNames, "");
    assertEquals(entry._description, "");
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, functionName);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "Q$");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, "");
    assertFalse(entry._isAutoAsynchronous);
    assertFalse(entry._isCallerRequired);
    assertFalse(entry._isLongRunning);
    assertFalse(entry._isManualAsynchronous);
    assertFalse(entry._isVarArgs);
  }

  /**
   * Tests the entry for a function definition containing a method.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testFunctionFromConstructor() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[0];
    final String name = "name";
    final String category = "category";
    final String description = "description";
    final String helpTopic = "helpTopic";
    final Boolean isVolatile = true;
    final Boolean isMultiThreadSafe = false;
    final Boolean isMacroEquivalent = true;
    final Boolean isLongRunning = true;
    final Boolean isAutoRTDAsynchronous = true;
    final Boolean isManualAsynchronous = true;
    final Boolean isCallerRequired = true;
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunction(name, category, description, helpTopic, isVolatile, isMultiThreadSafe, isMacroEquivalent,
            FunctionType.FUNCTION, isLongRunning, false, isAutoRTDAsynchronous, isManualAsynchronous, isCallerRequired), parameters, functionName);
    final int exportNumber = getExportNumber();
    final Constructor<?> constructor = DefaultExcelCallbackTest.class.getConstructor(new Class<?>[0]);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new ObjectConstructorInvoker(constructor, new TypeConverter[0], new PrimitiveIntegerXLNumberTypeConverter(),
            new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    final FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[0]);
    assertEquals(entry._argumentNames, "");
    assertEquals(entry._description, description);
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, category);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "Q#");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, helpTopic);
    assertEquals(entry._isCallerRequired, isCallerRequired.booleanValue());
    assertEquals(entry._isLongRunning, isLongRunning.booleanValue());
    assertEquals(entry._isManualAsynchronous, isManualAsynchronous.booleanValue());
    assertFalse(entry._isVarArgs);
  }

  /**
   * Tests the entry for a function definition containing a method where the annotation contains only default
   * values.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testFunctionsWithDefaultValues() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[0];
    final FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunctions(null, null, null, null, null, null, null, null, null, null, null, null, null), parameters, functionName);
    int exportNumber = getExportNumber();
    Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[0]);
    assertEquals(entry._argumentNames, "");
    assertEquals(entry._description, "");
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, functionName);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ$");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, "");
    assertFalse(entry._isAutoAsynchronous);
    assertFalse(entry._isCallerRequired);
    assertFalse(entry._isLongRunning);
    assertFalse(entry._isManualAsynchronous);
    assertTrue(entry._isVarArgs);
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method2", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QQ$");
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method3", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new XLLocalReferenceTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UQ$");
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method4", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UQ$");
  }

  /**
   * Tests the entry for a functions definition containing a method.
   * @throws NoSuchMethodException  if the method cannot be found
   * @throws SecurityException  if the method cannot be found
   */
  @Test
  public void testFunctions() throws NoSuchMethodException, SecurityException {
    final String functionName = "function1";
    final XLParameter[] parameters = new XLParameter[1];
    final String parameterName = "parameterName";
    final String parameterDescription = "parameterDescription";
    final Boolean optional = true;
    final Boolean referenceType = true;
    parameters[0] = createXLParameter(parameterName, parameterDescription, optional, referenceType);
    final String name = "name";
    final String category = "category";
    final String description = "description";
    final String helpTopic = "helpTopic";
    final Boolean isLongRunning = true;
    final Boolean isAutoRTDAsynchronous = true;
    final Boolean isManualAsynchronous = true;
    final Boolean isCallerRequired = true;
    final Boolean isMacroEquivalent = true;
    final Boolean isVolatile = true;
    final Boolean isMultiThreadSafe = false;
    FunctionMetadata annotation =
        FunctionMetadata.of(createXLFunctions(name, category, description, helpTopic, isVolatile, isMultiThreadSafe, isMacroEquivalent,
            FunctionType.FUNCTION, isLongRunning, false, isAutoRTDAsynchronous, isManualAsynchronous, isCallerRequired), parameters, functionName);
    int exportNumber = getExportNumber();
    Method method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    FunctionEntry entry = getFunction(exportNumber);
    assertEquals(entry._acceleratorKey, "");
    assertEquals(entry._argsHelp, new String[] {parameterDescription + "  "});
    assertEquals(entry._argumentNames, parameterName);
    assertEquals(entry._description, description);
    assertEquals(entry._exportNumber, exportNumber);
    assertEquals(entry._functionCategory, category);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU#");
    assertEquals(entry._functionType, FunctionType.FUNCTION.getExcelValue());
    assertEquals(entry._functionWorksheetName, functionName);
    assertEquals(entry._helpTopic, helpTopic);
    assertEquals(entry._isAutoRTDAsynchronous, isAutoRTDAsynchronous.booleanValue());
    assertEquals(entry._isCallerRequired, isCallerRequired.booleanValue());
    assertEquals(entry._isLongRunning, isLongRunning.booleanValue());
    assertEquals(entry._isManualAsynchronous, isManualAsynchronous.booleanValue());
    assertTrue(entry._isVarArgs);
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method2", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "QU#");
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method3", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new XLLocalReferenceTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UU#");
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method4", Integer.TYPE);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerXLNumberTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionExportName, "UDF_" + exportNumber);
    assertEquals(entry._functionSignature, "UU#");
    parameters[0] = createXLParameter(parameterName, parameterDescription, optional, false);
    annotation =
        FunctionMetadata.of(createXLFunctions(name, category, description, helpTopic, true, false, false,
            FunctionType.FUNCTION, isLongRunning, false, isAutoRTDAsynchronous, isManualAsynchronous, isCallerRequired), parameters, functionName);
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionSignature, "QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ!");
    annotation =
        FunctionMetadata.of(createXLFunctions(name, category, description, helpTopic, false, false, false,
            FunctionType.FUNCTION, isLongRunning, false, isAutoRTDAsynchronous, isManualAsynchronous, isCallerRequired), parameters, functionName);
    exportNumber = getExportNumber();
    method = DefaultExcelCallbackTest.class.getDeclaredMethod("method1", int[].class);
    CALLBACK.registerFunction(FunctionDefinition.of(annotation,
        new SimpleResultMethodInvoker(method, new TypeConverter[] {new PrimitiveIntegerArrayXLArrayTypeConverter()},
            new PrimitiveIntegerXLNumberTypeConverter(), new ObjectXLObjectTypeConverter(ExcelFactory.getInstance())), exportNumber));
    entry = getFunction(exportNumber);
    assertEquals(entry._functionSignature, "QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
  }

  private static synchronized int getExportNumber() {
    return s_exportNumber++;
  }

  private static FunctionEntry getFunction(final int exportNumber) {
    for (final FunctionEntry entry : FUNCTION_REGISTRY.getEntries()) {
      if (entry._exportNumber == exportNumber) {
        return entry;
      }
    }
    throw new IllegalArgumentException();
  }

  private static XLConstant createXLConstant(final String name, final String category, final String description, final String helpTopic) {
    return new XLConstant() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLConstant.class;
      }

      @Override
      public String name() {
        return name == null ? "" : name;
      }

      @Override
      public String category() {
        return category == null ? "" : category;
      }

      @Override
      public String description() {
        return description == null ? "" : description;
      }

      @Override
      public String helpTopic() {
        return helpTopic == null ? "" : helpTopic;
      }

      @Override
      public TypeConversionMode typeConversionMode() {
        return TypeConversionMode.OBJECT_RESULT;
      }

    };
  }

  private static XLParameter createXLParameter(final String name, final String description, final Boolean optional, final Boolean referenceType) {
    return new XLParameter() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLParameter.class;
      }

      @Override
      public String name() {
        return name == null ? "" : name;
      }

      @Override
      public String description() {
        return description == null ? "" : description;
      }

      @Override
      public boolean optional() {
        return optional == null ? false : optional;
      }

      @Override
      public boolean referenceType() {
        return referenceType == null ? false : referenceType;
      }

    };
  }

  private static XLFunction createXLFunction(final String name, final String category, final String description, final String helpTopic,
      final Boolean isVolatile, final Boolean isMultiThreadSafe, final Boolean isMacroEquivalent, final FunctionType functionType,
      final Boolean isLongRunning, final Boolean isAutoAsynchronous, final Boolean isAutoRTDAsynchronous, final Boolean isManualAsynchronous, 
      final Boolean isCallerRequired) {
    return new XLFunction() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLFunction.class;
      }

      @Override
      public String name() {
        return name == null ? "" : name;
      }

      @Override
      public String category() {
        return category == null ? "" : category;
      }

      @Override
      public String description() {
        return description == null ? "" : description;
      }

      @Override
      public String helpTopic() {
        return helpTopic == null ? "" : helpTopic;
      }

      @Override
      public boolean isVolatile() {
        return isVolatile == null ? false : isVolatile;
      }

      @Override
      public boolean isMultiThreadSafe() {
        return isMultiThreadSafe == null ? true : isMultiThreadSafe;
      }

      @Override
      public boolean isMacroEquivalent() {
        return isMacroEquivalent == null ? false : isMacroEquivalent;
      }

      @Override
      public TypeConversionMode typeConversionMode() {
        return TypeConversionMode.SIMPLEST_RESULT;
      }

      @Override
      public FunctionType functionType() {
        return functionType == null ? FunctionType.FUNCTION : functionType;
      }

      @Override
      public boolean isLongRunning() {
        return isLongRunning == null ? false : isLongRunning;
      }

      //@Override
      //public boolean isAutoAsynchronous() {
      //  return isAutoAsynchronous == null ? false : isAutoAsynchronous;
      //}
      
      @Override
      public boolean isAutoRTDAsynchronous() {
        return isAutoRTDAsynchronous == null ? false : isAutoRTDAsynchronous;
      }

      @Override
      public boolean isManualAsynchronous() {
        return isManualAsynchronous == null ? false : isManualAsynchronous;
      }

      @Override
      public boolean isCallerRequired() {
        return isCallerRequired == null ? false : isCallerRequired;
      }

    };
  }

  private static XLFunctions createXLFunctions(final String prefix, final String category, final String description, final String helpTopic,
      final Boolean isVolatile, final Boolean isMultiThreadSafe, final Boolean isMacroEquivalent, final FunctionType functionType,
      final Boolean isLongRunning, final Boolean isAutoAsynchronous, final Boolean isAutoRTDAsynchronous, final Boolean isManualAsynchronous, 
      final Boolean isCallerRequired) {
    return new XLFunctions() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return XLFunctions.class;
      }

      @Override
      public String prefix() {
        return prefix == null ? "" : prefix;
      }

      @Override
      public String category() {
        return category == null ? "" : category;
      }

      @Override
      public String description() {
        return description == null ? "" : description;
      }

      @Override
      public String helpTopic() {
        return helpTopic == null ? "" : helpTopic;
      }

      @Override
      public boolean isVolatile() {
        return isVolatile == null ? false : isVolatile;
      }

      @Override
      public boolean isMultiThreadSafe() {
        return isMultiThreadSafe == null ? true : isMultiThreadSafe;
      }

      @Override
      public boolean isMacroEquivalent() {
        return isMacroEquivalent == null ? false : isMacroEquivalent;
      }

      @Override
      public TypeConversionMode typeConversionMode() {
        return TypeConversionMode.SIMPLEST_RESULT;
      }

      @Override
      public FunctionType functionType() {
        return functionType == null ? FunctionType.FUNCTION : functionType;
      }

      @Override
      public boolean isLongRunning() {
        return isLongRunning == null ? false : isLongRunning;
      }

      //@Override
      //public boolean isAutoAsynchronous() {
      //  return isAutoAsynchronous == null ? false : isAutoAsynchronous;
      //}

      @Override
      public boolean isManualAsynchronous() {
        return isManualAsynchronous == null ? false : isManualAsynchronous;
      }

      @Override
      public boolean isCallerRequired() {
        return isCallerRequired == null ? false : isCallerRequired;
      }

      @Override
      public boolean isAutoRTDAsynchronous() {
        return isAutoRTDAsynchronous == null ? false : isAutoRTDAsynchronous;
      }

    };
  }

  @SuppressWarnings("unused")
  private static int method1(final int... args) {
    return INT;
  }

  @SuppressWarnings("unused")
  private static int method2(final int arg) {
    return INT;
  }

  @SuppressWarnings("unused")
  private static XLLocalReference method3(final int arg) {
    return XL_LOCAL_REF;
  }

  @SuppressWarnings("unused")
  private static XLMultiReference method4(final int arg) {
    return XL_MULTI_REF;
  }

  @SuppressWarnings("unused")
  private static XLInteger method5(final int arg) {
    return XLInteger.of(arg);
  }

  @SuppressWarnings("unused")
  private static int method6(final int arg1, final int arg2) {
    return INT;
  }

  /**
   * Type converter for XLLocalReference.
   */
  private static class XLLocalReferenceTypeConverter extends AbstractTypeConverter {

    protected XLLocalReferenceTypeConverter() {
      super(Object[].class, XLLocalReference.class);
    }

    @Override
    public Object toXLValue(final Object from) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object toJavaObject(final Type expectedType, final Object from) {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * Type converter for XLMultiReference.
   */
  private static class XLMultiReferenceTypeConverter extends AbstractTypeConverter {

    protected XLMultiReferenceTypeConverter() {
      super(Object[].class, XLMultiReference.class);
    }

    @Override
    public Object toXLValue(final Object from) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object toJavaObject(final Type expectedType, final Object from) {
      throw new UnsupportedOperationException();
    }

  }

  /**
   * Type converter for XLInteger.
   */
  private static class XLIntegerTypeConverter extends AbstractTypeConverter {

    protected XLIntegerTypeConverter() {
      super(Object.class, XLInteger.class);
    }

    @Override
    public Object toXLValue(final Object from) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object toJavaObject(final Type expectedType, final Object from) {
      throw new UnsupportedOperationException();
    }
  }
}
