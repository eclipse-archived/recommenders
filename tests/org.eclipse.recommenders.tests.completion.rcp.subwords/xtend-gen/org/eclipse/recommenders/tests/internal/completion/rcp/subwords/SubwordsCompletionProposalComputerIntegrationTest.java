package org.eclipse.recommenders.tests.internal.completion.rcp.subwords;

import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsCompletionProposalComputer;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.SmokeTestScenarios;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ComparableExtensions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("all")
public class SubwordsCompletionProposalComputerIntegrationTest {
  private static JavaProjectFixture fixture = new Function0<JavaProjectFixture>() {
    public JavaProjectFixture apply() {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
      return _javaProjectFixture;
    }
  }.apply();
  
  private Stopwatch stopwatch = new Function0<Stopwatch>() {
    public Stopwatch apply() {
      Stopwatch _stopwatch = new Stopwatch();
      return _stopwatch;
    }
  }.apply();
  
  private long MAX_COMPUTATION_LIMIT_MILLIS = 2000;
  
  @Test
  public void test000_smoke() {
    List<CharSequence> _scenarios = SmokeTestScenarios.scenarios();
    for (final CharSequence scenario : _scenarios) {
      this.smokeTest(scenario);
    }
  }
  
  @Test
  public void test001() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o=\"\"; o.hc$");
      CharSequence _method = CodeBuilder.method(_builder);
      final CharSequence code = _method;
      List<String> _asList = Arrays.<String>asList("hashCode");
      this.exercise(code, _asList);
  }
  
  @Test
  public void test002() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o=\"\"; o.c$");
      CharSequence _method = CodeBuilder.method(_builder);
      final CharSequence code = _method;
      List<String> _asList = Arrays.<String>asList("clone", "hashCode", "getClass");
      this.exercise(code, _asList);
  }
  
  @Test
  public void test003() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o=\"\"; o.C$");
      CharSequence _method = CodeBuilder.method(_builder);
      final CharSequence code = _method;
      List<String> _asList = Arrays.<String>asList("hashCode", "getClass");
      this.exercise(code, _asList);
  }
  
  @Test
  public void test004() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o=\"\"; o.cod$");
      CharSequence _method = CodeBuilder.method(_builder);
      final CharSequence code = _method;
      List<String> _asList = Arrays.<String>asList("hashCode");
      this.exercise(code, _asList);
  }
  
  @Test
  public void test005() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o=\"\"; o.coe$");
      CharSequence _method = CodeBuilder.method(_builder);
      final CharSequence code = _method;
      List<String> _asList = Arrays.<String>asList("hashCode", "clone");
      this.exercise(code, _asList);
  }
  
  @Test
  public void test006() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o=\"\"; o.Ce$");
      CharSequence _method = CodeBuilder.method(_builder);
      final CharSequence code = _method;
      List<String> _asList = Arrays.<String>asList("hashCode");
      this.exercise(code, _asList);
  }
  
  @Test
  public void test007_subtype() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("String s=\"\"; s.lone$");
      CharSequence _method = CodeBuilder.method(_builder);
      final CharSequence code = _method;
      List<String> _asList = Arrays.<String>asList("clone");
      this.exercise(code, _asList);
  }
  
  /**
   * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=370572
   */
  @Test
  public void testBug370572_1() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public class SubwordsBug {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("File aFile = null;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public void m1() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("afl$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public void m(File f) {}}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final CharSequence code = _builder;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("aFile");
      ArrayList<String> expected = _newArrayList;
      this.exercise(code, expected);
  }
  
  @Test
  @Ignore("this fails because JDT does not propose anything at m($afl) (note, *we* trigger code completion before the first token))")
  public void testBug370572_2() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public class SubwordsBug {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("File aFile = null;");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public void m1() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("m(afl$);");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("\t");
      _builder.append("public void m(File f) {}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final CharSequence code = _builder;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("aFile");
      ArrayList<String> expected = _newArrayList;
      this.exercise(code, expected);
  }
  
  public void smokeTest(final CharSequence code) {
    try {
      {
        SubwordsCompletionProposalComputerIntegrationTest.fixture.clear();
        String _string = code.toString();
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = SubwordsCompletionProposalComputerIntegrationTest.fixture.createFileAndParseWithMarkers(_string);
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        Set<Integer> _second = struct.getSecond();
        for (final Integer completionIndex : _second) {
          {
            JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
            final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
            SubwordsCompletionProposalComputer _subwordsCompletionProposalComputer = new SubwordsCompletionProposalComputer();
            final SubwordsCompletionProposalComputer sut = _subwordsCompletionProposalComputer;
            sut.sessionStarted();
            Stopwatch _stopwatch = new Stopwatch();
            this.stopwatch = _stopwatch;
            this.stopwatch.start();
            CompletionProposalCollector _completionProposalCollector = new CompletionProposalCollector(cu, false);
            cu.codeComplete((completionIndex).intValue(), _completionProposalCollector);
            sut.computeCompletionProposals(ctx, null);
            this.stopwatch.stop();
            this.failIfComputerTookTooLong(code);
          }
        }
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void exercise(final CharSequence code, final List<String> expected) {
    try {
      {
        SubwordsCompletionProposalComputerIntegrationTest.fixture.clear();
        String _string = code.toString();
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = SubwordsCompletionProposalComputerIntegrationTest.fixture.createFileAndParseWithMarkers(_string);
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        Set<Integer> _second = struct.getSecond();
        Integer _head = IterableExtensions.<Integer>head(_second);
        final Integer completionIndex = _head;
        CompletionProposalCollector _completionProposalCollector = new CompletionProposalCollector(cu, false);
        cu.codeComplete((completionIndex).intValue(), _completionProposalCollector);
        JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
        final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
        SubwordsCompletionProposalComputer _subwordsCompletionProposalComputer = new SubwordsCompletionProposalComputer();
        final SubwordsCompletionProposalComputer sut = _subwordsCompletionProposalComputer;
        sut.sessionStarted();
        this.stopwatch.start();
        List _computeCompletionProposals = sut.computeCompletionProposals(ctx, null);
        final List actual = _computeCompletionProposals;
        this.stopwatch.stop();
        this.failIfComputerTookTooLong(code);
        StringConcatenation _builder = new StringConcatenation();
        _builder.append(" ");
        _builder.append("some expected values were not found.\\nExpected: ");
        _builder.append(expected, " ");
        _builder.append(",\\nFound: ");
        _builder.append(actual, " ");
        _builder.append(" ");
        String _string_1 = _builder.toString();
        int _size = expected.size();
        int _size_1 = actual.size();
        Assert.assertEquals(_string_1, _size, _size_1);
        for (final String e : expected) {
          final Function1<Object,Boolean> _function = new Function1<Object,Boolean>() {
              public Boolean apply(final Object p) {
                String _string = p.toString();
                boolean _startsWith = _string.startsWith(e);
                return Boolean.valueOf(_startsWith);
              }
            };
          Object _findFirst = IterableExtensions.<Object>findFirst(actual, _function);
          Assert.assertNotNull(_findFirst);
        }
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public Object failIfComputerTookTooLong(final CharSequence code) {
    Object _xifexpression = null;
    long _elapsedMillis = this.stopwatch.elapsedMillis();
    boolean _operator_greaterThan = ComparableExtensions.<Long>operator_greaterThan(Long.valueOf(_elapsedMillis), Long.valueOf(this.MAX_COMPUTATION_LIMIT_MILLIS));
    if (_operator_greaterThan) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("completion took FAR too long: ");
      long _elapsedMillis_1 = this.stopwatch.elapsedMillis();
      _builder.append(_elapsedMillis_1, "");
      _builder.append("\\n in:\\n");
      _builder.append(code, "");
      String _string = _builder.toString();
      Assert.fail(_string);
    }
    return _xifexpression;
  }
}
