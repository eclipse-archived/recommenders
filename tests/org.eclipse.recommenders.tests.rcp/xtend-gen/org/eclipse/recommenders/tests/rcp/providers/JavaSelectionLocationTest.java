package org.eclipse.recommenders.tests.rcp.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.recommenders.internal.rcp.providers.JavaSelectionUtils;
import org.eclipse.recommenders.rcp.events.JavaSelectionEvent.JavaSelectionLocation;
import org.eclipse.recommenders.tests.XtendUtils;
import org.eclipse.recommenders.tests.jdt.AstUtils;
import org.eclipse.recommenders.utils.Tuple;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Pair;
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
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION, Integer.valueOf(5));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo);
    this.exerciseAndVerify(code, expected);
  }
  
  @Test
  public void testPrimaryTypeDeclaration() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("$pu$blic$ $cl$ass$ $My$class$ $ex$tends$ $Supe$rclass$ $imp$lements$ $Interfac$e1$ {}");
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION, Integer.valueOf(9));
    Pair<JavaSelectionLocation,Integer> _mappedTo_1 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION, Integer.valueOf(3));
    Pair<JavaSelectionLocation,Integer> _mappedTo_2 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION_EXTENDS, Integer.valueOf(3));
    Pair<JavaSelectionLocation,Integer> _mappedTo_3 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION, Integer.valueOf(3));
    Pair<JavaSelectionLocation,Integer> _mappedTo_4 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION_IMPLEMENTS, Integer.valueOf(3));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo, _mappedTo_1, _mappedTo_2, _mappedTo_3, _mappedTo_4);
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
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION, Integer.valueOf(2));
    Pair<JavaSelectionLocation,Integer> _mappedTo_1 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION_IMPLEMENTS, Integer.valueOf(1));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo, _mappedTo_1);
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
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, Integer.valueOf(1));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo);
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
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_BODY, Integer.valueOf(1));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo);
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
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.FIELD_DECLARATION, Integer.valueOf(3));
    Pair<JavaSelectionLocation,Integer> _mappedTo_1 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.FIELD_DECLARATION, Integer.valueOf(1));
    Pair<JavaSelectionLocation,Integer> _mappedTo_2 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.FIELD_DECLARATION, Integer.valueOf(2));
    Pair<JavaSelectionLocation,Integer> _mappedTo_3 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, Integer.valueOf(1));
    Pair<JavaSelectionLocation,Integer> _mappedTo_4 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, Integer.valueOf(4));
    Pair<JavaSelectionLocation,Integer> _mappedTo_5 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.FIELD_DECLARATION_INITIALIZER, Integer.valueOf(5));
    Pair<JavaSelectionLocation,Integer> _mappedTo_6 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.TYPE_DECLARATION, Integer.valueOf(1));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo, _mappedTo_1, _mappedTo_2, _mappedTo_3, _mappedTo_4, _mappedTo_5, _mappedTo_6);
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
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_DECLARATION, Integer.valueOf(3));
    Pair<JavaSelectionLocation,Integer> _mappedTo_1 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_DECLARATION_RETURN, Integer.valueOf(3));
    Pair<JavaSelectionLocation,Integer> _mappedTo_2 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_DECLARATION, Integer.valueOf(3));
    Pair<JavaSelectionLocation,Integer> _mappedTo_3 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_DECLARATION_PARAMETER, Integer.valueOf(7));
    Pair<JavaSelectionLocation,Integer> _mappedTo_4 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_DECLARATION, Integer.valueOf(1));
    Pair<JavaSelectionLocation,Integer> _mappedTo_5 = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_DECLARATION_THROWS, Integer.valueOf(3));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo, _mappedTo_1, _mappedTo_2, _mappedTo_3, _mappedTo_4, _mappedTo_5);
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
    final CharSequence code = _builder;
    Pair<JavaSelectionLocation,Integer> _mappedTo = Pair.<JavaSelectionLocation, Integer>of(JavaSelectionLocation.METHOD_BODY, Integer.valueOf(10));
    final List<JavaSelectionLocation> expected = XtendUtils.<JavaSelectionLocation>newListWithFrequency(_mappedTo);
    this.exerciseAndVerify(code, expected);
  }
  
  private void exerciseAndVerify(final CharSequence code, final List<JavaSelectionLocation> expected) {
    String _string = code.toString();
    final Tuple<CompilationUnit,Set<Integer>> markers = AstUtils.createAstWithMarkers(_string);
    final CompilationUnit cu = markers.getFirst();
    final Set<Integer> pos = markers.getSecond();
    final ArrayList<Object> actual = CollectionLiterals.<Object>newArrayList();
    for (final Integer position : pos) {
      {
        final JavaSelectionLocation selection = JavaSelectionUtils.resolveSelectionLocationFromAst(cu, (position).intValue());
        actual.add(selection);
      }
    }
    Assert.assertEquals(expected, actual);
  }
}
