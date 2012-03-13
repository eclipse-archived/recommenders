package org.eclipse.recommenders.tests.rcp.utils;

import com.google.common.base.Optional;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Test;

@SuppressWarnings("all")
public class JavaElementResolver4GenericsTest {
  private JavaElementResolver sut = new Function0<JavaElementResolver>() {
    public JavaElementResolver apply() {
      JavaElementResolver _javaElementResolver = new JavaElementResolver();
      return _javaElementResolver;
    }
  }.apply();
  
  @Test
  public void testBoundReturn() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public Iterable<? extends Executor> $m(){return null;}");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      final CharSequence code = _classbody;
      IMethod _method = this.getMethod(code);
      final IMethod method = _method;
      Optional<IMethodName> _recMethod = this.sut.toRecMethod(method);
      IMethodName _get = _recMethod.get();
      final IMethodName actual = _get;
      String _signature = actual.getSignature();
      Assert.assertEquals("m()Ljava/lang/Iterable;", _signature);
  }
  
  @Test
  public void testArrays() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public Iterable[][] $m(String[][] s){return null;}");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      final CharSequence code = _classbody;
      IMethod _method = this.getMethod(code);
      final IMethod method = _method;
      Optional<IMethodName> _recMethod = this.sut.toRecMethod(method);
      IMethodName _get = _recMethod.get();
      final IMethodName actual = _get;
      String _signature = actual.getSignature();
      Assert.assertEquals("m([[Ljava/lang/String;)[[Ljava/lang/Iterable;", _signature);
  }
  
  @Test
  public void testBoundArg() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public void $m(Iterable<? extends Executor> e){}");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      final CharSequence code = _classbody;
      IMethod _method = this.getMethod(code);
      final IMethod method = _method;
      Optional<IMethodName> _recMethod = this.sut.toRecMethod(method);
      final Optional<IMethodName> actual = _recMethod;
      Assert.assertNotNull(actual);
  }
  
  @Test
  public void testUnboundArg() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("public <T> void $m(T s){}");
      CharSequence _classbody = CodeBuilder.classbody(_builder);
      final CharSequence code = _classbody;
      IMethod _method = this.getMethod(code);
      final IMethod method = _method;
      Optional<IMethodName> _recMethod = this.sut.toRecMethod(method);
      final Optional<IMethodName> actual = _recMethod;
      Assert.assertNotNull(actual);
  }
  
  public IMethod getMethod(final CharSequence code) {
    try {
      IMethod _xblockexpression = null;
      {
        IWorkspace _workspace = ResourcesPlugin.getWorkspace();
        JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
        final JavaProjectFixture fixture = _javaProjectFixture;
        Tuple<ICompilationUnit,Set<Integer>> _createFileAndParseWithMarkers = fixture.createFileAndParseWithMarkers(code);
        final Tuple<ICompilationUnit,Set<Integer>> struct = _createFileAndParseWithMarkers;
        ICompilationUnit _first = struct.getFirst();
        final ICompilationUnit cu = _first;
        Set<Integer> _second = struct.getSecond();
        Integer _head = IterableExtensions.<Integer>head(_second);
        final Integer pos = _head;
        IJavaElement[] _codeSelect = cu.codeSelect((pos).intValue(), 0);
        final IJavaElement[] selected = _codeSelect;
        final IJavaElement[] _typeConverted_selected = (IJavaElement[])selected;
        IJavaElement _get = ((List<IJavaElement>)Conversions.doWrapArray(_typeConverted_selected)).get(0);
        final IMethod method = ((IMethod) _get);
        IMethod _ensureIsNotNull = Checks.<IMethod>ensureIsNotNull(method);
        _xblockexpression = (_ensureIsNotNull);
      }
      return _xblockexpression;
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
