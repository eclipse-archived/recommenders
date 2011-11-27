package org.eclipse.recommenders.tests.extdoc.rcp.selection2;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionLocation;
import org.eclipse.recommenders.extdoc.rcp.selection2.JavaSelectionUtils;
import org.eclipse.recommenders.tests.extdoc.rcp.selection2.XtendUtils;
import org.eclipse.recommenders.tests.jdt.AstUtils;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xtend2.lib.StringConcatenation;
import org.junit.Test;

@SuppressWarnings("all")
public class JavaSelectionLocationTest {
  @Test
  public void testBeforePackageDeclaration() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("$pack$age org.$eclipse.recommenders.extdoc.rcp.selection2;$");
      _builder.newLine();
      _builder.append("imp$ort List;");
      _builder.newLine();
      _builder.append("class X{}");
      _builder.newLine();
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION, ((Integer)5));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testPrimaryTypeDeclaration() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("$pu$blic$ $cl$ass$ $My$class$ $ex$tends$ $Supe$rclass$ $imp$lements$ $Interfac$e1$ {}");
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION, ((Integer)9));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_1 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION, ((Integer)3));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_2 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION_EXTENDS, ((Integer)3));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_3 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION, ((Integer)3));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_4 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION_IMPLEMENTS, ((Integer)3));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo, _operator_mappedTo_1, _operator_mappedTo_2, _operator_mappedTo_3, _operator_mappedTo_4);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testNestedTypeDeclaration() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Myclass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("class MyClas$s2 impl$ements L$istener {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("public void run(){}");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("};");
      _builder.newLine();
      _builder.append("}");
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION, ((Integer)2));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_1 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION_IMPLEMENTS, ((Integer)1));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo, _operator_mappedTo_1);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testAnonymousTypeDeclarationInFieldInitializer() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Myclass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("Class2 c = new L$istener(){");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("public void run(){}");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("};");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("}");
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, ((Integer)1));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testAnonymousInnerTypeDeclarationInMethodBody() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Myclass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("void m(){");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("Listener l = new L$istener(){");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("public void run(){}");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("};");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_BODY, ((Integer)1));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testFieldDeclaration() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class X {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("p$ublic stat$ic St$ring $  f$ield$ =$ $new$ St$ring(\"$\")$.$toStri$ng($)$; $");
      _builder.newLine();
      _builder.append("}");
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.FIELD_DECLARATION, ((Integer)3));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_1 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.FIELD_DECLARATION, ((Integer)1));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_2 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.FIELD_DECLARATION, ((Integer)2));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_3 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, ((Integer)1));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_4 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, ((Integer)4));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_5 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, ((Integer)5));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_6 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.TYPE_DECLARATION, ((Integer)1));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo, _operator_mappedTo_1, _operator_mappedTo_2, _operator_mappedTo_3, _operator_mappedTo_4, _operator_mappedTo_5, _operator_mappedTo_6);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testMethodDeclaration() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class X {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("$pu$blic$ $Stri$ng$ $metho$d$($St$ring$ a$rg0$, S$tring $arg1) th$rows $IllegalA$rgumentEception$ {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_DECLARATION, ((Integer)3));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_1 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_DECLARATION_RETURN, ((Integer)3));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_2 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_DECLARATION, ((Integer)3));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_3 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_DECLARATION_PARAMETER, ((Integer)7));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_4 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_DECLARATION, ((Integer)1));
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo_5 = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_DECLARATION_THROWS, ((Integer)3));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo, _operator_mappedTo_1, _operator_mappedTo_2, _operator_mappedTo_3, _operator_mappedTo_4, _operator_mappedTo_5);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testMethodBody() {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class X {");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("\t ");
      _builder.append("String method(String arg0) throws Exception {$");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("S$tring $s$2 = arg0.to$String();");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("if(s2.is$Empty()){");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("// c$omment");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("s2 = s2$.append(\"s\"$)");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("}$");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      final StringConcatenation code = _builder;
      Pair<JavaSelectionLocation,Integer> _operator_mappedTo = ObjectExtensions.<JavaSelectionLocation, Integer>operator_mappedTo(JavaSelectionLocation.METHOD_BODY, ((Integer)10));
      List<JavaSelectionLocation> _newListWithFrequency = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_operator_mappedTo);
      final List<JavaSelectionLocation> expected = _newListWithFrequency;
      this.exerciseAndVerify(code, expected);
  }
  
  private void exerciseAndVerify(final StringConcatenation code, final List<JavaSelectionLocation> expected) {
      String _string = code.toString();
      Tuple<CompilationUnit,Set<Integer>> _createAstWithMarkers = AstUtils.createAstWithMarkers(_string);
      final Tuple<CompilationUnit,Set<Integer>> markers = _createAstWithMarkers;
      CompilationUnit _first = markers.getFirst();
      final CompilationUnit cu = _first;
      Set<Integer> _second = markers.getSecond();
      final Set<Integer> pos = _second;
      ArrayList<Object> _newArrayList = CollectionLiterals.<Object>newArrayList();
      final ArrayList<Object> actual = _newArrayList;
      for (final Integer position : pos) {
        {
          JavaSelectionLocation _resolveSelectionLocationFromAst = JavaSelectionUtils.resolveSelectionLocationFromAst(cu, position);
          final JavaSelectionLocation selection = _resolveSelectionLocationFromAst;
          actual.add(selection);
        }
      }
      Assert.assertEquals(expected, actual);
  }
}
