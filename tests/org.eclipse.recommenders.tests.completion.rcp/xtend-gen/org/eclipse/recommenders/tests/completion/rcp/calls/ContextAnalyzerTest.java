package org.eclipse.recommenders.tests.completion.rcp.calls;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.recommenders.internal.completion.rcp.calls.engine.AstBasedObjectUsageResolver;
import org.eclipse.recommenders.internal.utils.codestructs.ObjectUsage;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("all")
public class ContextAnalyzerTest {
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
      ContextAnalyzerTest.fixture.clear();
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCalls() {
    final CharSequence code = this.methodbody("\n\t\t\tExecutorService pool;\n\t\t\tpool.shutdown();\n\t\t\tpool.hashCode();\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "pool");
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("shutdown", "hashCode");
    this.assertCalls(res, _newArrayList);
  }
  
  /**
   * documentation purpose: we simply match on variable names.
   * We do no control flow or variable scope analysis!
   */
  @Test
  public void testCallsOnReusedVar() {
    final CharSequence code = this.methodbody("\n\t\t\tObject o = new Object();\n\t\t\to.hashCode();\n\t\t\to = new Object();\n\t\t\to.equals(null);\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "o");
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("hashCode", "equals");
    this.assertCalls(res, _newArrayList);
    this.assertDef(res, "<init>");
  }
  
  @Test
  public void testCallsOnParam01() {
    final CharSequence code = this.classbody("\n\t\tpublic void m1(String s$){\n\t\t\thashCode();\n\t\t}\n\t\t");
    final ObjectUsage res = this.exercise(code, "s");
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
    this.assertCalls(res, _newArrayList);
    this.assertDef(res, "m1");
  }
  
  @Test
  public void testCallsOnThisAndSuper() {
    final CharSequence code = this.methodbody("\n\t\t\thashCode();\n\t\t\tsuper.wait();\n\t\t\tthis.equals(null);\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "this");
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("hashCode", "wait", "equals");
    this.assertCalls(res, _newArrayList);
  }
  
  @Test
  public void testCallsSuperConstructor() {
    final CharSequence code = this.classbody("\n\t\t\tMyClass() {\n\t\t\t\tsuper();\n\t\t\t\t$\n\t\t\t}\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "this");
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("<init>");
    this.assertCalls(res, _newArrayList);
  }
  
  @Test
  public void testCallThisConstructor() {
    final CharSequence code = this.classbody("\n\t\t\tMyClass() {\n\t\t\t}\n\n\t\t\tMyClass(String s) {\n\t\t\t\tthis();\n\t\t\t\t$\n\t\t\t}\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "this");
    ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("<init>");
    this.assertCalls(res, _newArrayList);
  }
  
  @Test
  public void testDefConstructor() {
    final CharSequence code = this.methodbody("\n\t\t\tObject o = new Object();\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "o");
    this.assertDef(res, "<init>");
    this.assertType(res, "Object");
  }
  
  @Test
  public void testDefMethodReturn() {
    final CharSequence code = this.methodbody("\n\t\t\tExecutorService pool = Executors.newCachedThreadPool();\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "pool");
    this.assertDef(res, "newCachedThreadPool");
    this.assertType(res, "ExecutorService");
  }
  
  @Test
  public void testDefSuperMethodReturn() {
    final CharSequence code = this.methodbody("\n\t\t\tint hash = super.hashCode();\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "hash");
    this.assertDef(res, "hashCode");
    this.assertType(res, "I");
  }
  
  @Test
  public void testDefOnCallChain() {
    final CharSequence code = this.methodbody("\n\t\t\tint i = Executors.newCachedThreadPool().hashCode();\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "i");
    this.assertDef(res, "hashCode");
    this.assertType(res, "I");
  }
  
  @Test
  public void testDefOnAlias() {
    final CharSequence code = this.methodbody("\n\t\t\tint i = 23;\n\t\t\tint j = i;\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "j");
    Assert.assertNull(res.definition);
    this.assertType(res, "I");
  }
  
  @Test
  public void testDefAssignment() {
    final CharSequence code = this.methodbody("\n\t\t\tint i,j = 23;\n\t\t\tj = i;\n\t\t\t");
    final ObjectUsage res = this.exercise(code, "j");
    Assert.assertNull(res.definition);
    this.assertType(res, "I");
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
    _builder.append("\t");
    _builder.append("$");
    _builder.newLine();
    _builder.append("}");
    _builder.newLine();
    CharSequence _classbody = this.classbody(_builder);
    return _classbody;
  }
  
  private ObjectUsage exercise(final CharSequence code, final String varname) {
    try {
      final Tuple<ICompilationUnit,Set<Integer>> struct = ContextAnalyzerTest.fixture.createFileAndParseWithMarkers(code);
      final ICompilationUnit icu = struct.getFirst();
      final CompilationUnit cu = SharedASTProvider.getAST(icu, SharedASTProvider.WAIT_YES, null);
      Set<Integer> _second = struct.getSecond();
      final Integer pos = IterableExtensions.<Integer>head(_second);
      final MethodDeclaration enclosingMethod = this.findEnclosingMethod(cu, (pos).intValue());
      AstBasedObjectUsageResolver _astBasedObjectUsageResolver = new AstBasedObjectUsageResolver();
      final AstBasedObjectUsageResolver sut = _astBasedObjectUsageResolver;
      return sut.findObjectUsage(varname, enclosingMethod);
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private MethodDeclaration findEnclosingMethod(final CompilationUnit ast, final int pos) {
    MethodDeclaration _xblockexpression = null;
    {
      final ASTNode node = NodeFinder.perform(ast, pos, 0);
      ASTNode parent = node;
      boolean _not = (!(parent instanceof MethodDeclaration));
      boolean _while = _not;
      while (_while) {
        ASTNode _parent = parent.getParent();
        parent = _parent;
        boolean _not_1 = (!(parent instanceof MethodDeclaration));
        _while = _not_1;
      }
      _xblockexpression = (((MethodDeclaration) parent));
    }
    return _xblockexpression;
  }
  
  private boolean assertType(final ObjectUsage usage, final String simpleTypeName) {
    boolean _xblockexpression = false;
    {
      Assert.assertNotNull(usage.type);
      String _className = usage.type.getClassName();
      boolean _equals = _className.equals(simpleTypeName);
      _xblockexpression = (_equals);
    }
    return _xblockexpression;
  }
  
  private void assertCalls(final ObjectUsage usage, final List<String> methods) {
    for (final String name : methods) {
      {
        final Function1<IMethodName,Boolean> _function = new Function1<IMethodName,Boolean>() {
            public Boolean apply(final IMethodName e) {
              String _name = e.getName();
              boolean _equals = _name.equals(name);
              return Boolean.valueOf(_equals);
            }
          };
        final IMethodName match = IterableExtensions.<IMethodName>findFirst(usage.calls, _function);
        boolean _equals = Objects.equal(match, null);
        if (_equals) {
          String _plus = ("method " + name);
          String _plus_1 = (_plus + " not found in ");
          String _plus_2 = (_plus_1 + usage.calls);
          Assert.fail(_plus_2);
        }
      }
    }
    int _size = methods.size();
    int _size_1 = usage.calls.size();
    Assert.assertEquals("expected calls not same size as actual", _size, _size_1);
  }
  
  private void assertDef(final ObjectUsage usage, final String method) {
    boolean _equals = Objects.equal(usage.definition, null);
    if (_equals) {
      Assert.fail("no definition found");
    }
    String _name = usage.definition.getName();
    boolean _equals_1 = _name.equals(method);
    boolean _not = (!_equals_1);
    if (_not) {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Def did not match ");
      _builder.append(method, "");
      String _string = _builder.toString();
      Assert.fail(_string);
    }
  }
}
