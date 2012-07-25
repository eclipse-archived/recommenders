package org.eclipse.recommenders.tests.completion.rcp;

import com.google.common.base.Optional;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnMemberAccess;
import org.eclipse.jdt.internal.codeassist.complete.CompletionOnQualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.recommenders.completion.rcp.IRecommendersCompletionContext;
import org.eclipse.recommenders.tests.completion.rcp.JavaContentAssistContextMock;
import org.eclipse.recommenders.tests.completion.rcp.RecommendersCompletionContextFactoryMock;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Test;

@SuppressWarnings("all")
public class RecommendersCompletionContextTest {
  @Test
  public void test01() {
    final CharSequence code = this.methodbody("s1.$;");
    final IRecommendersCompletionContext sut = this.exercise(code);
    this.assertCompletionNode(sut, CompletionOnQualifiedNameReference.class);
    this.assertCompletionNodeParentIsNull(sut);
  }
  
  @Test
  public void test02() {
    final CharSequence code = this.methodbody("s1.equals(s1.$);");
    final IRecommendersCompletionContext sut = this.exercise(code);
    this.assertCompletionNode(sut, CompletionOnQualifiedNameReference.class);
    this.assertCompletionNodeParent(sut, MessageSend.class);
  }
  
  @Test
  public void test03() {
    final CharSequence code = this.methodbody("String s1 = new String();\n\t\t\ts1.\n\t\t\tString s2 = new String();\n\t\t\ts2.$");
    final IRecommendersCompletionContext sut = this.exercise(code);
    Optional<?> _absent = Optional.absent();
    Optional<IType> _receiverType = sut.getReceiverType();
    Assert.assertEquals(_absent, _receiverType);
  }
  
  @Test
  public void test04() {
    final CharSequence code = this.methodbody("s1.concat(\"\").$;");
    final IRecommendersCompletionContext sut = this.exercise(code);
    this.assertCompletionNode(sut, CompletionOnMemberAccess.class);
    Optional<IMethodName> _methodDef = sut.getMethodDef();
    boolean _isPresent = _methodDef.isPresent();
    Assert.assertTrue(_isPresent);
  }
  
  @Test
  public void testTypeParameters01() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public <T> void m(T t){t.$}");
    final CharSequence code = this.classbody(_builder);
    final IRecommendersCompletionContext sut = this.exercise(code);
    Optional<IType> _receiverType = sut.getReceiverType();
    boolean _isPresent = _receiverType.isPresent();
    Assert.assertTrue(_isPresent);
    Optional<IType> _receiverType_1 = sut.getReceiverType();
    IType _get = _receiverType_1.get();
    String _elementName = _get.getElementName();
    Assert.assertEquals("Object", _elementName);
  }
  
  @Test
  public void testTypeParameters02() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public <T extends Collection> void m(T t){t.$}");
    final CharSequence code = this.classbody(_builder);
    final IRecommendersCompletionContext sut = this.exercise(code);
    Optional<IType> _receiverType = sut.getReceiverType();
    boolean _isPresent = _receiverType.isPresent();
    Assert.assertTrue(_isPresent);
    Optional<IType> _receiverType_1 = sut.getReceiverType();
    IType _get = _receiverType_1.get();
    String _elementName = _get.getElementName();
    Assert.assertEquals("Collection", _elementName);
  }
  
  @Test
  public void testTypeParameters021() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public <T super List> void m(T t){t.$}");
    final CharSequence code = this.classbody(_builder);
    final IRecommendersCompletionContext sut = this.exercise(code);
    Optional<IType> _receiverType = sut.getReceiverType();
    boolean _isPresent = _receiverType.isPresent();
    Assert.assertFalse(_isPresent);
  }
  
  @Test
  public void testTypeParameters03() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Class<?> clazz = null;");
    _builder.newLine();
    _builder.append("clazz.$");
    final CharSequence code = this.methodbody(_builder);
    final IRecommendersCompletionContext sut = this.exercise(code);
    Optional<IType> _receiverType = sut.getReceiverType();
    boolean _isPresent = _receiverType.isPresent();
    Assert.assertTrue(_isPresent);
    Optional<IType> _receiverType_1 = sut.getReceiverType();
    IType _get = _receiverType_1.get();
    String _elementName = _get.getElementName();
    Assert.assertEquals("Class", _elementName);
  }
  
  @Test
  public void testTypeParameters04() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Class<? super String> clazz = null;");
    _builder.newLine();
    _builder.append("clazz.$");
    final CharSequence code = this.methodbody(_builder);
    final IRecommendersCompletionContext sut = this.exercise(code);
    Optional<IType> _receiverType = sut.getReceiverType();
    boolean _isPresent = _receiverType.isPresent();
    Assert.assertTrue(_isPresent);
    Optional<IType> _receiverType_1 = sut.getReceiverType();
    IType _get = _receiverType_1.get();
    String _elementName = _get.getElementName();
    Assert.assertEquals("Class", _elementName);
  }
  
  public void testSignatureParseException() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("public class Part {");
    _builder.newLine();
    _builder.newLine();
    _builder.append("public Part(String string,$ S) {");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    final CharSequence code = _builder;
    final IRecommendersCompletionContext sut = this.exercise(code);
    Optional<?> _absent = Optional.absent();
    Optional<IJavaElement> _enclosingElement = sut.getEnclosingElement();
    Assert.assertEquals(_absent, _enclosingElement);
  }
  
  private void assertCompletionNode(final IRecommendersCompletionContext sut, final Class<? extends Object> type) {
    Optional<ASTNode> _completionNode = sut.getCompletionNode();
    final ASTNode node = _completionNode.orNull();
    this.assertInstanceof(node, type);
  }
  
  private void assertInstanceof(final ASTNode node, final Class<? extends Object> type) {
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
  
  private void assertCompletionNodeParent(final IRecommendersCompletionContext sut, final Class<? extends Object> type) {
    Optional<ASTNode> _completionNodeParent = sut.getCompletionNodeParent();
    final ASTNode node = _completionNodeParent.orNull();
    this.assertInstanceof(node, type);
  }
  
  private void assertCompletionNodeParentIsNull(final IRecommendersCompletionContext sut) {
    Optional<ASTNode> _completionNodeParent = sut.getCompletionNodeParent();
    ASTNode _orNull = _completionNodeParent.orNull();
    Assert.assertNull("parent node is not null!", _orNull);
  }
  
  public IRecommendersCompletionContext exercise(final CharSequence code) {
    try {
      IRecommendersCompletionContext _xblockexpression = null;
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        String _string = code.toString();
        final Tuple<ICompilationUnit,Set<Integer>> struct = fixture.createFileAndParseWithMarkers(_string);
        final ICompilationUnit cu = struct.getFirst();
        Set<Integer> _second = struct.getSecond();
        final Integer completionIndex = IterableExtensions.<Integer>head(_second);
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
