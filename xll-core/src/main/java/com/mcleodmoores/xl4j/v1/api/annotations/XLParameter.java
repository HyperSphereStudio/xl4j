/**
 * Copyright (C) 2014 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j.v1.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method parameters to provide extra meta-data to Excel.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
public @interface XLParameter {

  /**
   * The name of the parameter, as it is to appear in the function wizard. If not supplied, the library will attempt to: 1. Use the debug
   * symbol if available (the class must have been compiled with debugging info) 2. Use the JavaDoc name if available. 3. Use 'arg&lt;n&gt;'
   * where &lt;n&gt; is 1-based.
   *
   * @return the name
   */
  String name() default "";

  /**
   * The description of the parameter, as it is to appear in the function wizard. If not supplied, this will default to the JavaDoc
   * description if available.
   *
   * @return the description
   */
  String description() default "";

  /**
   * Whether the parameter is optional. This defaults to false, i.e. not optional.
   * 
   * @return whether this parameter is optional
   */
  boolean optional() default false;

  /**
   * @return true, if the parameter is a reference type (e.g. an XLLocalReference or XLMultiReference or XLArray byref)
   */
  boolean referenceType() default false;
}
