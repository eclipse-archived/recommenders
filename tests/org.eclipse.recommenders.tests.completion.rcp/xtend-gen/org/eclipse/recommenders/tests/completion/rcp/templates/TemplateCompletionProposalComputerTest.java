package org.eclipse.recommenders.tests.completion.rcp.templates;

import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.recommenders.internal.completion.rcp.templates.TemplatesCompletionProposalComputer;
import org.eclipse.recommenders.internal.completion.rcp.templates.TemplatesCompletionProposalComputer.CompletionMode;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.completion.rcp.calls.ModelStoreMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("all")
public class TemplateCompletionProposalComputerTest {
  private TemplatesCompletionProposalComputer sut;
  
  private List<IJavaCompletionProposal> proposals;
  
  private CharSequence code;
  
  @Test
  public void testThis() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.THIS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("", _variableName);
  }
  
  @Test
  public void testThisWithThisPrefix() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("this.$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.THIS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("", _variableName);
  }
  
  @Test
  public void testThisWithSuperPrefix() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("super.$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.THIS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("", _variableName);
  }
  
  @Test
  public void testThisWithMethodPrefix() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("eq$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.THIS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("eq", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("", _variableName);
  }
  
  @Test
  public void testType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("List$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.TYPE_NAME, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("", _variableName);
  }
  
  @Test
  public void testQualifiedType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("java.util.List$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.TYPE_NAME, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("", _variableName);
  }
  
  @Test
  public void testThisOnVariableName() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Event evt;");
    _builder.newLine();
    _builder.append("evt$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.THIS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("evt", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("", _variableName);
  }
  
  @Test
  public void testBehindQualifiedType() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("List $");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertNull(_completionMode);
  }
  
  @Test
  public void testMemberAccess() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Event evt;");
    _builder.newLine();
    _builder.append("evt.$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.MEMBER_ACCESS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("evt", _variableName);
  }
  
  @Test
  public void testQualifiedMemberAccess() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Event evt;");
    _builder.newLine();
    _builder.append("evt.evt.$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.MEMBER_ACCESS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("evt.evt", _variableName);
  }
  
  @Test
  public void testQualifiedMemberAccessWithMethodPrefix() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Event evt;");
    _builder.newLine();
    _builder.append("evt.evt.eq$");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(CompletionMode.MEMBER_ACCESS, _completionMode);
    String _methodPrefix = this.sut.getMethodPrefix();
    Assert.assertEquals("eq", _methodPrefix);
    String _variableName = this.sut.getVariableName();
    Assert.assertEquals("evt.evt", _variableName);
  }
  
  @Test
  @Ignore(value = "Not possible to distinguish this case and testThisOnVariableName")
  public void testNoTemplates() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Event evt = $");
    _builder.newLine();
    CharSequence _method = CodeBuilder.method(_builder);
    this.code = _method;
    this.exercise();
    CompletionMode _completionMode = this.sut.getCompletionMode();
    Assert.assertEquals(null, _completionMode);
  }
  
  private List<IJavaCompletionProposal> exercise() {
    try {
      List<IJavaCompletionProposal> _xblockexpression = null;
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        String _string = this.code.toString();
        final Tuple<ICompilationUnit,Set<Integer>> struct = fixture.createFileAndParseWithMarkers(_string);
        final ICompilationUnit cu = struct.getFirst();
        cu.becomeWorkingCopy(null);
        final CompilationUnit ast = cu.reconcile(AST.JLS4, true, true, null, null);
        Assert.assertNotNull(ast);
        RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
        ModelStoreMock _modelStoreMock = new ModelStoreMock();
        JavaElementResolver _javaElementResolver = new JavaElementResolver();
        TemplatesCompletionProposalComputer _templatesCompletionProposalComputer = new TemplatesCompletionProposalComputer(_recommendersCompletionContextFactoryMock, _modelStoreMock, _javaElementResolver);
        this.sut = _templatesCompletionProposalComputer;
        Set<Integer> _second = struct.getSecond();
        final Integer pos = IterableExtensions.<Integer>head(_second);
        JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (pos).intValue());
        List _computeCompletionProposals = this.sut.computeCompletionProposals(_javaContentAssistContextMock, null);
        List<IJavaCompletionProposal> _proposals = this.proposals = _computeCompletionProposals;
        _xblockexpression = (_proposals);
      }
      return _xblockexpression;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
