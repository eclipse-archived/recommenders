package org.eclipse.recommenders.tests;

import java.util.Arrays;
import java.util.List;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function0;

@SuppressWarnings("all")
public class SmokeTestScenarios {
  public static List<CharSequence> scenarios() {
    List<CharSequence> _asList = Arrays.<CharSequence>asList(
      SmokeTestScenarios.IMPORT_01, SmokeTestScenarios.IMPORT_02, 
      SmokeTestScenarios.PACKAGE_01, SmokeTestScenarios.PACKAGE_02, SmokeTestScenarios.PACKAGE_03, 
      SmokeTestScenarios.CLASSBODY_01, SmokeTestScenarios.CLASSBODY_02, SmokeTestScenarios.CLASSBODY_03, SmokeTestScenarios.CLASSBODY_04, SmokeTestScenarios.CLASSBODY_05, 
      SmokeTestScenarios.METHOD_STMT_01, SmokeTestScenarios.METHOD_STMT_02, SmokeTestScenarios.METHOD_STMT_03, SmokeTestScenarios.METHOD_STMT_04, SmokeTestScenarios.METHOD_STMT_05, SmokeTestScenarios.METHOD_STMT_06, 
      SmokeTestScenarios.METHOD_STMT_07, SmokeTestScenarios.METHOD_STMT_08, SmokeTestScenarios.METHOD_STMT_09, SmokeTestScenarios.METHOD_STMT_10, 
      SmokeTestScenarios.COMMENTS_01, SmokeTestScenarios.COMMENTS_02);
    return _asList;
  }
  
  public static CharSequence someClass = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public class C {}");
      return _builder;
    }
  }.apply();
  
  public static CharSequence IMPORT_01 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("$i$mport$ $java$.$uti$l.$");
      _builder.newLine();
      _builder.append(CodeBuilder.someClass, "");
      _builder.newLineIfNotEmpty();
      return _builder;
    }
  }.apply();
  
  public static CharSequence IMPORT_02 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("import $stat$ic$ $java$.$uti$l.Collection.$");
      _builder.newLine();
      _builder.append(CodeBuilder.someClass, "");
      _builder.newLineIfNotEmpty();
      return _builder;
    }
  }.apply();
  
  public static CharSequence IMPORT_03 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("$");
      _builder.newLine();
      _builder.append(CodeBuilder.someClass, "");
      _builder.newLineIfNotEmpty();
      return _builder;
    }
  }.apply();
  
  public static CharSequence PACKAGE_01 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("$");
      _builder.newLine();
      _builder.append(CodeBuilder.someClass, "");
      _builder.newLineIfNotEmpty();
      return _builder;
    }
  }.apply();
  
  public static CharSequence PACKAGE_02 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("pack$age $");
      _builder.newLine();
      _builder.append(CodeBuilder.someClass, "");
      _builder.newLineIfNotEmpty();
      return _builder;
    }
  }.apply();
  
  public static CharSequence PACKAGE_03 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package org.$");
      _builder.newLine();
      _builder.append(CodeBuilder.someClass, "");
      _builder.newLineIfNotEmpty();
      return _builder;
    }
  }.apply();
  
  public static CharSequence CLASSBODY_01 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class ExtendingClass1 extends UnknownType");
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("$");
      CharSequence _classDeclaration = CodeBuilder.classDeclaration(_builder, _builder_1);
      return _classDeclaration;
    }
  }.apply();
  
  public static CharSequence CLASSBODY_02 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class ExtendingClass2 extends UnknownType");
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("siz$");
      CharSequence _classDeclaration = CodeBuilder.classDeclaration(_builder, _builder_1);
      return _classDeclaration;
    }
  }.apply();
  
  public static CharSequence CLASSBODY_03 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("private UnknownType field = $");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      return _classbody;
    }
  }.apply();
  
  public static CharSequence CLASSBODY_04 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("modifier Object o = $");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      return _classbody;
    }
  }.apply();
  
  public static CharSequence CLASSBODY_05 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public List = $");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      return _classbody;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_01 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Ob$;");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_02 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object $");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_03 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object $o$ = $");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_04 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o = new $");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_05 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o = \"\";");
      _builder.newLine();
      _builder.append("o.$");
      _builder.newLine();
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_06 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("void <T> m(T t){");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("t.$");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("}");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      return _classbody;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_07 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("UnknownType.$exit$($)");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_08 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("UnknownType o = $new $File($to$String())");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_09 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("UnknownType o = \"\";");
      _builder.newLine();
      _builder.append("o.$");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence METHOD_STMT_10 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("undef$inedMethod($).$call($)");
      CharSequence _method = CodeBuilder.method(_builder);
      return _method;
    }
  }.apply();
  
  public static CharSequence COMMENTS_01 = new Function0<CharSequence>() {
    public CharSequence apply() {
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
      _builder.append("public class Comments01 {");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      return _builder;
    }
  }.apply();
  
  public static CharSequence COMMENTS_02 = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("/**");
      _builder.newLine();
      _builder.append("* $");
      _builder.newLine();
      _builder.append("*/");
      _builder.newLine();
      _builder.append("static {");
      _builder.newLine();
      _builder.append("}");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      return _classbody;
    }
  }.apply();
  
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
}
