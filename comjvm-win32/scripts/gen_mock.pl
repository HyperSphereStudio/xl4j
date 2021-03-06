#!/usr/bin/perl
use strict;
use warnings;
my $MAX_PARAMS = 16;
my $MAX_BLOCKS = 32;

my $header = <<'END';
/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.xl4j;

import com.mcleodmoores.xl4j.values.XLObject;
import com.mcleodmoores.xl4j.values.XLValue;
import com.mcleodmoores.xl4j.ExcelCallhandler;

/**
 * Class to mock the DLL exports table.
 */
public class MockDLLExports implements RawExcelCallback {
  
  private ExcelCallHandler _callHandler;
  /**
   * Constructor.
   * @param callHandler  the call handler for invoking methods
   */
  public MockDLLExports(final ExcelCallHandler callHandler) {
    _callHandler = callHandler;
  }
  // CHECKSTYLE:OFF -- autogenerated.
END
print $header;
for (my $params = 0; $params < $MAX_PARAMS; $params++) {
  for (my $block = 0; $block < $MAX_BLOCKS; $block++) {
    print "  public XLValue UDF_${params}_${block}(";
    for (my $paramCount = 0; $paramCount < $params; $paramCount++) {
      print "final XLValue param${paramCount}";
      if ($paramCount < $params - 1) {
        print ", ";
      } else {
      }
    }
    print ") {\n";
    print "    return UDF(${block}";
    for (my $paramCount = 0; $paramCount < $params; $paramCount++) {
      print ", param${paramCount}";
    }
    print ");\n";
    print "  }\n\n";
  }
}
print <<'END';
  
  public XLValue UDF(final int block, final XLValue... args) {
    _callHandler.invoke(block, args);
  }
  // CHECKSTYLE:ON
}
END

