package org.eclipse.recommenders.tests.rcp.providers;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils;
import org.eclipse.recommenders.tests.XtendUtils;
import org.eclipse.recommenders.tests.jdt.JavaProjectFixture;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("all")
public class JavaElementSelectionTest {
  @Test
  public void testTypeSelectionInTypeDeclaration() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Myc$lass {}");
    final CharSequence code = _builder;
    Pair<String,Integer> _mappedTo = Pair.<String, Integer>of("LMyclass;", Integer.valueOf(1));
    final List<String> expected = XtendUtils.<String>newListWithFrequency(_mappedTo);
    this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testTypeSelectionsInMethodBody() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Myclass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test(String s1){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("Str$ing s = new St$ring(\"\");");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    Pair<String,Integer> _mappedTo = Pair.<String, Integer>of("Ljava/lang/String;", Integer.valueOf(1));
    Pair<String,Integer> _mappedTo_1 = Pair.<String, Integer>of("Ljava/lang/String;.(Ljava/lang/String;)V", Integer.valueOf(1));
    final List<String> expected = XtendUtils.<String>newListWithFrequency(_mappedTo, _mappedTo_1);
    this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testTypeSelectionInExtends() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("import java.util.*;");
    _builder.newLine();
    _builder.append("class Myclass123 extends L$ist {}");
    _builder.newLine();
    final CharSequence code = _builder;
    Pair<String,Integer> _mappedTo = Pair.<String, Integer>of("Ljava/util/List<>;", Integer.valueOf(1));
    final List<String> expected = XtendUtils.<String>newListWithFrequency(_mappedTo);
    this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testTypeSelectionInFieldDeclaration() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Myclass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("Str$ing s = new St$ring(\"\");");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    Pair<String,Integer> _mappedTo = Pair.<String, Integer>of("Ljava/lang/String;", Integer.valueOf(1));
    Pair<String,Integer> _mappedTo_1 = Pair.<String, Integer>of("Ljava/lang/String;.(Ljava/lang/String;)V", Integer.valueOf(1));
    final List<String> expected = XtendUtils.<String>newListWithFrequency(_mappedTo, _mappedTo_1);
    this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testEmptySelectionInClassBody() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Myclass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("$");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    List<String> _emptyList = Collections.<String>emptyList();
    this.exerciseAndVerify(code, _emptyList);
  }
  
  @Test
  public void testMethodSelectionInMethodBody() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("class Myclass {");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("void test(String s1){");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("String s2 = s1.co$ncat(\"hello\");");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("s2.hashCode$();");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("s1.$equals(s2);");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("}");
    _builder.newLine();
    _builder.append("\t");
    _builder.newLine();
    _builder.append("}");
    final CharSequence code = _builder;
    Pair<String,Integer> _mappedTo = Pair.<String, Integer>of("Ljava/lang/String;.concat(Ljava/lang/String;)Ljava/lang/String;", Integer.valueOf(1));
    Pair<String,Integer> _mappedTo_1 = Pair.<String, Integer>of("Ljava/lang/String;.hashCode()I", Integer.valueOf(1));
    Pair<String,Integer> _mappedTo_2 = Pair.<String, Integer>of("Ljava/lang/String;.equals(Ljava/lang/Object;)Z", Integer.valueOf(1));
    final List<String> expected = XtendUtils.<String>newListWithFrequency(_mappedTo, _mappedTo_1, _mappedTo_2);
    this.exerciseAndVerify(code, expected);
  }
  
  @Test
  @Ignore("Only for debugging the ui")
  public void waitAlongTime() {
    try {
      int _multiply = (120 * 1000);
      Thread.sleep(_multiply);
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void exerciseAndVerify(final CharSequence code, final List<String> expected) {
    try {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      JavaProjectFixture _javaProjectFixture = new JavaProjectFixture(_workspace, "test");
      final JavaProjectFixture fixture = _javaProjectFixture;
      final Tuple<ICompilationUnit,Set<Integer>> struct = fixture.createFileAndParseWithMarkers(code);
      final ICompilationUnit cu = struct.getFirst();
      boolean _equals = Objects.equal(cu, null);
      if (_equals) {
        Assert.fail("cu is not allowed to be null!");
      }
      final Set<Integer> pos = struct.getSecond();
      final List<String> actual = CollectionLiterals.<String>newArrayList();
      for (final Integer position : pos) {
        {
          final Optional<IJavaElement> selection = JavaSelectionUtils.resolveJavaElementFromTypeRootInEditor(cu, (position).intValue());
          boolean _isPresent = selection.isPresent();
          if (_isPresent) {
            final IJavaElement t = selection.get();
            boolean _matched = false;
            if (!_matched) {
              if (t instanceof IType) {
                final IType _iType = (IType)t;
                _matched=true;
                String _key = _iType.getKey();
                actual.add(_key);
              }
            }
            if (!_matched) {
              if (t instanceof IMethod) {
                final IMethod _iMethod = (IMethod)t;
                _matched=true;
                String _key = _iMethod.getKey();
                actual.add(_key);
              }
            }
          }
        }
      }
      Assert.assertEquals(expected, actual);
    } catch (Exception _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
