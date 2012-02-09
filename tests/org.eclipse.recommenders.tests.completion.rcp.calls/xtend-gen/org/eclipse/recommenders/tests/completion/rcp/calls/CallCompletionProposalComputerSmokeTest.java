package org.eclipse.recommenders.tests.completion.rcp.calls;

import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer;
import org.eclipse.recommenders.tests.SmokeTestScenarios;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.completion.rcp.calls.ModelStoreMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IntegerExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
import org.junit.Test;

@SuppressWarnings("all")
public class CallCompletionProposalComputerSmokeTest {
  @Test
  public void smokeTest() {
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
      final CharSequence code = _builder;
      this.exercise(code);
  }
  
  @Test
  public void smokeTestScenarios() {
    List<CharSequence> _scenarios = SmokeTestScenarios.scenarios();
    for (final CharSequence scenario : _scenarios) {
      this.exercise(scenario);
    }
  }
  
  @Test
  public void testFailCompletionOnTypeParameter() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package completion.calls;");
      _builder.newLine();
      _builder.append("import java.util.Collection;");
      _builder.newLine();
      _builder.append("public class CompletionInClassWithGenerics {");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public void <T> test() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("final T item;");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("item.$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final CharSequence code = _builder;
      this.exercise(code);
  }
  
  private void exercise(final CharSequence code) {
    try {
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        String _string = code.toString();
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string);
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        cu.becomeWorkingCopy(null);
        CompilationUnit _reconcile = cu.reconcile(AST.JLS4, true, true, null, null);
        final CompilationUnit ast = _reconcile;
        Assert.assertNotNull(ast);
        ModelStoreMock _modelStoreMock = new ModelStoreMock();
        JavaElementResolver _javaElementResolver = new JavaElementResolver();
        RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
        CallsCompletionProposalComputer _callsCompletionProposalComputer = new CallsCompletionProposalComputer(_modelStoreMock, _javaElementResolver, _recommendersCompletionContextFactoryMock);
        final CallsCompletionProposalComputer sut = _callsCompletionProposalComputer;
        Set<Integer> _second = struct.getSecond();
        for (final Integer pos : _second) {
          String _source = cu.getSource();
          int _length = _source.length();
          boolean _operator_lessThan = IntegerExtensions.operator_lessThan((pos).intValue(), _length);
          if (_operator_lessThan) {
            JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (pos).intValue());
            sut.computeCompletionProposals(_javaContentAssistContextMock, null);
          } else {
            String _source_1 = cu.getSource();
            String _operator_plus = StringExtensions.operator_plus("warning: skipped smoke scenario: ", _source_1);
            InputOutput.<String>print(_operator_plus);
          }
        }
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
