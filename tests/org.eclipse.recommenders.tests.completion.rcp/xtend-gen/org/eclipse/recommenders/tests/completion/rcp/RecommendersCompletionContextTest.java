package org.eclipse.recommenders.tests.completion.rcp;

import com.google.common.base.Optional;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Test;

@SuppressWarnings("all")
public class RecommendersCompletionContextTest {
  @Test
  public void test01() {
      CharSequence _methodbody = this.methodbody("s1.$;");
      final CharSequence code = _methodbody;
      IRecommendersCompletionContext _exercise = this.exercise(code);
      final IRecommendersCompletionContext sut = _exercise;
      this.assertCompletionNode(sut, org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference.class);
      this.assertCompletionNodeParentIsNull(sut);
  }
  
  @Test
  public void test02() {
      CharSequence _methodbody = this.methodbody("s1.equals(s1.$);");
      final CharSequence code = _methodbody;
      IRecommendersCompletionContext _exercise = this.exercise(code);
      final IRecommendersCompletionContext sut = _exercise;
      this.assertCompletionNode(sut, org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference.class);
      this.assertCompletionNodeParent(sut, org.eclipse.jdt.internal.compiler.ast.MessageSend.class);
  }
  
  @Test
  public void test03() {
      CharSequence _methodbody = this.methodbody("String s1 = new String();\n\t\t\ts1.\n\t\t\tString s2 = new String();\n\t\t\ts2.$");
      final CharSequence code = _methodbody;
      IRecommendersCompletionContext _exercise = this.exercise(code);
      final IRecommendersCompletionContext sut = _exercise;
      Optional<?> _absent = Optional.absent();
      Optional<IType> _receiverType = sut.getReceiverType();
      Assert.assertEquals(_absent, _receiverType);
  }
  
  private void assertCompletionNode(final IRecommendersCompletionContext sut, final Class<?> type) {
      ASTNode _completionNode = sut.getCompletionNode();
      final ASTNode node = _completionNode;
      this.assertInstanceof(node, type);
  }
  
  private void assertInstanceof(final ASTNode node, final Class<?> type) {
      Assert.assertNotNull("completion node is null!", node);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("unexpected completion node type. Expected ");
      _builder.append(type, "");
      _builder.append(" but got ");
      Class<? extends Object> _class = node.getClass();
      _builder.append(_class, "");
      String _string = _builder.toString();
      Class<? extends Object> _class_1 = node.getClass();
      Assert.assertEquals(_string, type, _class_1);
  }
  
  private void assertCompletionNodeParent(final IRecommendersCompletionContext sut, final Class<?> type) {
      ASTNode _completionNodeParent = sut.getCompletionNodeParent();
      final ASTNode node = _completionNodeParent;
      this.assertInstanceof(node, type);
  }
  
  private void assertCompletionNodeParentIsNull(final IRecommendersCompletionContext sut) {
    ASTNode _completionNodeParent = sut.getCompletionNodeParent();
    Assert.assertNull("parent node is not null!", _completionNodeParent);
  }
  
  public IRecommendersCompletionContext exercise(final CharSequence code) {
    try {
      IRecommendersCompletionContext _xblockexpression = null;
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        String _string = code.toString();
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(_string, "MyClass.java");
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        Set<Integer> _second = struct.getSecond();
        Integer _head = IterableExtensions.<Integer>head(_second);
        final Integer completionIndex = _head;
        JavaContentAssistContextMock _javaContentAssistContextMock = new JavaContentAssistContextMock(cu, (completionIndex).intValue());
        final JavaContentAssistContextMock ctx = _javaContentAssistContextMock;
        RecommendersCompletionContextFactoryMock _recommendersCompletionContextFactoryMock = new RecommendersCompletionContextFactoryMock();
        IRecommendersCompletionContext _create = _recommendersCompletionContextFactoryMock.create(ctx);
        _xblockexpression = (_create);
      }
      return _xblockexpression;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private CharSequence classbody(final CharSequence classbody) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("import java.util.concurrent.*;");
    _builder.newLine();
    _builder.append("public class MyClass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("String s1;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("String s2;");
    _builder.newLine();
    _builder.append("\t");
    _builder.append(classbody, "	");
    _builder.newLineIfNotEmpty();
    _builder.append("}");
    _builder.newLine();
    return _builder;
  }
  
  private CharSequence methodbody(final CharSequence methodbody) {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("void test() {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append(methodbody, "	");
    _builder.newLineIfNotEmpty();
    CharSequence _classbody = this.classbody(_builder);
    return _classbody;
  }
}
