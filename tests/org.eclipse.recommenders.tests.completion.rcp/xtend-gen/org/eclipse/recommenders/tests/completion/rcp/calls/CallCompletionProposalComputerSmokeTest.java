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
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.CallsCompletionProposalComputer;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
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
  public void testStdCompletion() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("o.$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testOnConstructor() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("new Object().$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testOnReturn() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("l.get(0).$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testInIf() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("if(o.$)");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testExpectsPrimitive() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("int i = o.$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testExpectsNonPrimitive() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Object o1 = o.$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testInMessageSend() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("l.add(o.$)");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testPrefix01() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("o.has$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testPrefix02() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("o.hashc$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testPrefix03() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("o.hashC$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code);
  }
  
  @Test
  public void testPrefix04() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("o.x$");
      CharSequence _method = this.method(_builder);
      final CharSequence code = _method;
      this.test(code, 0);
  }
  
  private void test(final CharSequence code) {
    this.test(code, 1);
  }
  
  private void test(final CharSequence code, final int numberOfExpectedProposals) {
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
          {
            JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (pos).intValue());
            List<ICompletionProposal> _computeCompletionProposals = sut.computeCompletionProposals(_javaContentAssistContextMock, null);
            final List<ICompletionProposal> proposals = _computeCompletionProposals;
            int _size = proposals.size();
            Assert.assertEquals("wrong number of proposals", numberOfExpectedProposals, _size);
          }
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
        Integer _head = IterableExtensions.<Integer>head(_second);
        JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (_head).intValue());
        NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
        List<ICompletionProposal> _computeCompletionProposals = sut.computeCompletionProposals(_javaContentAssistContextMock, _nullProgressMonitor);
        final List<ICompletionProposal> proposals = _computeCompletionProposals;
        Tuple<?,?> _newTuple = Tuple.newTuple(proposals, sut);
        _xblockexpression = (((Tuple) _newTuple));
      }
      return _xblockexpression;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
