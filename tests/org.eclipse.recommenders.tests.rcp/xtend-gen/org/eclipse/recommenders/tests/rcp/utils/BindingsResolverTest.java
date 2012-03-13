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
      ArrayList<String> _newArrayList = CollectionLiterals.<String>newArrayList("String", "Integer", "List", "List<Integer>", "List<? extends Number>");
      final ArrayList<String> names = _newArrayList;
      for (final String name : names) {
        {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append(name, "");
          _builder.append("$ var;");
          CharSequence _method = CodeBuilder.method(_builder);
          final CharSequence code = _method;
          ITypeBinding _findTypeBinding = this.findTypeBinding(code);
          final ITypeBinding binding = _findTypeBinding;
          Optional<ITypeName> _typeName = BindingUtils.toTypeName(binding);
          ITypeName _get = _typeName.get();
          final ITypeName recTypeName = _get;
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
      Tuple<CompilationUnit,Set<Integer>> _parseWithMarkers = fixture.parseWithMarkers(_string);
      final Tuple<CompilationUnit,Set<Integer>> struct = _parseWithMarkers;
      CompilationUnit _first = struct.getFirst();
      final CompilationUnit cu = _first;
      Set<Integer> _second = struct.getSecond();
      Integer _head = IterableExtensions.<Integer>head(_second);
      final Integer pos = _head;
      ASTNode _perform = NodeFinder.perform(cu, (pos).intValue(), 0);
      final ASTNode selection = _perform;
      boolean matched = false;
      if (!matched) {
        if (selection instanceof SimpleName) {
          final SimpleName _simpleName = (SimpleName)selection;
          matched=true;
          IBinding _resolveBinding = _simpleName.resolveBinding();
          return ((ITypeBinding) _resolveBinding);
        }
      }
      if (!matched) {
        if (selection instanceof TypeParameter) {
          final TypeParameter _typeParameter = (TypeParameter)selection;
          matched=true;
          ITypeBinding _resolveBinding = _typeParameter.resolveBinding();
          return _resolveBinding;
        }
      }
      if (!matched) {
        if (selection instanceof ParameterizedType) {
          final ParameterizedType _parameterizedType = (ParameterizedType)selection;
          matched=true;
          ITypeBinding _resolveBinding = _parameterizedType.resolveBinding();
          return _resolveBinding;
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
