package org.eclipse.recommenders.tests.rcp.utils;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.Set;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.recommenders.utils.rcp.ast.BindingUtils;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class BindingsResolverTest {
  private JavaElementResolver jdtResolver = new Function0<JavaElementResolver>() {
    public JavaElementResolver apply() {
      JavaElementResolver _javaElementResolver = new JavaElementResolver();
      return _javaElementResolver;
    }
  }.apply();
  
  @Test
  public void test01() {
    final ArrayList<String> names = CollectionLiterals.<String>newArrayList("String", "Integer", "List", "List<Integer>", "List<? extends Number>");
    for (final String name : names) {
      {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append(name, "");
        _builder.append("$ var;");
        final CharSequence code = CodeBuilder.method(_builder);
        final ITypeBinding binding = this.findTypeBinding(code);
        Optional<ITypeName> _typeName = BindingUtils.toTypeName(binding);
        final ITypeName recTypeName = _typeName.get();
        Optional<IType> _jdtType = this.jdtResolver.toJdtType(recTypeName);
        boolean _isPresent = _jdtType.isPresent();
        Assert.assertTrue(_isPresent);
      }
    }
  }
  
  public ITypeBinding findTypeBinding(final CharSequence code) {
    IWorkspace _workspace = ResourcesPlugin.getWorkspace();
    JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
    final JavaProjectFixture fixture = _javaProjectFixture;
    String _string = code.toString();
    final Tuple<CompilationUnit,Set<Integer>> struct = fixture.parseWithMarkers(_string);
    final CompilationUnit cu = struct.getFirst();
    Set<Integer> _second = struct.getSecond();
    final Integer pos = IterableExtensions.<Integer>head(_second);
    final ASTNode selection = NodeFinder.perform(cu, (pos).intValue(), 0);
    boolean _matched = false;
    if (!_matched) {
      if (selection instanceof SimpleName) {
        final SimpleName _simpleName = (SimpleName)selection;
        _matched=true;
        IBinding _resolveBinding = _simpleName.resolveBinding();
        return ((ITypeBinding) _resolveBinding);
      }
    }
    if (!_matched) {
      if (selection instanceof TypeParameter) {
        final TypeParameter _typeParameter = (TypeParameter)selection;
        _matched=true;
        return _typeParameter.resolveBinding();
      }
    }
    if (!_matched) {
      if (selection instanceof ParameterizedType) {
        final ParameterizedType _parameterizedType = (ParameterizedType)selection;
        _matched=true;
        return _parameterizedType.resolveBinding();
      }
    }
    IllegalArgumentException _illegalArgumentException = new IllegalArgumentException();
    throw _illegalArgumentException;
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
}
