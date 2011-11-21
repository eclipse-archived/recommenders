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
      _builder.append("\u2022pack\u2022age org.\u2022eclipse.recommenders.extdoc.rcp.selection2;\u2022");
      _builder.newLine();
      _builder.append("imp\u2022ort List;");
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
      _builder.append("\u2022pu\u2022blic\u2022 \u2022cl\u2022ass\u2022 \u2022My\u2022class\u2022 \u2022ex\u2022tends\u2022 \u2022Supe\u2022rclass\u2022 \u2022imp\u2022lements\u2022 \u2022Interfac\u2022e1\u2022 {}");
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
      _builder.append("class MyClas\u2022s2 impl\u2022ements L\u2022istener {");
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
      _builder.append("Class2 c = new L\u2022istener(){");
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
      _builder.append("Listener l = new L\u2022istener(){");
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
      _builder.append("p\u2022ublic stat\u2022ic St\u2022ring \u2022  f\u2022ield\u2022 =\u2022 \u2022new\u2022 St\u2022ring(\"\u2022\")\u2022.\u2022toStri\u2022ng(\u2022)\u2022; \u2022");
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
      _builder.append("\u2022pu\u2022blic\u2022 \u2022Stri\u2022ng\u2022 \u2022metho\u2022d\u2022(\u2022St\u2022ring\u2022 a\u2022rg0\u2022, S\u2022tring \u2022arg1) th\u2022rows \u2022IllegalA\u2022rgumentEception\u2022 {");
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
      _builder.append("String method(String arg0) throws Exception {\u2022");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("S\u2022tring \u2022s\u20222 = arg0.to\u2022String();");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("if(s2.is\u2022Empty()){");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("// c\u2022omment");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("s2 = s2\u2022.append(\"s\"\u2022)");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("}\u2022");
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
