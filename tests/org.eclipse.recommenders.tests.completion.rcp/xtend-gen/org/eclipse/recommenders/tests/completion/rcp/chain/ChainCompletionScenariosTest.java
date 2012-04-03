package org.eclipse.recommenders.tests.completion.rcp.chain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.chain.ChainCompletionProposalComputer;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.SmokeTestScenarios;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("all")
public class ChainCompletionScenariosTest {
  private static JavaProjectFixture fixture = new Function0<JavaProjectFixture>() {
    public JavaProjectFixture apply() {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
      return _javaProjectFixture;
    }
  }.apply();
  
  @Before
  public void before() {
    try {
      ChainCompletionScenariosTest.fixture.clear();
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void smokeTestScenarios() {
    try {
      List<CharSequence> _scenarios = SmokeTestScenarios.scenarios();
      for (final CharSequence scenario : _scenarios) {
        {
          final Tuple<ICompilationUnit,Set<Integer>> struct = ChainCompletionScenariosTest.fixture.createFileAndParseWithMarkers(scenario);
          final ICompilationUnit cu = struct.getFirst();
          Set<Integer> _second = struct.getSecond();
          for (final Integer completionIndex : _second) {
            {
              JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
              final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
              RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
              ChainCompletionProposalComputer _chainCompletionProposalComputer = new ChainCompletionProposalComputer(_recommendersCompletionContextFactoryMock);
              final ChainCompletionProposalComputer sut = _chainCompletionProposalComputer;
              sut.sessionStarted();
              sut.computeCompletionProposals(ctx, null);
            }
          }
        }
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testFindLocalAnchor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("ExecutorService pool = Executors.newCachedThreadPool();");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Future future = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindLocalAnchorWithIsExactMatch() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<Object> findMe;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<String> l = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "findMe", 
      "findMe subList");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindFieldAnchor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ExecutorService pool;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Future future = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindArrayFieldAnchor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ExecutorService pool[];");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Future future = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindMultiDimArrayField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ExecutorService pool[][][];");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Future future = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("} ");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool submit", 
      "pool submit", 
      "pool submit");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindFieldInSuperType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("import java.awt.*;");
    _builder.newLine();
    _builder.append("class MyClass extends Event{");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Event e = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "evt");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  @Ignore("too many solutions - more than 200!")
  public void testCompletionOnRuntime() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.io.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("InputStream in = Runtime.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "getRuntime getLocalizedInputStream", 
      "findMe subList");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnLocaVariable() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<Object> findMe;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<String> l = findMe.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "subList");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  @Ignore("fails on build server")
  public void testCompletionOnStaticType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<String> l = Collections.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "list", 
      "list subList", 
      "unmodifiableList", 
      "unmodifiableList subList", 
      "synchronizedList", 
      "synchronizedList subList", 
      "checkedList", 
      "checkedList subList", 
      "emptyList", 
      "emptyList subList", 
      "singletonList", 
      "singletonList subList", 
      "nCopies", 
      "nCopies subList", 
      "EMPTY_LIST", 
      "EMPTY_LIST subList");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnReturnStatement() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("List<String> m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("List<Object> l;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("return $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "l", 
      "l subList", 
      "m", 
      "m subList");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnEnumDoesNotThrowNPE() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.lang.annotation.*;");
    _builder.newLine();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void m(){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("String s = Annotation.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  /**
   * we had some trouble with supertype hierarchy. This test that we do not generate
   * any chains that return a supertype of the requested type (ExecutorService in this case)
   */
  @Test
  public void testFindSelfAssignment() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ThreadPoolExecutor pool;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("pool = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testFindMatchingSubtypeForAssignment() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("ThreadPoolExecutor pool;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("ExecutorService pool = $");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "pool");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testCompletionOnFieldField() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.awt.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Event e;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Event evt = e.evt.$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "evt");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void testPrefixFilter() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.awt.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Event evt;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Event aevt;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Event evt = a$");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "aevt", "aevt evt");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  @Test
  public void test012() {
    this.compile(this.FIELDS);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public static Fields f = new Fields();");
    _builder.newLine();
    _builder.append("public static void test_protected() {");
    _builder.newLine();
    _builder.append("final Boolean c = $");
    _builder.newLine();
    final CharSequence code = CodeBuilder.classbody(_builder);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(
      "f _public");
    List<List<String>> expected = this.w(((String[])Conversions.unwrapArray(_newArrayList, String.class)));
    this.exercise(code, expected);
  }
  
  public void exercise(final CharSequence code, final List<? extends List<String>> expected) {
    try {
      String _string = code.toString();
      final Tuple<ICompilationUnit,Set<Integer>> struct = ChainCompletionScenariosTest.fixture.createFileAndParseWithMarkers(_string);
      final ICompilationUnit cu = struct.getFirst();
      Set<Integer> _second = struct.getSecond();
      final Integer completionIndex = IterableExtensions.<Integer>head(_second);
      JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
      final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
      RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
      ChainCompletionProposalComputer _chainCompletionProposalComputer = new ChainCompletionProposalComputer(_recommendersCompletionContextFactoryMock);
      final ChainCompletionProposalComputer sut = _chainCompletionProposalComputer;
      sut.sessionStarted();
      final List<ICompletionProposal> proposals = sut.computeCompletionProposals(ctx, null);
      for (final ICompletionProposal proposal : proposals) {
        {
          final List<String> names = ((ChainCompletionProposal) proposal).getChainElementNames();
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("couldn\'t find ");
          _builder.append(names, "");
          _builder.append(" in expected.");
          String _string_1 = _builder.toString();
          boolean _remove = expected.remove(names);
          Assert.assertTrue(_string_1, _remove);
        }
      }
      StringConcatenation _builder = new StringConcatenation();
      _builder.append(" ");
      _builder.append("some expected values were not found ");
      _builder.append(expected, " ");
      _builder.append(" ");
      String _string_1 = _builder.toString();
      boolean _isEmpty = expected.isEmpty();
      Assert.assertTrue(_string_1, _isEmpty);
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Tuple<ICompilationUnit,Set<Integer>> compile(final CharSequence code) {
    try {
      Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = ChainCompletionScenariosTest.fixture.createFileAndParseWithMarkers(code);
      return _createFileAndParseWithMarkers;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public List<String> l(final String spaceSeparatedElementNames) {
    final String[] elementNames = StringUtils.split(spaceSeparatedElementNames);
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList(elementNames);
    return ((List<String>) _newArrayList);
  }
  
  public List<List<String>> w(final String[] chains) {
    final List<List<String>> res = CollectionLiterals.<List<String>>newArrayList();
    for (final String chain : chains) {
      List<String> _l = this.l(chain);
      res.add(_l);
    }
    return ((List<List<String>>) res);
  }
  
  private CharSequence FIELDS = new Function0<CharSequence>() {
    public CharSequence apply() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public class Fields {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public static \t\t\tBoolean _spublic;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("protected static \t\tBoolean _sprotected;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("/* default */ static \tBoolean _sdefault;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("private static \t\t\tBoolean _sprivate;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public \t\t\tBoolean _public;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("protected \t\tBoolean _protected;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("/* default */\tBoolean _default;");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("private \t\tBoolean _private;");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      return _builder;
    }
  }.apply();
}
