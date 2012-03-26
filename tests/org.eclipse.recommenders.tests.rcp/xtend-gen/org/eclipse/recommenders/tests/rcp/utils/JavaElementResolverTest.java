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
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.tests.CodeBuilder;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Checks;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.recommenders.utils.names.IMethodName;
import org.eclipse.recommenders.utils.names.VmMethodName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.eclipse.recommenders.utils.rcp.JavaElementResolver;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Test;

@SuppressWarnings("all")
public class JavaElementResolverTest {
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
      boolean _isPresent = actual.isPresent();
      Assert.assertTrue(_isPresent);
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
      boolean _isPresent = actual.isPresent();
      Assert.assertTrue(_isPresent);
  }
  
  @Test
  public void testJdtMethods() {
      VmMethodName _get = VmMethodName.get("Ljava/lang/Object.hashCode()I");
      Optional<IMethod> _jdtMethod = this.sut.toJdtMethod(_get);
      boolean _isPresent = _jdtMethod.isPresent();
      Assert.assertTrue("no hashCode?", _isPresent);
      VmMethodName _get_1 = VmMethodName.get("Ljava/util/Arrays.sort([J)V");
      Optional<IMethod> _jdtMethod_1 = this.sut.toJdtMethod(_get_1);
      boolean _isPresent_1 = _jdtMethod_1.isPresent();
      Assert.assertTrue("no Arrays.sort?", _isPresent_1);
      VmMethodName _get_2 = VmMethodName.get("Ljava/util/Arrays.equals([Ljava/lang/Object;[Ljava/lang/Object;)Z");
      Optional<IMethod> _jdtMethod_2 = this.sut.toJdtMethod(_get_2);
      boolean _isPresent_2 = _jdtMethod_2.isPresent();
      Assert.assertTrue("no Arrays.equals?", _isPresent_2);
  }
  
  @Test
  public void testJdtClass() {
      Optional<IType> _jdtType = this.sut.toJdtType(VmTypeName.NULL);
      boolean _isPresent = _jdtType.isPresent();
      Assert.assertFalse("Lnull found???", _isPresent);
      Optional<IType> _jdtType_1 = this.sut.toJdtType(VmTypeName.BOOLEAN);
      boolean _isPresent_1 = _jdtType_1.isPresent();
      Assert.assertFalse("primitive found???", _isPresent_1);
      Optional<IType> _jdtType_2 = this.sut.toJdtType(VmTypeName.OBJECT);
      boolean _isPresent_2 = _jdtType_2.isPresent();
      Assert.assertTrue("Object not found???", _isPresent_2);
      Optional<IType> _jdtType_3 = this.sut.toJdtType(VmTypeName.JavaLangNullPointerException);
      boolean _isPresent_3 = _jdtType_3.isPresent();
      Assert.assertTrue("NPE not found???", _isPresent_3);
      VmTypeName _get = VmTypeName.get("Ljava/util/Map$Entry");
      Optional<IType> _jdtType_4 = this.sut.toJdtType(_get);
      boolean _isPresent_4 = _jdtType_4.isPresent();
      Assert.assertTrue("NPE not found???", _isPresent_4);
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
