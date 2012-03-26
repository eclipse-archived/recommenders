package org.eclipse.recommenders.tests.completion.rcp.calls;

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
import org.eclipse.xtext.xbase.lib.BooleanExtensions;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
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
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tExecutorService pool;\r\n\t\t\tpool.shutdown();\r\n\t\t\tpool.hashCode();\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "pool");
      final ObjectUsage res = _exercise;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("shutdown", "hashCode");
      this.assertCalls(res, _newArrayList);
  }
  
  /**
   * documentation purpose: we simply match on variable names.
   * We do no control flow or variable scope analysis!
   */
  @Test
  public void testCallsOnReusedVar() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tObject o = new Object();\r\n\t\t\to.hashCode();\r\n\t\t\to = new Object();\r\n\t\t\to.equals(null);\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "o");
      final ObjectUsage res = _exercise;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("hashCode", "equals");
      this.assertCalls(res, _newArrayList);
      this.assertDef(res, "<init>");
  }
  
  @Test
  public void testCallsOnParam01() {
      CharSequence _classbody = this.classbody("\r\n\t\tpublic void m1(String s$){\r\n\t\t\thashCode();\r\n\t\t}\r\n\t\t");
      final CharSequence code = _classbody;
      ObjectUsage _exercise = this.exercise(code, "s");
      final ObjectUsage res = _exercise;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList();
      this.assertCalls(res, _newArrayList);
      this.assertDef(res, "m1");
  }
  
  @Test
  public void testCallsOnThisAndSuper() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\thashCode();\r\n\t\t\tsuper.wait();\r\n\t\t\tthis.equals(null);\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "this");
      final ObjectUsage res = _exercise;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("hashCode", "wait", "equals");
      this.assertCalls(res, _newArrayList);
  }
  
  @Test
  public void testCallsSuperConstructor() {
      CharSequence _classbody = this.classbody("\r\n\t\t\tMyClass() {\r\n\t\t\t\tsuper();\r\n\t\t\t\t$\r\n\t\t\t}\r\n\t\t\t");
      final CharSequence code = _classbody;
      ObjectUsage _exercise = this.exercise(code, "this");
      final ObjectUsage res = _exercise;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("<init>");
      this.assertCalls(res, _newArrayList);
  }
  
  @Test
  public void testCallThisConstructor() {
      CharSequence _classbody = this.classbody("\r\n\t\t\tMyClass() {\r\n\t\t\t}\r\n\r\n\t\t\tMyClass(String s) {\r\n\t\t\t\tthis();\r\n\t\t\t\t$\r\n\t\t\t}\r\n\t\t\t");
      final CharSequence code = _classbody;
      ObjectUsage _exercise = this.exercise(code, "this");
      final ObjectUsage res = _exercise;
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("<init>");
      this.assertCalls(res, _newArrayList);
  }
  
  @Test
  public void testDefConstructor() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tObject o = new Object();\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "o");
      final ObjectUsage res = _exercise;
      this.assertDef(res, "<init>");
      this.assertType(res, "Object");
  }
  
  @Test
  public void testDefMethodReturn() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tExecutorService pool = Executors.newCachedThreadPool();\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "pool");
      final ObjectUsage res = _exercise;
      this.assertDef(res, "newCachedThreadPool");
      this.assertType(res, "ExecutorService");
  }
  
  @Test
  public void testDefSuperMethodReturn() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tint hash = super.hashCode();\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "hash");
      final ObjectUsage res = _exercise;
      this.assertDef(res, "hashCode");
      this.assertType(res, "I");
  }
  
  @Test
  public void testDefOnCallChain() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tint i = Executors.newCachedThreadPool().hashCode();\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "i");
      final ObjectUsage res = _exercise;
      this.assertDef(res, "hashCode");
      this.assertType(res, "I");
  }
  
  @Test
  public void testDefOnAlias() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tint i = 23;\r\n\t\t\tint j = i;\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "j");
      final ObjectUsage res = _exercise;
      Assert.assertNull(res.definition);
      this.assertType(res, "I");
  }
  
  @Test
  public void testDefAssignment() {
      CharSequence _methodbody = this.methodbody("\r\n\t\t\tint i,j = 23;\r\n\t\t\tj = i;\r\n\t\t\t");
      final CharSequence code = _methodbody;
      ObjectUsage _exercise = this.exercise(code, "j");
      final ObjectUsage res = _exercise;
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
      {
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = ContextAnalyzerTest.fixture.createFileAndParseWithMarkers(code);
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit icu = _first;
        CompilationUnit _aST = SharedASTProvider.getAST(icu, SharedASTProvider.WAIT_YES, null);
        final CompilationUnit cu = _aST;
        Set<Integer> _second = struct.getSecond();
        Integer _head = IterableExtensions.<Integer>head(_second);
        final Integer pos = _head;
        MethodDeclaration _findEnclosingMethod = this.findEnclosingMethod(cu, (pos).intValue());
        final MethodDeclaration enclosingMethod = _findEnclosingMethod;
        AstBasedObjectUsageResolver _astBasedObjectUsageResolver = new AstBasedObjectUsageResolver();
        final AstBasedObjectUsageResolver sut = _astBasedObjectUsageResolver;
        ObjectUsage _findObjectUsage = sut.findObjectUsage(varname, enclosingMethod);
        return _findObjectUsage;
      }
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private MethodDeclaration findEnclosingMethod(final CompilationUnit ast, final int pos) {
    MethodDeclaration _xblockexpression = null;
    {
      ASTNode _perform = NodeFinder.perform(ast, pos, 0);
      final ASTNode node = _perform;
      ASTNode parent = node;
      boolean _operator_not = BooleanExtensions.operator_not((parent instanceof MethodDeclaration));
      boolean _while = _operator_not;
      while (_while) {
        ASTNode _parent = parent.getParent();
        parent = _parent;
        boolean _operator_not_1 = BooleanExtensions.operator_not((parent instanceof MethodDeclaration));
        _while = _operator_not_1;
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
          IMethodName _findFirst = IterableExtensions.<IMethodName>findFirst(usage.calls, _function);
          final IMethodName match = _findFirst;
          boolean _operator_equals = ObjectExtensions.operator_equals(match, null);
          if (_operator_equals) {
            String _operator_plus = StringExtensions.operator_plus("method ", name);
            String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, " not found in ");
            String _operator_plus_2 = StringExtensions.operator_plus(_operator_plus_1, usage.calls);
            Assert.fail(_operator_plus_2);
          }
        }
      }
      int _size = methods.size();
      int _size_1 = usage.calls.size();
      Assert.assertEquals("expected calls not same size as actual", _size, _size_1);
  }
  
  private Object assertDef(final ObjectUsage usage, final String method) {
    Object _xblockexpression = null;
    {
      boolean _operator_equals = ObjectExtensions.operator_equals(usage.definition, null);
      if (_operator_equals) {
        Assert.fail("no definition found");
      }
      Object _xifexpression = null;
      String _name = usage.definition.getName();
      boolean _equals = _name.equals(method);
      boolean _operator_not = BooleanExtensions.operator_not(_equals);
      if (_operator_not) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("Def did not match ");
        _builder.append(method, "");
        String _string = _builder.toString();
        Assert.fail(_string);
      }
      _xblockexpression = (_xifexpression);
    }
    return _xblockexpression;
  }
}
