package org.eclipse.recommenders.tests.completion.rcp.calls;

import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.SmokeTestScenarios;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.completion.rcp.calls.CallsPreferenceStoreMock;
import org.eclipse.recommenders.tests.completion.rcp.calls.ModelStoreMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("all")
public class CallCompletionProposalComputerSmokeTest {
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
      CallCompletionProposalComputerSmokeTest.fixture.clear();
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public CharSequence method(final CharSequence code) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public void __test(Object o, List l) {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append(code, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    CharSequence _classbody = CodeBuilder.classbody(_builder);
    return _classbody;
  }
  
  @Test
  public void test000_smoke() {
    List<CharSequence> _scenarios = SmokeTestScenarios.scenarios();
    for (final CharSequence scenario : _scenarios) {
      CallCompletionProposalComputerSmokeTest.exercise(scenario);
    }
  }
  
  @Test
  public void testStdCompletion() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("o.$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testOnConstructor() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("new Object().$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testOnReturn() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("l.get(0).$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testInIf() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("if(o.$)");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testExpectsPrimitive() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("int i = o.$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testExpectsNonPrimitive() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Object o1 = o.$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testInMessageSend() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("l.add(o.$)");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testPrefix01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("o.has$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testPrefix02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("o.hashc$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testPrefix03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("o.hashC$");
    final CharSequence code = this.method(_builder);
    this.test(code);
  }
  
  @Test
  public void testPrefix04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("o.x$");
    final CharSequence code = this.method(_builder);
    this.test(code, 0);
  }
  
  private void test(final CharSequence code) {
    this.test(code, 1);
  }
  
  private void test(final CharSequence code, final int numberOfExpectedProposals) {
    try {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
      final JavaProjectFixture fixture = _javaProjectFixture;
      String _string = code.toString();
      final Tuple<ICompilationUnit,Set<Integer>> struct = fixture.createFileAndParseWithMarkers(_string);
      final ICompilationUnit cu = struct.getFirst();
      cu.becomeWorkingCopy(null);
      final CompilationUnit ast = cu.reconcile(AST.JLS4, true, true, null, null);
      Assert.assertNotNull(ast);
      ModelStoreMock _modelStoreMock = new ModelStoreMock();
      JavaElementResolver _javaElementResolver = new JavaElementResolver();
      RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
      IPreferenceStore _create = CallsPreferenceStoreMock.create();
      CallsCompletionProposalComputer _callsCompletionProposalComputer = new CallsCompletionProposalComputer(_modelStoreMock, _javaElementResolver, _recommendersCompletionContextFactoryMock, _create);
      final CallsCompletionProposalComputer sut = _callsCompletionProposalComputer;
      Set<Integer> _second = struct.getSecond();
      for (final Integer pos : _second) {
        {
          JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (pos).intValue());
          final List<ICompletionProposal> proposals = sut.computeCompletionProposals(_javaContentAssistContextMock, null);
          int _size = proposals.size();
          Assert.assertEquals("wrong number of proposals", numberOfExpectedProposals, _size);
        }
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static Tuple<List<IJavaCompletionProposal>,CallsCompletionProposalComputer> exercise(final CharSequence code) {
    try {
      Tuple _xblockexpression = null;
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        fixture.clear();
        String _string = code.toString();
        final Tuple<ICompilationUnit,Set<Integer>> struct = fixture.createFileAndParseWithMarkers(_string);
        final ICompilationUnit cu = struct.getFirst();
        cu.becomeWorkingCopy(null);
        final CompilationUnit ast = cu.reconcile(AST.JLS4, true, true, null, null);
        Assert.assertNotNull(ast);
        ModelStoreMock _modelStoreMock = new ModelStoreMock();
        JavaElementResolver _javaElementResolver = new JavaElementResolver();
        RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
        IPreferenceStore _create = CallsPreferenceStoreMock.create();
        CallsCompletionProposalComputer _callsCompletionProposalComputer = new CallsCompletionProposalComputer(_modelStoreMock, _javaElementResolver, _recommendersCompletionContextFactoryMock, _create);
        final CallsCompletionProposalComputer sut = _callsCompletionProposalComputer;
        Set<Integer> _second = struct.getSecond();
        Integer _head = IterableExtensions.<Integer>head(_second);
        JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (_head).intValue());
        NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
        final List<ICompletionProposal> proposals = sut.computeCompletionProposals(_javaContentAssistContextMock, _nullProgressMonitor);
        Tuple<?,?> _newTuple = Tuple.newTuple(proposals, sut);
        _xblockexpression = (((Tuple) _newTuple));
      }
      return _xblockexpression;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
