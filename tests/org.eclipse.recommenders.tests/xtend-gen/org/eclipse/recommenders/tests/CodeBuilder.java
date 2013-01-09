package org.eclipse.recommenders.tests;

import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function0;

@SuppressWarnings("all")
public class CodeBuilder {
  private static AtomicInteger classCounter = new Function0<AtomicInteger>() {
    public AtomicInteger apply() {
      AtomicInteger _atomicInteger = new AtomicInteger();
      return _atomicInteger;
    }
  }.apply();
  
  public static String classname() {
    int _addAndGet = CodeBuilder.classCounter.addAndGet(1);
    String _plus = ("TestClass" + Integer.valueOf(_addAndGet));
    return _plus;
  }
  
  public static CharSequence classbody(final CharSequence classbody) {
    String _classname = CodeBuilder.classname();
    CharSequence _classbody = CodeBuilder.classbody(_classname, classbody);
    return _classbody;
  }
  
  public static CharSequence classbody(final CharSequence classname, final CharSequence classbody) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public class ");
    _builder.append(classname, "");
    _builder.append(" ");
    CharSequence _classDeclaration = CodeBuilder.classDeclaration(_builder, classbody);
    return _classDeclaration;
  }
  
  public static CharSequence classDeclaration(final CharSequence declaration, final CharSequence body) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.lang.reflect.*;");
    _builder.newLine();
    _builder.append("import java.lang.annotation.*;");
    _builder.newLine();
    _builder.append("import java.math.*;");
    _builder.newLine();
    _builder.append("import java.io.*;");
    _builder.newLine();
    _builder.append("import java.text.*;");
    _builder.newLine();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("import java.util.concurrent.atomic.*;");
    _builder.newLine();
    _builder.append("import javax.annotation.*;");
    _builder.newLine();
    _builder.append("import javax.xml.ws.Action;");
    _builder.newLine();
    _builder.append(declaration, "");
    _builder.append(" {");
    _builder.newLineIfNotEmpty();
    _builder.append("\t");
    _builder.append(body, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  public static CharSequence OLD_TEST_CLASS() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*$ Copyright (c) 2010, 2011 Darmstadt University of Technology.");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* All rights reserved. This$ program and the accompanying materials");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* are made available under the terms of the Eclipse Public License v1.0");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* which accompanies this distribution, and is available at");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* http://www.$eclipse.org/legal/epl-v10.html");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* Contributors$:");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*    Marcel Bruch $- initial API and implementation.");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("package org.ecli$pse.recommenders.tests.comp$letion.rcp.calls$;$");
    _builder.newLine();
    _builder.append("$");
    _builder.newLine();
    _builder.append("im$port java.$util.*$;");
    _builder.newLine();
    _builder.append("im$port $stati$c$ java.util.Collections.$;");
    _builder.newLine();
    _builder.append("$");
    _builder.newLine();
    _builder.append("/**");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* Some $class comments {@link$plain $}");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* ");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("* @see $");
    _builder.newLine();
    _builder.append(" ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("public class AllJavaFeatures<T extends Collection> {");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("/**");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("* $");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("static {");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("S$et $s = new Has$hSet<St$ring>();");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("s$.$add(\"$\");");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.newLine();
    _builder.append("    ");
    _builder.append("/**");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("* $");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("* ");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("* @par$am $");
    _builder.newLine();
    _builder.append("     ");
    _builder.append("*/");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("pub$lic st$atic voi$d stat$ic1(fi$nal St$ring ar$g) {");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("ch$ar$ c$ = a$rg.$charAt($);");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("Str$ing $s $=$ \"$\"$;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("    ");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("public void <T$> mT$ypeParameter(T$ s$) {");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("s.$;");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("    ");
    _builder.newLine();
    _builder.append("    ");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("priv$ate sta$tic cl$ass MyInne$rClass extend$s Obse$rvable{");
    _builder.newLine();
    _builder.append("        ");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("@Override");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("pub$lic synchro$nized vo$id addObs$erver(Observ$er $o) {");
    _builder.newLine();
    _builder.append("        \t");
    _builder.append("o$");
    _builder.newLine();
    _builder.append("        \t");
    _builder.append(";");
    _builder.newLine();
    _builder.append("            ");
    _builder.append("// TO$DO A$uto-generated method stub");
    _builder.newLine();
    _builder.append("            ");
    _builder.append("sup$er.addOb$server($o);");
    _builder.newLine();
    _builder.append("            ");
    _builder.append("o.$");
    _builder.newLine();
    _builder.append("        ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("    ");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  public static CharSequence method(final CharSequence methodbody) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void __test() throws Exception {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append(methodbody, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    CharSequence _classbody = CodeBuilder.classbody(_builder);
    return _classbody;
  }
  
  public static CharSequence method(final CharSequence classname, final CharSequence methodbody) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void __test() throws Exception {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append(methodbody, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    CharSequence _classbody = CodeBuilder.classbody(classname, _builder);
    return _classbody;
  }
  
  public static CharSequence classWithFieldsAndTestMethod(final CharSequence fieldDeclarations, final CharSequence methodbody) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.newLine();
    _builder.append(fieldDeclarations, "");
    _builder.newLineIfNotEmpty();
    _builder.newLine();
    _builder.append("public void __test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append(methodbody, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    CharSequence _classbody = CodeBuilder.classbody(_builder);
    return _classbody;
  }
}
