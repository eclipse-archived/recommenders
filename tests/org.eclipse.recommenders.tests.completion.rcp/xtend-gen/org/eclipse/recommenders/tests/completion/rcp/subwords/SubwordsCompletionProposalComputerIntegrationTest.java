package org.eclipse.recommenders.tests.completion.rcp.subwords;

import com.google.common.base.Stopwatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.CompletionProposalCollector;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.Document;
import org.eclipse.recommenders.internal.completion.rcp.subwords.SubwordsCompletionProposalComputer;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.SmokeTestScenarios;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
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
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("hashCode");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test002() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o=\"\"; o.c$");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("clone", "hashCode", "getClass");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test003() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o=\"\"; o.C$");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("hashCode", "getClass", 
      "clone");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test004() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o=\"\"; o.cod$");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("hashCode");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test005() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o=\"\"; o.coe$");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("hashCode", "clone");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test006() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o=\"\"; o.Ce$");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("hashCode", "clone");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test007_subtype() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("String s=\"\"; s.lone$");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("clone");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test008_overloaded() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o=\"\"; o.w$");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("wait", "wait", "wait");
    this.exerciseAndVerify(code, _asList);
  }
  
  @Test
  public void test009_ProposedGettersAndSetters() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("String id;$");
    final CharSequence code = CodeBuilder.classbody(_builder);
    List<String> _asList = Arrays.<String>asList("getId", "setId");
    this.exerciseAndVerifyLenient(code, _asList);
  }
  
  @Test
  public void test010_ConstuctorCalls() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("ConcurrentHashMap b = new ConcurrentHashMap$");
    final CharSequence code = CodeBuilder.classbody(_builder);
    List<String> _asList = Arrays.<String>asList("ConcurrentHashMap(int");
    this.exerciseAndVerifyLenient(code, _asList);
  }
  
  @Test
  public void test011_NewCompletionOnCompleteStatement() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("new LinkedLis$t(){};");
    final CharSequence code = CodeBuilder.method(_builder);
    List<String> _asList = Arrays.<String>asList("LinkedList(");
    this.exerciseAndVerifyLenient(code, _asList);
  }
  
  @Test
  public void test012_OverrideWithNewImports() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.concurrent.ThreadPoolExecutor;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("import java.util.concurrent.TimeUnit;");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("public class MyThreadPool extends ThreadPoolExecutor {");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("awaitTermination$");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("}");
    final CharSequence code = _builder;
    List<IJavaCompletionProposal> _exercise = this.exercise(code);
    final IJavaCompletionProposal proposal = IterableExtensions.<IJavaCompletionProposal>head(_exercise);
    Document _document = new Document();
    final Document d = _document;
    String _string = code.toString();
    d.set(_string);
    proposal.apply(d);
    String after = d.get();
    String _plus = ("couldn\'t find completion body in doc:\n" + after);
    StringConcatenation _builder_1 = new StringConcatenation();
    _builder_1.append("@Override");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("public boolean awaitTermination(long arg0, TimeUnit arg1)");
    _builder_1.newLine();
    _builder_1.append("\t\t\t\t");
    _builder_1.append("throws InterruptedException {");
    _builder_1.newLine();
    _builder_1.append("\t\t\t");
    _builder_1.append("// TODO Auto-generated method stub");
    _builder_1.newLine();
    _builder_1.append("\t\t\t");
    _builder_1.append("return super.awaitTermination(arg0, arg1);");
    _builder_1.newLine();
    _builder_1.append("\t\t");
    _builder_1.append("}");
    boolean _contains = after.contains(_builder_1);
    Assert.assertTrue(_plus, _contains);
  }
  
  @Test
  public void test008_ranking() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("String s=\"\"; s.has$");
    final CharSequence code = CodeBuilder.method(_builder);
    final List<IJavaCompletionProposal> actual = this.exercise(code);
    final Function1<IJavaCompletionProposal,Boolean> _function = new Function1<IJavaCompletionProposal,Boolean>() {
        public Boolean apply(final IJavaCompletionProposal p) {
          String _string = p.toString();
          boolean _startsWith = _string.startsWith("hashCode");
          return Boolean.valueOf(_startsWith);
        }
      };
    IJavaCompletionProposal _findFirst = IterableExtensions.<IJavaCompletionProposal>findFirst(actual, _function);
    final IJavaCompletionProposal pHashCode = ((IJavaCompletionProposal) _findFirst);
    final Function1<IJavaCompletionProposal,Boolean> _function_1 = new Function1<IJavaCompletionProposal,Boolean>() {
        public Boolean apply(final IJavaCompletionProposal p) {
          String _string = p.toString();
          boolean _startsWith = _string.startsWith("getChars");
          return Boolean.valueOf(_startsWith);
        }
      };
    IJavaCompletionProposal _findFirst_1 = IterableExtensions.<IJavaCompletionProposal>findFirst(actual, _function_1);
    final IJavaCompletionProposal pGetChars = ((IJavaCompletionProposal) _findFirst_1);
    int _relevance = pGetChars.getRelevance();
    int _relevance_1 = pHashCode.getRelevance();
    boolean _lessThan = (_relevance < _relevance_1);
    Assert.assertTrue(_lessThan);
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
    ArrayList<String> expected = CollectionLiterals.<String>newArrayList("aFile");
    this.exerciseAndVerify(code, expected);
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
    ArrayList<String> expected = CollectionLiterals.<String>newArrayList("aFile");
    this.exerciseAndVerify(code, expected);
  }
  
  public void smokeTest(final CharSequence code) {
    try {
      SubwordsCompletionProposalComputerIntegrationTest.fixture.clear();
      String _string = code.toString();
      final Tuple<ICompilationUnit,Set<Integer>> struct = SubwordsCompletionProposalComputerIntegrationTest.fixture.createFileAndParseWithMarkers(_string);
      final ICompilationUnit cu = struct.getFirst();
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
          NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
          sut.computeCompletionProposals(ctx, _nullProgressMonitor);
          this.stopwatch.stop();
        }
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void exerciseAndVerifyLenient(final CharSequence code, final List<String> expected) {
    final List<IJavaCompletionProposal> actual = this.exercise(code);
    for (final String e : expected) {
      {
        final Function1<IJavaCompletionProposal,Boolean> _function = new Function1<IJavaCompletionProposal,Boolean>() {
            public Boolean apply(final IJavaCompletionProposal p) {
              String _string = p.toString();
              boolean _startsWith = _string.startsWith(e);
              return Boolean.valueOf(_startsWith);
            }
          };
        final IJavaCompletionProposal match = IterableExtensions.<IJavaCompletionProposal>findFirst(actual, _function);
        Assert.assertNotNull(match);
        this.applyProposal(match, code);
        actual.remove(match);
        boolean _isEmpty = actual.isEmpty();
        if (_isEmpty) {
          return;
        }
      }
    }
  }
  
  public void applyProposal(final IJavaCompletionProposal proposal, final CharSequence code) {
    String _string = code.toString();
    Document _document = new Document(_string);
    final Document doc = _document;
    proposal.apply(doc);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("applying template ");
    _builder.append(proposal, "");
    _builder.append(" on code ");
    _builder.append(code, "");
    _builder.append(" failed.");
    String _string_1 = _builder.toString();
    String _get = doc.get();
    int _length = _get.length();
    int _length_1 = code.length();
    boolean _greaterThan = (_length > _length_1);
    Assert.assertTrue(_string_1, _greaterThan);
  }
  
  public void exerciseAndVerify(final CharSequence code, final List<String> expected) {
    final List<IJavaCompletionProposal> actual = this.exercise(code);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append(" ");
    _builder.append("some expected values were not found.\\nExpected: ");
    _builder.append(expected, " ");
    _builder.append(",\\nFound: ");
    _builder.append(actual, " ");
    _builder.append(" ");
    String _string = _builder.toString();
    int _size = expected.size();
    int _size_1 = actual.size();
    Assert.assertEquals(_string, _size, _size_1);
    for (final String e : expected) {
      {
        final Function1<IJavaCompletionProposal,Boolean> _function = new Function1<IJavaCompletionProposal,Boolean>() {
            public Boolean apply(final IJavaCompletionProposal p) {
              String _string = p.toString();
              boolean _startsWith = _string.startsWith(e);
              return Boolean.valueOf(_startsWith);
            }
          };
        final IJavaCompletionProposal match = IterableExtensions.<IJavaCompletionProposal>findFirst(actual, _function);
        Assert.assertNotNull(match);
        this.applyProposal(match, code);
        actual.remove(match);
      }
    }
  }
  
  public List<IJavaCompletionProposal> exercise(final CharSequence code) {
    try {
      SubwordsCompletionProposalComputerIntegrationTest.fixture.clear();
      String _string = code.toString();
      final Tuple<ICompilationUnit,Set<Integer>> struct = SubwordsCompletionProposalComputerIntegrationTest.fixture.createFileAndParseWithMarkers(_string);
      final ICompilationUnit cu = struct.getFirst();
      Set<Integer> _second = struct.getSecond();
      final Integer completionIndex = IterableExtensions.<Integer>head(_second);
      JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
      final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
      SubwordsCompletionProposalComputer _subwordsCompletionProposalComputer = new SubwordsCompletionProposalComputer();
      final SubwordsCompletionProposalComputer sut = _subwordsCompletionProposalComputer;
      sut.sessionStarted();
      this.stopwatch.start();
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      final List actual = sut.computeCompletionProposals(ctx, _nullProgressMonitor);
      this.stopwatch.stop();
      return actual;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void failIfComputerTookTooLong(final CharSequence code) {
    long _elapsedMillis = this.stopwatch.elapsedMillis();
    boolean _greaterThan = (_elapsedMillis > this.MAX_COMPUTATION_LIMIT_MILLIS);
    if (_greaterThan) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("completion took FAR too long: ");
      long _elapsedMillis_1 = this.stopwatch.elapsedMillis();
      _builder.append(_elapsedMillis_1, "");
      _builder.append("\\n in:\\n");
      _builder.append(code, "");
      String _string = _builder.toString();
      Assert.fail(_string);
    }
  }
}
