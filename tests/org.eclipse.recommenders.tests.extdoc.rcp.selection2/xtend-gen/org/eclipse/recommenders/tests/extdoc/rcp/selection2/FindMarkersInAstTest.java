package org.eclipse.recommenders.tests.extdoc.rcp.selection2;

import java.util.Set;
import junit.framework.Assert;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.tests.jdt.AstUtils;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtext.xtend2.lib.StringConcatenation;
import org.junit.Test;

@SuppressWarnings("all")
public class FindMarkersInAstTest {
  @Test
  public void test001() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("$public class X extends Y {}");
      final StringConcatenation code = _builder;
      String _string = code.toString();
      Tuple<CompilationUnit,Set<Integer>> _createAstWithMarkers = AstUtils.createAstWithMarkers(_string);
      final Tuple<CompilationUnit,Set<Integer>> markers = _createAstWithMarkers;
      Set<Integer> _second = markers.getSecond();
      boolean _contains = _second.contains(((Integer)0));
      Assert.assertTrue(_contains);
  }
  
  @Test
  public void test002() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class $X$ {}");
      final StringConcatenation code = _builder;
      String _string = code.toString();
      Tuple<CompilationUnit,Set<Integer>> _createAstWithMarkers = AstUtils.createAstWithMarkers(_string);
      final Tuple<CompilationUnit,Set<Integer>> markers = _createAstWithMarkers;
      CompilationUnit _first = markers.getFirst();
      String _string_1 = _first.toString();
      boolean _contains = _string_1.contains(AstUtils.MARKER);
      Assert.assertFalse(_contains);
      Set<Integer> _second = markers.getSecond();
      boolean _contains_1 = _second.contains(((Integer)6));
      Assert.assertTrue(_contains_1);
      Set<Integer> _second_1 = markers.getSecond();
      boolean _contains_2 = _second_1.contains(((Integer)7));
      Assert.assertTrue(_contains_2);
  }
}
